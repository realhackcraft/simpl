package net.borui.simpl.exceptions;

import net.borui.simpl.constructs.Variable;

/** UnexpectedValueException */
public class UnexpectedValueException extends Exception {
  private Variable result;
  private String expected;
  private String actual;

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
