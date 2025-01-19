package net.borui.simpl.exceptions;

/**
 * The type of a tree-sitter is unexpected.
 */
@SuppressWarnings("CallToPrintStackTrace")
public class UnexpectedNodeTypeException extends Exception {
  /**
   * The expected type of the tree-sitter node.
   */
  private final String expected;
  /**
   * The actual type of the tree-sitter node.
   */
  private final String actual;

  /**
   * Creates a new UnexpectedNodeTypeException with the expected an actual types.
   *
   * @param expected the expected return type of the tree-sitter node.
   * @param actual   the actual return type of the tree-sitter node.
   */
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
