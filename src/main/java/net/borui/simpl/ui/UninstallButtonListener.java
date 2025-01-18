package net.borui.simpl.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class UninstallButtonListener implements ActionListener {
  @Override
  public void actionPerformed(ActionEvent e) {
    GUI.getInstance().uninstallButtonClicked();
  }
}
