import processing.core.*; 
import processing.xml.*; 

import java.awt.*; 
import java.awt.event.*; 
import java.awt.geom.*; 
import processing.serial.*; 
import proxml.*; 

import java.applet.*; 
import java.awt.Dimension; 
import java.awt.Frame; 
import java.awt.event.MouseEvent; 
import java.awt.event.KeyEvent; 
import java.awt.event.FocusEvent; 
import java.awt.Image; 
import java.io.*; 
import java.net.*; 
import java.text.*; 
import java.util.*; 
import java.util.zip.*; 
import java.util.regex.*; 

public class ps3Interface extends PApplet {








ArrayList theButtons;
ArrayList theButtonLibrary;
ArrayList theLayouts;
ArrayList layoutMenu_buttons;

PFont font;
Serial myPort; //serial port
String[] availablePorts; //used to keep track of available ports
int currentPortNumber = 0; // element number from availablePorts

proxml.XMLElement controller, buttons, joysticks, layouts;
XMLInOut xmlButtons, xmlLayouts;

Button lastButtonPressed, prevButtonPressed = null;
boolean buttonsLocked = true;
boolean debugOn = false;

String lastCommandPressed = "";

//sets
ArrayList theSets;
boolean initialSets = false;

//gui variable
ArrayList menu_buttons;

Layout currentLayout;
int currentLayoutNumber = 0;

ButtonLibrary library;

ActiveJoystick activejoy;

public void setup() {
  size(1366,700);

  println(Serial.list());
  availablePorts = Serial.list();
  println("There are " + availablePorts.length + " serial ports");
  println("----------------------------------------");
  myPort = new Serial(this, Serial.list()[currentPortNumber], 19200);


  theButtons = new ArrayList();
  theButtonLibrary = new ArrayList();
  theLayouts = new ArrayList();
  theSets = new ArrayList();
  layoutMenu_buttons = new ArrayList();

  //proXML object to read in button layout and command information via xml file controllerInfo.xml
  xmlButtons = new XMLInOut(this);
  loadControllerInfo();



  font = loadFont("Futura-CondensedMedium-18.vlw");

  //menu buttons
  menu_buttons = new ArrayList();
  Menu_button buttonLockTemp = new Menu_button("button_lock","(B)uttons locked", "toggle",0,height-35,170,30,color(200),color(255), color(0), color(255,0,0));
  buttonLockTemp.setActiveLabel("(B)uttons unlocked");

  Menu_button buttonLibraryTemp = new Menu_button("button_library","Button (L)ibrary", "toggle",170,height-35,170,30,color(200),color(255), color(0), color(255,0,0));
  Menu_button resetTemp =new Menu_button("reset","(R)eset", "momentary",320,height-35,100,30,color(200),color(255), color(0),color(255,0,0));
  Menu_button serialTemp =new Menu_button("serial","(S)erial - " +availablePorts[currentPortNumber],"momentary", 430,height-35,350,30,color(200),color(255), color(0),color(255,0,0));

  int layoutButtonWidth = 45;
  Menu_button layout1 = new Menu_button("layout_1","(1)","toggle",width-(layoutButtonWidth*5), height-35,layoutButtonWidth,30,color(200),color(255), color(0), color(255,0,0));
  Menu_button layout2 = new Menu_button("layout_2","(2)","toggle",width-(layoutButtonWidth*4), height-35,layoutButtonWidth,30,color(200),color(255), color(0), color(255,0,0));
  Menu_button layout3 = new Menu_button("layout_3","(3)","toggle",width-(layoutButtonWidth*3), height-35,layoutButtonWidth,30,color(200),color(255), color(0), color(255,0,0));
  Menu_button layout4 = new Menu_button("layout_4","(4)","toggle",width-(layoutButtonWidth*2), height-35,layoutButtonWidth,30,color(200),color(255), color(0), color(255,0,0));
  Menu_button layout5 = new Menu_button("layout_5","(5)","toggle",width-(layoutButtonWidth*1), height-35,layoutButtonWidth,30,color(200),color(255), color(0), color(255,0,0));

  menu_buttons.add(buttonLockTemp);
  menu_buttons.add(buttonLibraryTemp);
  menu_buttons.add(resetTemp);
  menu_buttons.add(serialTemp);

  menu_buttons.add(layout1);
  menu_buttons.add(layout2);
  menu_buttons.add(layout3);
  menu_buttons.add(layout4);
  menu_buttons.add(layout5);

  layoutMenu_buttons.add(layout1);
  layoutMenu_buttons.add(layout2);
  layoutMenu_buttons.add(layout3);
  layoutMenu_buttons.add(layout4);
  layoutMenu_buttons.add(layout5);

  library = new ButtonLibrary();

}


public void draw() {
  background(255); //clear the screen each time

  if (currentLayout != null) {
    renderSets();
    currentLayout.display(); 
    
    if (library.isActive) {
      library.display();
    }

  }

  //display status bar on bottom of the window.

  fill(200);
  noStroke();
  rect(0,height-40,width, 40);
  textFont(font);

  fill(0);

  for(int mb=0;mb<menu_buttons.size();mb++) {
    Menu_button tempmb = (Menu_button)menu_buttons.get(mb);
    tempmb.display();
  }
  
  //testJoy.display();
}





public void loadControllerInfo() {

  //load ellipses from file if it exists
  xmlButtons = new XMLInOut(this);
  try{
    xmlButtons.loadElement("xml/buttons.xml");

  }
  catch(Exception e){
    println("file not found");
  }
}

public void loadLayouts() {

  //load layouts
  xmlLayouts = new XMLInOut(this);
  try{
    xmlLayouts.loadElement("xml/layouts.xml"); 
  }
  catch(Exception e){
    println("file not found");
  }


}

public void xmlEvent(proxml.XMLElement element){
  String currentElement = element.getName();
  println("--------------------------------------");
  println("Loading XML <" + currentElement + ">");


  if (currentElement.equals("buttons") && element.hasChildren() ) {
    buttons = element;

    //prepare button objects from xml data
    println("There are  " + buttons.countChildren() + " buttons");
    for(int i =0; i< buttons.countChildren(); i++) {

      proxml.XMLElement tempButton = buttons.getChild(i);
      proxml.XMLElement tempButtonElements[] = tempButton.getChildren();   

      //get the button name
      String buttonName = tempButton.getAttribute("name");
      String buttonImage = "";
      int buttonXPos = 0;
      int buttonYPos = 0;
      String buttonCommand = "";
      String isAnalog = "";

      //get the button elements
      for(int e=0; e<tempButton.countChildren(); e++ ) {
        proxml.XMLElement tempElement = tempButton.getChild(e);

        if (tempElement.getElement().equals("image")) {
          buttonImage = tempElement.getAttribute("file");
        } 

        else if (tempElement.getElement().equals("command")) {
          buttonCommand = tempElement.getAttribute("string");

        } 
        else if (tempElement.getElement().equals("analog")) {
          isAnalog = tempElement.getAttribute("value"); 
        }

      } //end of for loop for button elements

      Button tb = new Button(buttonName, buttonImage,buttonCommand, buttonXPos, buttonYPos,isAnalog);
      theButtonLibrary.add(tb);

    } //end of for loop for buttons   
    xmlLayouts = new XMLInOut(this);

    library = new ButtonLibrary();
    loadLayouts();


  } 
  else if (currentElement.equals("layouts") && element.hasChildren() ) {
    layouts = element;

    print(currentElement);
    println("There are  " + layouts.countChildren() + " layouts");
    for(int i =0; i< layouts.countChildren(); i++) {
      proxml.XMLElement tempLayout = layouts.getChild(i);
      proxml.XMLElement layoutButtons[] = tempLayout.getChildren();

      Layout tempLayoutObj = new Layout(); //create new layout obj
      println("# of buttons in layout = " + tempLayout.countChildren());

      for(int b=0; b<tempLayout.countChildren(); b++) {
        proxml.XMLElement tButton = tempLayout.getChild(b); //get the xml for the layout button

        //get name
        String tName = tButton.getAttribute("name");

        //find button in library & copy
        Button retrievedButton = null;
        retrievedButton = (Button) getButtonByName(tName);
        Button tmpButtonObj = new Button(retrievedButton);

        //get xpos, ypos
        tmpButtonObj.setX( tButton.getIntAttribute("xpos"));
        tmpButtonObj.setY( tButton.getIntAttribute("ypos"));

        //put button in layout arraylist
        //put it in an array of arraylists ??? layouts[0][buttonArrayList]
        tempLayoutObj.addButton(tmpButtonObj);
      }

      theLayouts.add(tempLayoutObj);
    }
    currentLayout = (Layout)theLayouts.get(currentLayoutNumber); //get current layout
    menu_button_command("layout_1");
    resetSets();

  }




}

public Button getButtonByName(String name) {
  Button foundButton = null;

  for(int i=0; i<theButtonLibrary.size(); i++) {
    Button tButton = (Button) theButtonLibrary.get(i);
    if (tButton.name.equals(name)){
      foundButton = tButton; 
    } 
  } 

  return foundButton;
}

public void serialEvent(Serial p) { 
  String inString = (myPort.readString()); 
  println("received " + inString);
}


public void mouseReleased(){
  if (!buttonsLocked || library.isDisplayed() ) {

    proxml.XMLElement layoutsXML = new proxml.XMLElement("layouts");

    for(int n=0; n < theLayouts.size(); n++) {
      Layout tmpLayout = (Layout) theLayouts.get(n);

      //create xml for <layout>
      proxml.XMLElement layout = new proxml.XMLElement("layout");

      ArrayList tmpButtons = (ArrayList) tmpLayout.getButtons();
      for(int i=0; i < tmpButtons.size();i++) {
        Button tmpB =(Button) tmpButtons.get(i);
        layout.addChild(tmpB.getXML());
      }

      layoutsXML.addChild(layout);

    }


    xmlLayouts.saveElement(layoutsXML,"xml/layouts.xml");

    resetSets();
  } //end if buttonslocked

  //button library mouse detection
  library.mousePressing = false;

  //screenJoy was it pressed?
  
  currentLayout = (Layout)theLayouts.get(currentLayoutNumber); //get current layout
  if (currentLayout.activeJoy.clicked()) {
    currentLayout.activeJoy.toggleActivity(); 
  } 
  else if ( currentLayout.activeJoy.leftRightClicked() ) {
    currentLayout.activeJoy.toggleLeftRight();
  } else if ( currentLayout.activeJoy.clickConfigClicked() ) {
    //method takes care of itself 
  }
  

}

public void mousePressed() {

  for(int i=0; i<menu_buttons.size();i++) {
    Menu_button tmb = (Menu_button)menu_buttons.get(i);
    if (tmb.contains(mouseX, mouseY)) {
      menu_button_command(tmb.getName());
    } 
  }

}

public void mouseDragged() {
  if (!buttonsLocked) {

    ArrayList layoutButtons = (ArrayList) currentLayout.getButtons();

    for(int i=0; i < layoutButtons.size(); i++ ){
      Button tb = (Button)layoutButtons.get(i);
      if ( tb.contains(mouseX, mouseY)  ) {

        tb.updatePosition();

      } //end if button contains 


    }  //end for loop

  }
}



public void keyPressed() {

  if (key == 'd' || key == 'D') {
    debugOn = !debugOn; //toggle debug on
    println("DEBUG ON = " + debugOn);

  } 
  else if(key == 's' || key== 'S'){
    //changeSerialPort(); 
    menu_button_command("serial");

  } 
  else if (key == 'b' || key == 'B') {
    menu_button_command("button_lock");
  }

  else if (key == 'l' || key == 'l') {
    menu_button_command("button_library");
  }

  else if (key == 'r' || key == 'R') {
    menu_button_command("reset");
  }

  else if (key == '1') {
    currentLayout = (Layout) theLayouts.get(0);
    resetSets();
  }
  else if (key == '2') {
    currentLayout = (Layout) theLayouts.get(1);
    resetSets();
  }
  else if (keyCode == LEFT) {
    if (currentLayoutNumber == 0) {
      currentLayoutNumber = theLayouts.size()-1; 
    } 
    else {
      currentLayoutNumber--;
    }
    currentLayout = (Layout) theLayouts.get(currentLayoutNumber);
    resetSets(); 
  } 
  else if (keyCode == RIGHT) {
    if (currentLayoutNumber == ( theLayouts.size()-1 )) {
      currentLayoutNumber = 0; 
    } 
    else {
      currentLayoutNumber++;
    }
    currentLayout = (Layout) theLayouts.get(currentLayoutNumber);
    resetSets(); 
  }


}



public void resetAllButtons() {

  ArrayList tmpButtons = (ArrayList) currentLayout.getButtons();
  for(int i=0; i < tmpButtons.size(); i++ ){
    Button tb = (Button)tmpButtons.get(i);
    tb.forceReset();
  }

  myPort.write('#');
  myPort.write('#');
}

public String changeSerialPort() {
  myPort.stop();
  println("Stopping serial port " + availablePorts[currentPortNumber]);
  delay(100);

  if ( currentPortNumber == (availablePorts.length-1)) {
    currentPortNumber = 0; 
  } 
  else {
    currentPortNumber++;
  }
  println(" ");
  println("**");
  println("Starting serial port " + availablePorts[currentPortNumber]);
  myPort = new Serial(this, Serial.list()[currentPortNumber], 19200);

  return availablePorts[currentPortNumber];


}






























class ActiveJoystick {
  Ellipse2D container, centerDot; //contains xPos, yPos, width and height

