package net.borui.simpl.exceptions;

/** VariableNotFound */
public class VariableNotFound extends Exception {
  private String variableName;

  public VariableNotFound(String variableName) {
    this.variableName = variableName;
  }

  @Override
  public void printStackTrace() {
    System.err.println("The variable named: " + this.variableName + " cannot be found.");
    super.printStackTrace();
    System.exit(1);
  }
}
