#include <Sparki.h> // include the sparki library
 
String inputString; //make an empty String called inputString
boolean returnFlag; //flag to check for carriage return
boolean oKSent; //flag to check for OK communication
char commArray [10]; //array to store communication
int arrayCounter = 0; //integer to count through commArray
int boredCounter = 0;
 
void setup()
{
 Serial1.begin(9600);
 sparki.servo(SERVO_CENTER+10);
}
 
void loop()
{
 readComm();
 makeMove();
 
}
 
void makeMove(){
 for(int i = 0; i <= 9; i++) 
  {
  if(commArray[i] == 'f' || commArray[i] == 'F')
   {
   sparki.moveLeft(90);
   delay(100);
   sparki.beep();
   delay(100);
   sparki.moveRight(90);
   delay(100);
   sparki.moveStop();
   }
   else if (commArray[i] != 0) //in case it's a character sparki doesn't know
   {
   Serial1.print("I'm sorry, I didn't understand the command- ");
   Serial1.println(commArray[i]); //send the character back
   if(sparki.ping() < 10){
    sparki.moveLeft(90);
   }else{
   sparki.moveForward();
     
   }
   delay(1000);
   }
 } 
 memset(commArray, 0, sizeof(commArray)); //clear out commArray
}
 
void readComm()
{
 while (Serial1.available())
 {
 int inByte = Serial1.read();
 if ((char)inByte == '\n')
   {
   returnFlag = true;
   arrayCounter = 0;
   }
   else
   {
   if(inByte == 32) //if it's a blank space
   {
   arrayCounter ++; //increment array counter to store in new array space
   }
   else
   {
   //add the character to the arrayCounter space in commArray
   commArray[arrayCounter] = (char)inByte;
   }
  }
 }
}


