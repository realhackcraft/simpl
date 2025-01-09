package net.borui.simpl.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/** SaveButtonListener */
public class SaveButtonListener implements ActionListener {
  @Override
  public void actionPerformed(ActionEvent e) {
    GUI.getInstance().saveButtonClicked();
  }
}
