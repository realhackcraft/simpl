package net.borui.simpl.ui;

import net.borui.simpl.interpreter.ProgramOutput;

import javax.swing.*;

public class GUIOut extends ProgramOutput {
  @Override
  public void println(String a) {
    JTextArea out = GUI.getInstance().output;
    out.setText(out.getText() + a + "\n");
  }

  private static GUIOut instance;

  @Override
  public ProgramOutput getInstance() {
    if (instance == null) instance = new GUIOut();
    return instance;
  }
}
