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

  void display() {


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

  void hide() {
    this.active = false;
    background(255);
  }

  void saveLayout() {

  }

  void removeLayout() {

  } 

  ArrayList getButtons() {
    return this.buttons;  
  }


  void addButton(Button buttonObj) {   
    this.buttons.add(buttonObj);
  }

  void removeButton(int index) {
    this.buttons.remove(index);

  }


}







