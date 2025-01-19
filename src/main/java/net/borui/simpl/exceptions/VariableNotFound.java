package net.borui.simpl.exceptions;

/**
 * A variable cannot be found.
 */
@SuppressWarnings("CallToPrintStackTrace")
public class VariableNotFound extends Exception {
  /**
   * The name of the variable which cannot be found.
   */
  private final String variableName;

  /**
   * create a new VariableNotFound exception based on the variable name.
   *
   * @param variableName the variable which cannot be found.
   */
  public VariableNotFound(String variableName) {
    this.variableName = variableName;
  }

  @Override
  public void printStackTrace() {
    System.err.println("The variable named: " + this.variableName + " cannot be found.");
    super.printStackTrace();
  }
}
