package net.borui.simpl.exceptions;

public class UnexpectedNodeTypeException extends Exception {
  private String expected;
  private String actual;

  public UnexpectedNodeTypeException(String expected, String actual) {
    this.expected = expected;
    this.actual = actual;
  }

  @Override
  public void printStackTrace() {
    if (this.expected != null && this.actual != null) {
      System.err.println("Expected: " + this.expected);
      System.err.println("Actual: " + this.actual);
    }
    super.printStackTrace();
  }
}
