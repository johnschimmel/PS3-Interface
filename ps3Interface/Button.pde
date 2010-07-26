class Button {

  Rectangle theRect;
  color theColor;
  String name;
  String commandString;
  PImage buttonImage;
  String imageFilename;
  boolean highlight = false; //show the button label on the screen
  boolean setHighlight = false;

  //digital variables
  int digitalMode = 1; //1 - dynamic  2 - holding button

  //analog variables
  Boolean isAnalog = false; //is this button a joystick
  int analogMode = 1; //1 - dynamic, 2 - hold till next button press,  3 - hold until double clicked
  int analogX = 128; // center
  int analogY = 128; // center

  Boolean active = false; //the button was pressed, needs to be reset?
  float lastPress = 0; //keeps millis of time button was pressed

  int buttonPadding = 20;

  Button(String _name, String _imageFilename, String _commandStr, int _xpos, int _ypos, String _isAnalog) {
    this.name = _name;

    //load image
    imageFilename = _imageFilename;
    this.buttonImage = loadImage(_imageFilename);

    //command string to send over serial
    this.commandString = _commandStr;

    Boolean isItAnalog =  new Boolean(_isAnalog);
    if (isItAnalog == true) {
      //is this button analog
      this.isAnalog = true;
      this.analogMode = 1;
    } 
    else {
      this.isAnalog = false;
      this.digitalMode = 1;

    }

    //preparing the buttonImage
    if (this.buttonImage != null) {
      this.theRect = new Rectangle(_xpos, _ypos,this.buttonImage.width, this.buttonImage.height+this.buttonPadding); 
    }



  }

  Button(Button anotherButton) {
    this.name = anotherButton.name;

    //load image
    imageFilename = anotherButton.imageFilename;
    this.buttonImage = loadImage(anotherButton.imageFilename);

    //command string to send over serial
    this.commandString = anotherButton.commandString;

    Boolean isItAnalog =  new Boolean(anotherButton.isAnalog);
    if (isItAnalog == true) {
      //is this button analog
      this.isAnalog = true;
      this.analogMode = 1;
    } 
    else {
      this.isAnalog = false;
      this.digitalMode = 1;

    }

    this.theRect =  new Rectangle(anotherButton.theRect); 



  }

  void display() {


    image(this.buttonImage, this.theRect.x, this.theRect.y, (float) this.theRect.getWidth(), (float)this.theRect.getHeight()-this.buttonPadding);

    if ( (this.highlight) || (this.setHighlight) ||   (!this.isAnalog && this.digitalMode == 2 ) ) {
      textFont(font);

      if ( this.digitalMode == 1) {
        fill(0);
        text(this.name, this.theRect.x, (this.theRect.y + (float)this.theRect.getHeight()) );
        if (!this.isAnalog) {
          stroke(238,57,48); //red
          fill(255);
          ellipse(this.theRect.x+10+textWidth(this.name), (this.theRect.y + (float)this.theRect.getHeight()-5), 10,10);
          noStroke();
        }
      } 
      else {

        fill(238,57,48); //red
        text(this.name, this.theRect.x, (this.theRect.y + (float)this.theRect.getHeight()) );
        noStroke();
        ellipse(this.theRect.x+10+textWidth(this.name), (this.theRect.y + (float)this.theRect.getHeight()-5), 10,10);
        noFill();

      }
    }

    if (this.isAnalog && this.analogMode > 1) {

      int tXpos =(int) map(this.analogX, 0,255,this.theRect.x,this.theRect.x + this.buttonImage.width);
      int tYpos =(int) map(this.analogY, 0,255,this.theRect.y, this.theRect.y +this.buttonImage.height);

      if (this.analogMode ==2) {
        fill(0,0,255);
        text("Analog Single", (this.theRect.x+this.buttonImage.width-100 ), (this.theRect.y + this.buttonImage.height + 20) );

      } 
      else if (this.analogMode == 3) 
      {
        fill(238,57,48);
        text("Analog On", (this.theRect.x+this.buttonImage.width-100 ), (this.theRect.y + this.buttonImage.height + 20) );
      }

      ellipse(tXpos, tYpos, 15, 15); 

    }

  }

  boolean contains( int _x, int _y) {

    return (boolean)this.theRect.contains(_x, _y); 

  }

  void setX(int X) {
    this.theRect.x = X; 
  }

  void setY(int Y) {

    this.theRect.y = Y; 
  }

  int getWidth() {
    return (int)this.theRect.getWidth(); 
  }

  int getHeight() {
    return (int)this.theRect.getHeight();

  }

  void resize(int factor) {

    int newWidth =((int)this.theRect.getWidth())/factor;
    int newHeight =((int)this.theRect.getHeight())/factor;
    this.theRect.setSize(newWidth,newHeight);

  }


  void highlight(boolean _state) {
    this.highlight = _state;
  }

  void setHighlight(boolean _state) {
    this.setHighlight = _state; 
  }

  void updatePosition() {    
    //get offsets
    int offsetX = pmouseX - this.theRect.x;
    int offsetY = pmouseY - this.theRect.y;

    int newX = mouseX - offsetX;
    int newY = mouseY - offsetY;

    if ( (newX > 0) && (newX<width-theRect.width) && (newY>0) && (newY< height-theRect.height-30)) {
      this.theRect.setLocation(newX, newY); 
    }
  }

  int getAnalogX() {
    int tX = mouseX - this.theRect.x;
    int tVal = (int)map(tX, 0, this.theRect.width, 0,255);
    this.analogX = tVal;
    return this.analogX;
  }

  int getAnalogY() {
    int tY =  mouseY - this.theRect.y;
    int tVal = (int)map(tY,0, this.theRect.height, 0,255);
    this.analogY = tVal;
    return this.analogY;

  }

  void toggleHoldDigital() {

    Rectangle rectTarget = new Rectangle(this.theRect.x,  this.theRect.y+this.buttonImage.height, (int)this.theRect.getWidth(),30); 

    if (rectTarget.contains(pmouseX, pmouseY)) {
      if (debugOn) println("Double click on = " + this.name);
    }

    if ( (!this.isAnalog) && (millis() - this.lastPress > 100 )  && (rectTarget.contains(mouseX, mouseY)) ) {

      if (this.digitalMode == 1) {
        this.digitalMode = 2;
      } 
      else {
        this.digitalMode = 1;
      }

      if (debugOn) println("Digital mode = " + this.digitalMode);
      this.lastPress = millis();

    } 

  }

  void toggleHoldAnalog() {

    if (this.isAnalog && (millis() - this.lastPress > 100) ) {

      if (this.analogMode <= 2) {
        this.analogMode++;
      } 
      else if (this.analogMode == 3) {
        this.analogMode = 1; 
      }

      if (debugOn) println("Analog mode = " + this.analogMode);

      this.lastPress = millis();
    } 
  }

  void resetDigitalMode() {
    if (!this.isAnalog && this.digitalMode == 2) {
      this.digitalMode = 1;
    }    
  }

  void resetAnalogMode() {
    if (this.isAnalog && this.analogMode == 2) {
      this.analogMode = 1;
    } 

  }

  void sendCommand() {

    if (!this.isAnalog) {
      myPort.write(this.commandString);
      myPort.write(255);
      if (debugOn) println("sending " + this.name + " " + 255);
    } 
    else {

      //send X
      String xCommandString = this.commandString.toLowerCase();
      int analogXVal = this.getAnalogX();
      myPort.write(xCommandString);
      myPort.write(analogXVal);

      //send Y 
      String yCommandString = this.commandString;
      int analogYVal = this.getAnalogY();
      myPort.write(yCommandString);
      myPort.write(analogYVal);

      if (debugOn) println("sending analog: " + this.name + " " + xCommandString + "=" + analogXVal + "   " + yCommandString + "=" + analogYVal);

    }

    this.active = true;
    this.lastPress = millis();
    lastButtonPressed = this;
  }


  void resetCommand() {

    if (this.active == true && (millis() - this.lastPress > 100)) {
      if (!this.isAnalog) {

        myPort.write(this.commandString);
        myPort.write(0); //off
        this.active = false;
        if (debugOn) println("resetting " + this.name);
      } 
      else if (this.isAnalog && this.analogMode == 1) {

        //send X
        String xCommandString = this.commandString.toLowerCase();
        myPort.write(xCommandString);
        myPort.write(128);

        //send Y 
        String yCommandString = this.commandString;
        myPort.write(yCommandString);
        myPort.write(128);
        this.active = false;

        if (debugOn) println("resetting " + this.name);
      }

    }
  }

  void forceReset() {
    if (!this.isAnalog) {
      myPort.write(this.commandString);
      myPort.write(0); //off
      this.digitalMode = 1;
      this.active = false;
      if (debugOn) println("resetting " + this.name);

    } 
    else {
      //send X
      String xCommandString = this.commandString.toLowerCase();
      myPort.write(xCommandString);
      myPort.write(128);

      //send Y 
      String yCommandString = this.commandString;
      myPort.write(yCommandString);
      myPort.write(128);

      this.analogMode = 1;
    } 
    this.active = false;
    if (debugOn) println("resetting " + this.name);
  }

  //return the xmlElement for this button
  proxml.XMLElement getXML() {

    proxml.XMLElement tempObj =  new proxml.XMLElement("button");
    tempObj.addAttribute("name",this.name);
    tempObj.addAttribute("xpos",this.theRect.x);
    tempObj.addAttribute("ypos",this.theRect.y);

    return tempObj;

  }

}




//global functions

void displayButtonLibrary() {

}
























