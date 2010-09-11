class Set {

  Rectangle fence;  //holds xpos,ypos, width and height
  color theColor = color(230);
  boolean highlight = false;
  ArrayList buttons = new ArrayList();

  Set(Button b1, Button b2) {

    buttons.add(b1);
    buttons.add(b2);
    this.fence = new Rectangle(b1.theRect);
    fence.add(b2.theRect);
    fence.grow(10,10);
  }

  void display() {

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

  void highlight(boolean val) {
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
  
  void sendCommands() {
     if ( !buttons.isEmpty() ) {
      for(int i=0; i<buttons.size(); i++) {
        Button tb = (Button)buttons.get(i);
        tb.sendCommand();
      }
    }
    
  }

  void addButton(Button b) {
    this.buttons.add(b);
    this.fence.add(b.theRect);

  }

  boolean overButtons(int _mx, int _my) {
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

  int getX() {
    return this.fence.x; 
  }

  int getY() {
    return this.fence.y; 
  }

  void setWidth(int _width) {
    this.fence.width = _width; 
  }

  void setHeight(int _height) {
    this.fence.height = _height; 
  }

}


// GLOBAL FUNCTIONS

Set buttonIsInSet(Button button) {
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
void renderSets() {
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

void resetSets() {
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

