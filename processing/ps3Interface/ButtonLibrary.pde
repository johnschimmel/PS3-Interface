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

  void display() {

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

  void toggleDisplay() {
    this.isActive = !this.isActive; 
    Menu_button btnLock = (Menu_button)getMenuButtonByName("button_lock");
    toggleButtonLock();
  }

  boolean isDisplayed() {
    return this.isActive;
  }

  //turn off deleteMode and reset the cursor to Pointer
  void deleteModeOff() {
    this.deleteMode = false;
    cursor(ARROW); 
  }

}












