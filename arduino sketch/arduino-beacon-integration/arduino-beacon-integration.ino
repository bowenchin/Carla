#include <SoftwareSerial.h>

String beaconName = "CB107:";
//CB141
bool inRange = false;
int maxAttempts = 10;
int attempts = 0;
int rangeSignal = 12;
SoftwareSerial mySerial(5, 6); // RX, TX
//SoftwareSerial outSerial(2,3); // RX, TX

void setup() {
// Open serial communications and wait for port to open:
Serial.begin(9600);
while (!Serial) {
// wait for serial port to connect. Needed for native USB port only
}

// set the data rate for the SoftwareSerial port
mySerial.begin(9600);
mySerial.write("AT");
delay(100);
//outSerial.begin(9600);
delay(100);

pinMode(rangeSignal, OUTPUT);

}

void loop() { // run over and over
String beaconMessage;
beaconMessage = findBeacon();
if(beaconMessage !="NO CHANGE") {
Serial.print(beaconMessage);
if (beaconMessage==("Device=" + beaconName + ";STATUS=IN"))
{
digitalWrite(rangeSignal, HIGH);

//outSerial.write("in");

delay(1000);
}
else
{
digitalWrite(rangeSignal, LOW);

//outSerial.write("out");
delay(1000);
}
}
 //outSerial.write('~'); //used as an end of transmission character - used in app for string length
 //outSerial.write('\n');
delay(50);
}

void sendToPhone(String message){
//Send IN/OUT range to the phone
//Set up Bluetooth to transmit data

mySerial.print(message);
delay(50);
}

String findBeacon() {
String disiOutput;
mySerial.flush();
//mySerial.write("AT");
mySerial.write("AT+DISI?");
if (mySerial.available()) {
//Serial.write(mySerial.read());
disiOutput = mySerial.readString();
}
String message;

int beacIndx = disiOutput.indexOf(beaconName);
String distance = disiOutput.substring(beacIndx+8,beacIndx+10);
int rssiValue = distance.toInt();
Serial.println("Distance "+ String(rssiValue)+"//");
message = "NO CHANGE";
if (rssiValue > 0) {
Serial.print(beaconName);
Serial.print(" ");
Serial.println(rssiValue);
attempts = 0;
if(!inRange) {
message = "Device=" + beaconName + ";STATUS=IN";
Serial.println("Device=" + beaconName + ";STATUS=IN");
inRange = true;
}
}
else {
Serial.print(rssiValue);
Serial.print("N/A, beaconIndex = ");
Serial.println(beacIndx);
attempts++;
if(attempts >= maxAttempts) {
if(inRange){
message = "Device=" + beaconName + ";STATUS=OUT";
Serial.println("Device=" + beaconName + ";STATUS=OUT");
inRange = false;
}
attempts = 0;
}
}
//6CB107
return message;
}

