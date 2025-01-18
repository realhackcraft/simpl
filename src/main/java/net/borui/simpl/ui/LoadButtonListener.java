package net.borui.simpl.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LoadButtonListener implements ActionListener {
  @Override
  public void actionPerformed(ActionEvent e) {
    GUI.getInstance().loadButtonClicked();
  }
}
