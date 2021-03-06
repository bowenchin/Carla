//FOR ARDUINO THAT SENDS OUT

#include <SoftwareSerial.h>

SoftwareSerial mySerial(5,6);
int inPin = 12;

int counter;


void setup() {

  // put your setup code here, to run once:
  Serial.begin(9600);
  mySerial.begin(9600);

  delay(100);

  pinMode(inPin, INPUT);

  delay(100);
  //counter = 0;
}

void loop() {
  // put your main code here, to run repeatedly:
  if(readStatus()==true){
    //mySerial.write('#');
    mySerial.write("IN");
  }
  else{
    //mySerial.write('#');
    mySerial.write("OUT");
  }

   mySerial.write('~'); //used as an end of transmission character - used in app for string length
   mySerial.write('\n');
   Serial.println('~');


 delay(1000);        //added a delay to eliminate missed transmissions

}

bool readStatus(){
  if(digitalRead(inPin)==HIGH){
    Serial.println("IN");
    return true;
  }
  else{
    Serial.println("OUT");
    return false;
  }
}