  int dotX, dotY; // the position of the joystick dot
  int dotWidth =  30;

  
  //colors
  int containerStroke = color(255,0,0);
  int dotColor = color(255,0,0);

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
  int leftFill = color(20,255,50); //green ;
  int rightFill = color(0,120,255); //blue;
  
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

  public void display() {

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

  public void displayMouseDot() {
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

  public void sendCommand() {
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


  public boolean activate() {
    this.active = true; 
    return true;
  }

  public boolean deactivate() {
    this.reset(); //sets x and y  to 128, centered position

    this.active = false;
    return true; 
  }

  public boolean  isActive() {
    return this.active;
  }

  public void toggleActivity() {
    this.reset(); //sets x and y  to 128, centered position
    this.active = !this.active;
  }

  public boolean clicked() {
    if (this.labelContainer.contains(mouseX,mouseY)) {
      return true; 
    }
    else {
      return false; 
    }

  }

  public void displayScreenJoyLabel() {
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

  public void displayButtons() {
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

  public void displayLeftRightToggle() {
   
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
  
  
  public boolean leftRightClicked() {
    if (this.modeToggleBtn.contains(mouseX, mouseY)) {
     return true; 
    } else {
     return false; 
    }
    
  }
  
  public void toggleLeftRight() {
    this.reset();
   if (this.mode == 'L') {
     this.mode = 'R';
   }  else {
    this.mode = 'L'; 
   }
  }


  public boolean clickConfigClicked() {
    if (this.clickConfigBtn.contains(mouseX, mouseY)) {
     this.displayClickConfigButtons = !this.displayClickConfigButtons;
     return true;
    } else {
     return false; 
    }
      
    
  }
  

  
  public void reset() {

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












class Button {

  Rectangle theRect;
  int theColor;
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

  public void display() {


    image(this.buttonImage, this.theRect.x, this.theRect.y, (float) this.theRect.getWidth(), (float)this.theRect.getHeight()-this.buttonPadding);

    if ( (this.highlight) || (this.setHighlight) ||   (!this.isAnalog && this.digitalMode == 2 ) ) {
      textFont(font);

      if ( this.digitalMode == 1) {
        //turn button label red on click, otherwise 
        int tmpTextColor = (this.active) ? color(255,0,0) : color(0,0,0);
        fill(tmpTextColor);
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

  public boolean contains( int _x, int _y) {

    return (boolean)this.theRect.contains(_x, _y); 

  }

  public void setX(int X) {
    this.theRect.x = X; 
  }

  public void setY(int Y) {

    this.theRect.y = Y; 
  }

  public int getWidth() {
    return (int)this.theRect.getWidth(); 
  }

  public int getHeight() {
    return (int)this.theRect.getHeight();

  }

  public void resize(int factor) {

    int newWidth =((int)this.theRect.getWidth())/factor;
    int newHeight =((int)this.theRect.getHeight())/factor;
    this.theRect.setSize(newWidth,newHeight);

  }


  public void highlight(boolean _state) {
    this.highlight = _state;
  }

  public void setHighlight(boolean _state) {
    this.setHighlight = _state; 
  }

  public void updatePosition() {    
    //get offsets
    int offsetX = pmouseX - this.theRect.x;
    int offsetY = pmouseY - this.theRect.y;

    int newX = mouseX - offsetX;
    int newY = mouseY - offsetY;

    if ( (newX > 0) && (newX<width-theRect.width) && (newY>0) && (newY< height-theRect.height-30)) {
      this.theRect.setLocation(newX, newY); 
    }
  }

  public int getAnalogX() {
    int tX = mouseX - this.theRect.x;
    int tVal = (int)map(tX, 0, this.theRect.width, 0,255);
    this.analogX = tVal;
    return this.analogX;
  }

  public int getAnalogY() {
    int tY =  mouseY - this.theRect.y;
    int tVal = (int)map(tY,0, this.theRect.height, 0,255);
    this.analogY = tVal;
    return this.analogY;

  }

  public void toggleHoldDigital() {

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

  public void toggleHoldAnalog() {

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

  public void resetDigitalMode() {
    if (!this.isAnalog && this.digitalMode == 2) {
      this.digitalMode = 1;
    }    
  }

  public void resetAnalogMode() {
    if (this.isAnalog && this.analogMode == 2) {
      this.analogMode = 1;
    } 

  }

  public void sendCommand() {

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


  public void resetCommand() {

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

  public void forceReset() {
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
  public proxml.XMLElement getXML() {

    proxml.XMLElement tempObj =  new proxml.XMLElement("button");
    tempObj.addAttribute("name",this.name);
    tempObj.addAttribute("xpos",this.theRect.x);
    tempObj.addAttribute("ypos",this.theRect.y);

    return tempObj;

  }

}




//global functions

public void displayButtonLibrary() {

}
























class ButtonLibrary {

  ArrayList buttons;
  int maxHeight = 0;
  boolean isActive = false;
  Button deleteButton;

  boolean mousePressing = false;  // prevents multiple new button instances in layout when clicking on button in library
  boolean deleteMode = false;

  ButtonLibrary() {

    this.buttons = new ArrayList();

    int currX = 0;
    int yPos = 10;

    for(int i = 0; i < theButtonLibrary.size(); i++ ) {
      Button tb = (Button) theButtonLibrary.get(i);
      Button tmpButtonObj = new Button(tb);

      tmpButtonObj.setX((width/2)-50);
      tmpButtonObj.setY((height/2)-50);

      tmpButtonObj.setX((currX));
      tmpButtonObj.setY(yPos);
      tmpButtonObj.resize(2);

      // x position for next button with 5px spacing
      currX += tmpButtonObj.getWidth() + 5;

      if (tmpButtonObj.getHeight() > this.maxHeight) {
        this.maxHeight = tmpButtonObj.getHeight(); 
      }

      this.buttons.add(tmpButtonObj);
    }

    // add the DELETE X button
    deleteButton = new Button("Delete","button_delete.png","",(int)currX,(int) yPos,"false");
    deleteButton.resize(2);
  }

  public void display() {

    //white rectangle 
    fill(255);
    noStroke();
    rect(0,0,width, this.maxHeight+20);

    for(int i = 0; i < this.buttons.size(); i++ ) {
      Button tb = (Button) this.buttons.get(i);
      if ( tb.contains(mouseX, mouseY) ) {
        tb.highlight(true);

        //add a new button to the current layout
        if (mousePressed && !this.mousePressing) {
          Button retrievedButton = null;
          retrievedButton = (Button) getButtonByName(tb.name);
          Button tmpButtonObj = new Button(retrievedButton);
          tmpButtonObj.setX((width/2)-50);
          tmpButtonObj.setY((height/2)-50);
          currentLayout.addButton(tmpButtonObj);

          this.mousePressing = true; //prevent multiple buttons to be instantiated.
        }

      } //end if contains 
      else {
        tb.highlight(false);
      }

      tb.display(); 
    }

    //display the delete button
    deleteButton.display();
    if (  deleteButton.contains(mouseX, mouseY) ) {
      deleteButton.highlight(true);
      if (mousePressed) {
        cursor(CROSS);
        // cursor(deleteButton.buttonImage);
        this.deleteMode = true;
      } 

    } 
    else {
      deleteButton.highlight(false); 
    }

    //draw line under button library
    stroke(200);
    line(0, this.maxHeight+20, width, this.maxHeight+20);

    //it is active
    this.isActive = true;
  }

  public void toggleDisplay() {
    this.isActive = !this.isActive; 
    Menu_button btnLock = (Menu_button)getMenuButtonByName("button_lock");
    toggleButtonLock();
  }

  public boolean isDisplayed() {
    return this.isActive;
  }

  //turn off deleteMode and reset the cursor to Pointer
  public void deleteModeOff() {
    this.deleteMode = false;
    cursor(ARROW); 
  }

}












class Layout {
  int id;
  Rectangle boundary;

  ArrayList buttons;
  boolean active = false;

  //sets
  ArrayList theSets;
  boolean initialSets = false;


  
  int theJoySize = 450;
  ActiveJoystick activeJoy;

  Layout() {
    boundary = new Rectangle(0,0, width, height-30);

    this.buttons = new ArrayList();
    
    activeJoy  = new ActiveJoystick(width/2-(this.theJoySize/2), height/3-(this.theJoySize/2),this.theJoySize, this.theJoySize);
  }

  public void display() {


    for(int i=0; i < this.buttons.size(); i++) {
      Button tb = (Button) this.buttons.get(i);
      tb.display(); 
      
      boolean overCurrButtonNotUnderActiveJoy = (tb.contains(mouseX, mouseY)  &&  !activeJoy.container.contains(mouseX, mouseY) && activeJoy.isActive() );
      boolean overCurrButtonActiveJoyOff = ( tb.contains(mouseX, mouseY) && !activeJoy.isActive() );
      if ( overCurrButtonNotUnderActiveJoy  || overCurrButtonActiveJoyOff  ) {

        tb.highlight(true);

        if (mousePressed == true && buttonsLocked) {
          if (tb.isAnalog && (mouseEvent.getClickCount()==2) ) {
            //analog doubleclicked
            tb.toggleHoldAnalog();
          } 
          else if (mouseEvent.getClickCount() == 2) {
            //digital doubleclicked
            tb.toggleHoldDigital(); 
          }

          tb.sendCommand();
        } 
        else {
          if (tb.digitalMode == 1) {
            tb.resetCommand(); 
          }
        }


        if (library.deleteMode == true && mousePressed) {
          this.removeButton(i);
          library.deleteModeOff();
        }


      } //end if contains 
      else {
        if (!tb.isAnalog && tb.digitalMode==1) {
          tb.highlight(false);
          tb.resetCommand(); 
        }
      }


      tb.display(); 

      if (lastButtonPressed != null && tb.isAnalog && !lastButtonPressed.isAnalog ) {
        //reset analogMode 2 to 1
        tb.resetAnalogMode();
        prevButtonPressed = lastButtonPressed;
      }
      
    }//end for loop to display layout buttons

    // screenJoy.display();
    activeJoy.display();
    
    this.active = true;

  }

  public void hide() {
    this.active = false;
    background(255);
  }

  public void saveLayout() {

  }

  public void removeLayout() {

  } 

  public ArrayList getButtons() {
    return this.buttons;  
  }


  public void addButton(Button buttonObj) {   
    this.buttons.add(buttonObj);
  }

  public void removeButton(int index) {
    this.buttons.remove(index);

  }


}







class Menu_button {

  Rectangle buttonArea;  //holds xpos,ypos, width and height
  String name, label,active_label = null;
  boolean toggle;

  int theColor = color(230);
  int activeColor = color(190);
  int fontColor = color(0);
  int activeFontColor = color(255,0,0);

  boolean highlight, active = false;
  boolean display = true;
  float lastPress = 0; //keeps millis of time button was pressed

  boolean blink = false;
  float blinkTime = 25; //how long to display

  Menu_button(String _name, String _label, String _type, int _xpos, int _ypos, int _width, int _height, int _color, int _activeColor, int _fontColor, int _activeFontColor) {

    this.buttonArea = new Rectangle(_xpos, _ypos, _width, _height);
    this.name = _name;
    this.label = _label;
    this.theColor = _color;
    this.activeColor = _activeColor;
    this.fontColor = _fontColor;
    this.activeFontColor = _activeFontColor;

    if (_type == "toggle") {
      this.toggle = true;
    } 
    else {
      this.toggle = false; 
    }

  }

  public void display() {

    if (this.display) {
      noStroke();
      //fill color
      if ( this.buttonArea.contains(mouseX, mouseY) || this.isBlinking() ) {
        this.highlight = true;
      } 
      else {
        this.highlight = false; 
      }

      fill(this.getDisplayColor()); 

      rect((int)this.buttonArea.getX(),(int) this.buttonArea.getY(), (int)this.buttonArea.getWidth(),(int) this.buttonArea.getHeight());

      //active
      fill(this.getFontColor());
      textFont(font);

      String tmpLabel = "ok go";
      if (this.isActive() && this.active_label != null) {
        tmpLabel = this.active_label;
      } 
      else {
        tmpLabel = this.label;
      }


      text(tmpLabel, (int)this.buttonArea.getX()+15, (int)this.buttonArea.getY()+((int)this.buttonArea.getHeight()/2)+5);
      noFill();
    } 

  }

  public void setActiveLabel(String txt) {
    this.active_label = txt; 
  }

  public boolean contains(int _x, int _y) {

    return this.buttonArea.contains(_x, _y);

  }

  public void activate() {
    this.active = true; 
  }

  public void deactivate() {
    this.active = false; 
  }

  public void toggle() {
    if (this.toggle) {
      this.active = !this.active;
    }
  }

  public boolean isActive() {
    return this.active; 
  }

  public void setHighlight(boolean _value) {
    this.highlight = _value; 
  }

  public int getDisplayColor() {
    if (this.highlight || this.active) {
      return this.activeColor;
    } 
    else {
      return this.theColor; 
    }
  }

  public int getFontColor() {
    if (this.highlight || this.active) {
      return this.activeFontColor;
    } 
    else {
      return this.fontColor; 
    }
  }

  public String getName() {
    return this.name; 
  }

  public void setLabel(String _text) {
    this.label = _text;
  }

  public void blink() {
    this.blink = true;    
    this.lastPress = millis();
  }

  public boolean isBlinking() {

    if (this.blink && (millis() - this.lastPress < blinkTime) ) {
      return true;
    } 
    else {
      return false; 
    }

  }

}


// GLOBAL FUNCTIONS


public void menu_button_command(String buttonName) {
  Menu_button button = (Menu_button)getMenuButtonByName(buttonName);

  if (buttonName == "button_lock") {
    toggleButtonLock();

  } 
  else if (buttonName == "button_library") {
    button.toggle();
    library.toggleDisplay();

    currentLayout.activeJoy.toggleActivity();

  }

  else if (buttonName == "reset") {
    button.blink();
    resetAllButtons();
    resetSets();
  }
  else if (buttonName == "serial") {
    button.blink();
    String newPortName = changeSerialPort(); 
    String newLabel = "(S)erial port - " + newPortName;
    button.setLabel(newLabel);

  }   
  else if (buttonName == "layout_1") {
    resetLayoutButtons();
    button.toggle();
    currentLayoutNumber = 0;
    currentLayout = (Layout) theLayouts.get(currentLayoutNumber);
    resetSets();

  } 
  else if (buttonName == "layout_2") {
    resetLayoutButtons();
    button.toggle();
    currentLayoutNumber = 1;
    currentLayout = (Layout) theLayouts.get(currentLayoutNumber);
    resetSets();
  }   
  else if (buttonName == "layout_3") {
    resetLayoutButtons();
    button.toggle();
    currentLayoutNumber = 2;
    currentLayout = (Layout) theLayouts.get(currentLayoutNumber);
    resetSets();
  } 
  else if (buttonName == "layout_4") {
    resetLayoutButtons();
    button.toggle();
    currentLayoutNumber = 3;
    currentLayout = (Layout) theLayouts.get(currentLayoutNumber);
    resetSets();
  }
  else if (buttonName == "layout_5") {
    resetLayoutButtons();
    button.toggle();
    currentLayoutNumber = 4;
    currentLayout = (Layout) theLayouts.get(currentLayoutNumber);
    resetSets();

  }  
}


public Menu_button getMenuButtonByName(String _name) {
  for(int i=0; i<menu_buttons.size();i++) {
    Menu_button temp = (Menu_button)menu_buttons.get(i);
    if (temp.name == _name) {
      return temp; 
    }

  } 



  return null;
}


public void resetLayoutButtons() {
  for(int i=0; i<layoutMenu_buttons.size();i++) {
    Menu_button tb = (Menu_button) layoutMenu_buttons.get(i);
    tb.deactivate(); 
  }
}


public void toggleButtonLock() {
  Menu_button btnLock = (Menu_button)getMenuButtonByName("button_lock");
  btnLock.toggle();
  if (btnLock.isActive()) {
    buttonsLocked =false; //set global buttonLock variable
    cursor(HAND);
  } 
  else {
    buttonsLocked =true; //set global buttonLock variable
    cursor(ARROW);

  }

}














class Set {

  Rectangle fence;  //holds xpos,ypos, width and height
  int theColor = color(230);
  boolean highlight = false;
  ArrayList buttons = new ArrayList();

  Set(Button b1, Button b2) {

    buttons.add(b1);
    buttons.add(b2);
    this.fence = new Rectangle(b1.theRect);
    fence.add(b2.theRect);
    fence.grow(10,10);
  }

  public void display() {

    stroke(42,160,233);
    noFill();
    if (this.highlight) {
      strokeWeight(5); 
    } 
    else {
      stroke(this.theColor);
      strokeWeight(1);
    }
    rect(this.fence.x, this.fence.y, this.fence.width, this.fence.height);
  
    noStroke();
  }

  public void highlight(boolean val) {
    //the set's highlight
    this.highlight = val; 
    
    //set the display for all the set's buttons
    if ( !buttons.isEmpty() ) {
      for(int i=0; i<buttons.size(); i++) {
        Button tb = (Button)buttons.get(i);
        tb.setHighlight(val);
      }
    }

  }
  
  public void sendCommands() {
     if ( !buttons.isEmpty() ) {
      for(int i=0; i<buttons.size(); i++) {
        Button tb = (Button)buttons.get(i);
        tb.sendCommand();
      }
    }
    
  }

  public void addButton(Button b) {
    this.buttons.add(b);
    this.fence.add(b.theRect);

  }

  public boolean overButtons(int _mx, int _my) {
    boolean isOver;

    if ( !buttons.isEmpty() ) {
      for(int i=0; i<buttons.size(); i++) {
        Button tb = (Button)buttons.get(i);
        if ( tb.contains(_mx, _my)) {
          return true;
        } 
        else {
          //continue
        } 
      }
      return false;
    } 
    else {
      return false;
    } 
  }

  public int getX() {
    return this.fence.x; 
  }

  public int getY() {
    return this.fence.y; 
  }

  public void setWidth(int _width) {
    this.fence.width = _width; 
  }

  public void setHeight(int _height) {
    this.fence.height = _height; 
  }

}


// GLOBAL FUNCTIONS

public Set buttonIsInSet(Button button) {
  if ( theSets.isEmpty() ) {
    return null;
  } 
  else {
    for(int s=0; s<theSets.size();s++) {
      Set temp = (Set) theSets.get(s);
      if (temp.fence.contains(button.theRect)) {
        return temp; 
      }
    } 
    return null;
  }

}


//global set functions
public void renderSets() {
 for(int s=0; s<theSets.size();s++) {
    Set ts = (Set)theSets.get(s);

    if (ts.fence.contains(mouseX, mouseY) && !ts.overButtons(mouseX, mouseY)) {
      ts.highlight(true);

      if (mousePressed == true) {
        ts.sendCommands(); 
      }
    } 
    else {
      ts.highlight(false);

    } 
    ts.display();
  } 
}

public void resetSets() {
  //create sets
  theSets.clear();

   ArrayList tmpButtons = (ArrayList) currentLayout.getButtons();
  //check all neighbors
  for(int n=0; n<tmpButtons.size(); n++) {

    Button tb = (Button)tmpButtons.get(n);

    for(int i = 0; i<tmpButtons.size(); i++) {

      Button neighborRect =(Button) tmpButtons.get(i);

      if ( tb.theRect.intersects(neighborRect.theRect) && (n!=i) ) {
        //does the neighbor button have a Set already?
        Set neighborRectSet = buttonIsInSet(neighborRect);
        if ( neighborRectSet != null) {
          neighborRectSet.addButton(tb);
        } 
        else {
          Set tempSet = new Set(tb, neighborRect);
          tempSet.display();
          theSets.add(tempSet);
        }
      }

    } 
  }

}


  static public void main(String args[]) {
    PApplet.main(new String[] { "--bgcolor=#c0c0c0", "ps3Interface" });
  }
}
