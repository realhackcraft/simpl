package net.borui.simpl.exceptions;

/**
 * VariableNotFound
 */
@SuppressWarnings("CallToPrintStackTrace")
public class VariableNotFound extends Exception {
  private final String variableName;

  public VariableNotFound(String variableName) {
    this.variableName = variableName;
  }

  @Override
  public void printStackTrace() {
    System.err.println("The variable named: " + this.variableName + " cannot be found.");
    super.printStackTrace();
  }
}
