import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;

import processing.serial.*;
import proxml.*;

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

void setup() {
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


void draw() {
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





void loadControllerInfo() {

  //load ellipses from file if it exists
  xmlButtons = new XMLInOut(this);
  try{
    xmlButtons.loadElement("xml/buttons.xml");

  }
  catch(Exception e){
    println("file not found");
  }
}

void loadLayouts() {

  //load layouts
  xmlLayouts = new XMLInOut(this);
  try{
    xmlLayouts.loadElement("xml/layouts.xml"); 
  }
  catch(Exception e){
    println("file not found");
  }


}

void xmlEvent(proxml.XMLElement element){
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

Button getButtonByName(String name) {
  Button foundButton = null;

  for(int i=0; i<theButtonLibrary.size(); i++) {
    Button tButton = (Button) theButtonLibrary.get(i);
    if (tButton.name.equals(name)){
      foundButton = tButton; 
    } 
  } 

  return foundButton;
}

void serialEvent(Serial p) { 
  String inString = (myPort.readString()); 
  println("received " + inString);
}


void mouseReleased(){
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

void mousePressed() {

  for(int i=0; i<menu_buttons.size();i++) {
    Menu_button tmb = (Menu_button)menu_buttons.get(i);
    if (tmb.contains(mouseX, mouseY)) {
      menu_button_command(tmb.getName());
    } 
  }

}

void mouseDragged() {
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



void keyPressed() {

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



void resetAllButtons() {

  ArrayList tmpButtons = (ArrayList) currentLayout.getButtons();
  for(int i=0; i < tmpButtons.size(); i++ ){
    Button tb = (Button)tmpButtons.get(i);
    tb.forceReset();
  }

  myPort.write('#');
  myPort.write('#');
}

String changeSerialPort() {
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






























