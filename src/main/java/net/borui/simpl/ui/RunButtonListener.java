package net.borui.simpl.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/** RunButtonListener */
public class RunButtonListener implements ActionListener {
  @Override
  public void actionPerformed(ActionEvent e) {
    // run button clicked
    GUI.getInstance().runButtonClicked();
  }
}
