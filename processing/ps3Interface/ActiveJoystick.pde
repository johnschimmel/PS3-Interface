class ActiveJoystick {
  Ellipse2D container, centerDot; //contains xPos, yPos, width and height

  int dotX, dotY; // the position of the joystick dot
  int dotWidth =  30;

  
  //colors
  color containerStroke = color(255,0,0);
  color dotColor = color(255,0,0);

  char mode = 'L'; // L for left joystick, R for right
  String commandLeftJoy, commandRightJoy;

  int analogXVal, analogYVal = 128; //default center position
  boolean active = false;

  //button container
  Rectangle labelContainer, modeToggleBtn, switchButtonLabel,clickConfigBtn;

  Button switchButtonL,switchButtonR; //button that is pressed when ActiveJoystick is used and mouse clicked.
  Boolean switchPressed;
  ArrayList selectedButtons, joystickButtons;
  boolean displayClickConfigButtons = false;

  //colors
  color leftFill = color(20,255,50); //green ;
  color rightFill = color(0,120,255); //blue;
  
  //boolean
  boolean locked = false;
  
  ActiveJoystick(int x, int y, int w, int h) {
    this.container = new Ellipse2D.Double(x,y,w+(this.dotWidth/2),h+(this.dotWidth/2));
    this.dotX = (int)this.container.getX()+((int)this.container.getWidth()/2)-(this.dotWidth/2);
    this.dotY = (int)this.container.getY()/2+((int)this.container.getHeight()/2)-(this.dotWidth/2);

    this.centerDot = new Ellipse2D.Double(x + (w/2), y + (h/2),this.dotWidth,this.dotWidth);

    //this.joystickButtons = (ArrayList) currentLayout.buttons.clone();
    Button tmpLeftJoy = (Button)getButtonByName("L Joy");
    this.commandLeftJoy = tmpLeftJoy.commandString;

    Button tmpRightJoy = (Button)getButtonByName("R Joy");
    this.commandRightJoy = tmpRightJoy.commandString;

    this.selectedButtons = new ArrayList();
    this.selectedButtons.add((Button)getButtonByName("Cross"));
    this.selectedButtons.add((Button)getButtonByName("Circle"));
    this.selectedButtons.add((Button)getButtonByName("Square"));
    this.selectedButtons.add((Button)getButtonByName("Triangle"));
    this.selectedButtons.add((Button)getButtonByName("DPad Up"));
    this.selectedButtons.add((Button)getButtonByName("DPad Down"));
    this.selectedButtons.add((Button)getButtonByName("DPad Left"));
    this.selectedButtons.add((Button)getButtonByName("DPad Right"));
    this.selectedButtons.add((Button)getButtonByName("L1 Trigger"));
    this.selectedButtons.add((Button)getButtonByName("L2 Trigger"));
    this.selectedButtons.add((Button)getButtonByName("R1 Trigger"));
    this.selectedButtons.add((Button)getButtonByName("R2 Trigger"));    
    this.joystickButtons = new ArrayList();

    int currX = (int)this.container.getX()+(int)this.container.getWidth()/6;
    int yPos = (int)this.container.getHeight()+(int)this.container.getY()+10;
    int currMaxHeight = 0;
    for(int i = 0; i < selectedButtons.size(); i++ ) {
      Button tb = (Button) selectedButtons.get(i);
      Button tmpButtonObj = new Button(tb);

      if ( i>0 && ((i%6)==0) ) { 
        yPos = currMaxHeight+yPos+5; 
        currX = (int)this.container.getX()+(int)this.container.getWidth()/6;
      }

      tmpButtonObj.setX((currX));
      tmpButtonObj.setY(yPos);
      tmpButtonObj.resize(2);

      if (currMaxHeight < tmpButtonObj.getHeight() ) {
        currMaxHeight =(int) tmpButtonObj.getHeight(); 
      }
      // x position for next button with 5px spacing
      currX += tmpButtonObj.getWidth() + 5;

      this.joystickButtons.add(tmpButtonObj);
    }

    Button tButton = (Button)joystickButtons.get(0);
    this.switchButtonL = new Button(tButton); 
    Button tButton2 = (Button)joystickButtons.get(1);
    this.switchButtonR = new Button(tButton2); 
    this.switchPressed = false;

    this.labelContainer = new Rectangle();
    this.labelContainer.setLocation(10, height-70);

    this.modeToggleBtn = new Rectangle();
    this.clickConfigBtn = new Rectangle();
    this.switchButtonLabel = new Rectangle(); //display the current switchButton namej
  }

  void display() {

    if (this.active) {

      stroke(this.containerStroke);

      smooth();
      if (this.mode == 'L') {
       stroke(this.leftFill); 
      } else {
       stroke(this.rightFill);
      }
      fill(255);
      ellipseMode(CORNER);
      ellipse((int)this.container.getX(),(int)this.container.getY(),(int)this.container.getWidth(), (int)this.container.getHeight());

      //cross hairs
      stroke(120);
      int midX =(int) (this.container.getMaxX()+this.container.getMinX())/2;
      int midY =(int) (this.container.getMaxY()+this.container.getMinY())/2;
      line(midX, (int)this.container.getMinY()-(this.dotWidth/2), midX, (int)this.container.getMaxY()+this.dotWidth/2);
      line( (int)this.container.getMinX()-(this.dotWidth/2),midY, (int)this.container.getMaxX()+this.dotWidth-(this.dotWidth/2),midY);

      //center dot
      stroke(120);
      ellipseMode(CENTER);
      ellipse((int)this.centerDot.getX()+(this.dotWidth/4),(int)this.centerDot.getY()+(this.dotWidth/4),(int)this.centerDot.getWidth(), (int)this.centerDot.getHeight());
      
      if (!this.locked) {      
        //send command
        this.sendCommand();
      }
      
      //toggle the click lock and unlock
      if (this.centerDot.contains(mouseX, mouseY) && mousePressed) {
        this.locked=true;
      } else if (this.locked && this.container.contains(mouseX, mouseY) && mousePressed) {
        this.locked = false; 

      }
      
      this.displayMouseDot();
      //display the click config button area 
      if (displayClickConfigButtons) {
        this.displayButtons();
      }
    }
    
    //display the lower left label

    this.displayScreenJoyLabel();
  }

  void displayMouseDot() {
    noStroke();
    if (this.mode == 'L') {
      fill(this.leftFill); 
    } else {
     fill(this.rightFill);
    }
    
    if (this.centerDot.contains(mouseX, mouseY)) {
     fill(255,0,0);
    }

    if (this.container.contains(mouseX,mouseY) ) {
      dotX = mouseX;
      dotY = mouseY;
    } 
    
    if (this.locked) {
      fill(255,0,0);
      ellipseMode(CENTER);
      ellipse((int)this.centerDot.getX()+(this.dotWidth/4),(int)this.centerDot.getY()+(this.dotWidth/4),(int)this.centerDot.getWidth(), (int)this.centerDot.getHeight());
      
    } else {
      ellipseMode(CENTER);
      ellipse( dotX, dotY, this.dotWidth, this.dotWidth);
    }
  }

  void sendCommand() {
    String commandString = "";
    if (this.mode == 'L') {
      commandString = this.commandLeftJoy; 
    } 
    else {
      commandString =this. commandRightJoy; 
    }

    if (this.container.contains(mouseX, mouseY)) {
      //send X
      String xCommandString = commandString.toLowerCase();
      int tempXVal =  (int)map(mouseX, (int)this.container.getX(), ((int)this.container.getWidth()+(int)this.container.getX()), 0,255);
      this.analogXVal = constrain(tempXVal, 0, 255);

      myPort.write(xCommandString);
      myPort.write(this.analogXVal);

      //send Y 
      String yCommandString = commandString;
      int tempYVal =  (int)map(mouseY,  (int)this.container.getY(), ((int)this.container.getHeight()+(int)this.container.getY()), 0, 255);
      this.analogYVal = constrain(tempYVal, 0,255);

      myPort.write(yCommandString);
      myPort.write(this.analogYVal);

      if (debugOn) println("sending ScreenJoystick (" + this.mode + ") : " + xCommandString + "=" + analogXVal + "   " + yCommandString + "=" + analogYVal);
    }
    //switch Button control
    if (mousePressed &&  this.container.contains(mouseX,mouseY) ) { 
      if (mouseButton == LEFT) {
        this.switchButtonL.setX(mouseX-(int)this.switchButtonL.getWidth()/2);
        this.switchButtonL.setY(mouseY-(int)this.switchButtonL.getHeight()-this.dotWidth);
        this.switchButtonL.display();
        this.switchButtonL.sendCommand(); 
        this.switchButtonL.highlight(true);
        this.switchPressed = true;
      } else {
        this.switchButtonR.setX(mouseX-(int)this.switchButtonR.getWidth()/2);
        this.switchButtonR.setY(mouseY-(int)this.switchButtonR.getHeight()-this.dotWidth);
        this.switchButtonR.display();
        this.switchButtonR.sendCommand(); 
        this.switchButtonR.highlight(true);
        this.switchPressed = true;  
      }

    } 
    else if (!mousePressed && this.switchPressed) {

      this.switchButtonL.forceReset();
      this.switchButtonR.forceReset();

      this.switchPressed = false;

    }

  }


  boolean activate() {
    this.active = true; 
    return true;
  }

  boolean deactivate() {
    this.reset(); //sets x and y  to 128, centered position

    this.active = false;
    return true; 
  }

  boolean  isActive() {
    return this.active;
  }

  void toggleActivity() {
    this.reset(); //sets x and y  to 128, centered position
    this.active = !this.active;
  }

  boolean clicked() {
    if (this.labelContainer.contains(mouseX,mouseY)) {
      return true; 
    }
    else {
      return false; 
    }

  }

  void displayScreenJoyLabel() {
    //print the x and y analog values to the 
    //setup label
    textFont(font);
    String label = "ACTIVE JOYSTICK ";

  
    if (this.isActive()) {
      fill(30);
      rect(0, (int)this.labelContainer.getY(),width,(int)this.labelContainer.getY()+20);
      label += "ON   X:" + analogXVal + "   Y:" + analogYVal;
      if ( this.mode == 'R') {
        fill(0,120,255); //blue
      } 
      else {
        fill(20,255,50); //green 
      }  
    } 
    else {
      label += "OFF";
      fill(255,0,20);
    }

    int labelWidth = (int)textWidth(label);
    this.labelContainer.setSize(labelWidth, 20); //height 20

      //end of label definition

    text(label,(int)this.labelContainer.getX(),(int)this.labelContainer.getY()+20 ); 

    //display LEFT RIGHT buttons
    if (this.isActive()) {
      this.displayLeftRightToggle();
      
    }

  }

  void displayButtons() {
    Button firstButton = (Button) this.joystickButtons.get(0);
    
    Button lastButton = (Button) this.joystickButtons.get(this.joystickButtons.size()-1);
    
    fill(255);
    stroke(100);
    rect(firstButton.theRect.x-10, firstButton.theRect.y-5, 340, 90);
    noStroke();
    noFill();
    
    for(int i=0; i<this.joystickButtons.size(); i++) {
      Button tButton = (Button) this.joystickButtons.get(i);
      tButton.display();

      if (tButton.theRect.contains(mouseX, mouseY)) {
        tButton.highlight(true); 
        if (mousePressed) {
           if (mouseButton == LEFT) {
              this.switchButtonL =new Button(tButton);
           } else {
              this.switchButtonR =new Button(tButton);
             
           }
        }
      } 
      else {
        tButton.highlight(false); 
      }

      //button group highlighted
      if (tButton.name == this.switchButtonL.name || tButton.name == this.switchButtonR.name) {
        tButton.highlight(true); 
      }
    } 

  }

  void displayLeftRightToggle() {
   
   fill(120);
   String leftStr = "L-JOY";
   String rightStr = "R-JOY";
   String clickConfStr = "CLICK CONFIG";

    int tmpX = (int)this.labelContainer.getWidth() + 15;
    int tmpY = (int)this.labelContainer.getY() ;   
    this.modeToggleBtn.setLocation(tmpX, tmpY);
    this.modeToggleBtn.setSize((int)textWidth(leftStr)+(int)textWidth(rightStr),20); //110 shouldn't be hardcoded, should be 
   
   this.clickConfigBtn.setLocation( (int)this.modeToggleBtn.getX()+ (int)this.modeToggleBtn.getWidth()+50, tmpY);
    this.clickConfigBtn.setSize((int)textWidth(clickConfStr), 20);
   
   //rect((int)this.modeToggleBtn.getX(),(int)this.modeToggleBtn.getY()-5, (int)this.modeToggleBtn.getWidth(), (int)this.modeToggleBtn.getHeight()+20);
   int leftFill,rightFill = 0;
   
   if ( this.mode == 'L') {
      leftFill = this.leftFill; //green ;
      rightFill = 120;
     
   } else {
      leftFill =120;
      rightFill = this.rightFill; //blue;
      
   }
   
   fill(leftFill);
   text(leftStr,(int)this.modeToggleBtn.getX()+5,(int)this.modeToggleBtn.getY()+20);
   fill(rightFill);
   text(rightStr, (int)this.modeToggleBtn.getX()+(int)textWidth(leftStr)+15,(int)this.modeToggleBtn.getY()+20);
   
   //display click config label
   fill(120);
   text(clickConfStr, (int)this.clickConfigBtn.getX(),(int)this.clickConfigBtn.getY()+20);
  }
  
  
  boolean leftRightClicked() {
    if (this.modeToggleBtn.contains(mouseX, mouseY)) {
     return true; 
    } else {
     return false; 
    }
    
  }
  
  void toggleLeftRight() {
    this.reset();
   if (this.mode == 'L') {
     this.mode = 'R';
   }  else {
    this.mode = 'L'; 
   }
  }


  boolean clickConfigClicked() {
    if (this.clickConfigBtn.contains(mouseX, mouseY)) {
     this.displayClickConfigButtons = !this.displayClickConfigButtons;
     return true;
    } else {
     return false; 
    }
      
    
  }
  

  
  void reset() {

    this.switchButtonL.forceReset();
    this.switchButtonR.forceReset();
    
    String commandString = "";
    if (this.mode == 'L') {
      commandString = commandLeftJoy; 
    } 
    else {
      commandString = commandRightJoy; 
    }

    //send X
    String xCommandString = commandString.toLowerCase();
    this.analogXVal = 128;
    myPort.write(xCommandString);
    myPort.write(this.analogXVal);

    //send Y 
    String yCommandString = commandString;
    this.analogYVal = 128;
    myPort.write(yCommandString);
    myPort.write(this.analogYVal);


    if (debugOn){
      println("resetting activejoy");
      println("sending activejoy (" + this.mode + ") : " + xCommandString + "=" + analogXVal + "   " + yCommandString + "=" + analogYVal);
    }
  }



}












