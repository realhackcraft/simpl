package net.borui.simpl.ui;

import net.borui.simpl.interpreter.ProgramOutput;

import javax.swing.*;

/**
 * A Singleton class to display the output of a simpl program to the GUI instance.
 */
public class GUIOut implements ProgramOutput {

  /**
   * Print to the output.
   *
   * @param a the String to print.
   */
  @Override
  public void println(String a) {
    JTextArea out = GUI.getInstance().output;
    out.setText(out.getText() + a + "\n");
  }

  /**
   * The instance of the output.
   */
  private static GUIOut instance;

  /**
   * Get the instance of the program output.
   *
   * @return the instance of the program output.
   */
  @Override
  public ProgramOutput getInstance() {
    if (instance == null) instance = new GUIOut();
    return instance;
  }
}
