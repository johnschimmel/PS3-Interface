class Menu_button {

  Rectangle buttonArea;  //holds xpos,ypos, width and height
  String name, label,active_label = null;
  boolean toggle;

  color theColor = color(230);
  color activeColor = color(190);
  color fontColor = color(0);
  color activeFontColor = color(255,0,0);

  boolean highlight, active = false;
  boolean display = true;
  float lastPress = 0; //keeps millis of time button was pressed

  boolean blink = false;
  float blinkTime = 25; //how long to display

  Menu_button(String _name, String _label, String _type, int _xpos, int _ypos, int _width, int _height, color _color, color _activeColor, color _fontColor, color _activeFontColor) {

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

  void display() {

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

  void setActiveLabel(String txt) {
    this.active_label = txt; 
  }

  boolean contains(int _x, int _y) {

    return this.buttonArea.contains(_x, _y);

  }

  void activate() {
    this.active = true; 
  }

  void deactivate() {
    this.active = false; 
  }

  void toggle() {
    if (this.toggle) {
      this.active = !this.active;
    }
  }

  boolean isActive() {
    return this.active; 
  }

  void setHighlight(boolean _value) {
    this.highlight = _value; 
  }

  color getDisplayColor() {
    if (this.highlight || this.active) {
      return this.activeColor;
    } 
    else {
      return this.theColor; 
    }
  }

  color getFontColor() {
    if (this.highlight || this.active) {
      return this.activeFontColor;
    } 
    else {
      return this.fontColor; 
    }
  }

  String getName() {
    return this.name; 
  }

  void setLabel(String _text) {
    this.label = _text;
  }

  void blink() {
    this.blink = true;    
    this.lastPress = millis();
  }

  boolean isBlinking() {

    if (this.blink && (millis() - this.lastPress < blinkTime) ) {
      return true;
    } 
    else {
      return false; 
    }

  }

}


// GLOBAL FUNCTIONS


void menu_button_command(String buttonName) {
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


Menu_button getMenuButtonByName(String _name) {
  for(int i=0; i<menu_buttons.size();i++) {
    Menu_button temp = (Menu_button)menu_buttons.get(i);
    if (temp.name == _name) {
      return temp; 
    }

  } 



  return null;
}


void resetLayoutButtons() {
  for(int i=0; i<layoutMenu_buttons.size();i++) {
    Menu_button tb = (Menu_button) layoutMenu_buttons.get(i);
    tb.deactivate(); 
  }
}


void toggleButtonLock() {
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














