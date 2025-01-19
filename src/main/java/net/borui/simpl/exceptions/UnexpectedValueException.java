package net.borui.simpl.exceptions;

import net.borui.simpl.constructs.Variable;

/**
 * A variable is not the correct type.
 */
@SuppressWarnings("CallToPrintStackTrace")
public class UnexpectedValueException extends Exception {
  /**
   * the value of the incorrectly typed variable.
   */
  private final Variable result;
  /**
   * The expected type for the variable.
   */
  private final String expected;
  /**
   * The actual type for the variable.
   */
  private final String actual;

  /**
   * Creates a new UnexpectedValueException with the expected an actual types as well as the value of the variable.
   *
   * @param result   the content of the variable.
   * @param expected the expected type for the variable.
   * @param actual   the actual type for the variable.
   */
  public UnexpectedValueException(Variable result, String expected, String actual) {
    this.result = result;
    this.expected = expected;
    this.actual = actual;
  }

  @Override
  public void printStackTrace() {
    if (this.expected != null && this.actual != null && this.result != null) {
      System.err.println("The value: " + this.result.display());
      System.err.println("is expected to be type: " + this.expected);
      System.err.println("but is actual: " + this.actual);
    }
    super.printStackTrace();
  }
}
