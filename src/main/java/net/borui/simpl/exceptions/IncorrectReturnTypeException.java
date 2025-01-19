package net.borui.simpl.exceptions;

import net.borui.simpl.constructs.Variable;
import net.borui.simpl.interpreter.Interpreter;

/**
 * A return type of a function is incorrect.
 */
@SuppressWarnings("CallToPrintStackTrace")
public class IncorrectReturnTypeException extends Exception {
  /**
   * The expected type of the return value.
   */
  public final Class<? extends Variable> expected;
  /**
   * The actual type of the return value.
   */
  public final Class<? extends Variable> actual;

  /**
   * Creates a new IncorrectReturnTypeException with the expected an actual types.
   *
   * @param expected the expected return type of a function.
   * @param actual   the actual return type of a function.
   */
  public IncorrectReturnTypeException(Class<? extends Variable> expected, Class<? extends Variable> actual) {
    this.expected = expected;
    this.actual = actual;
  }

  @Override
  public void printStackTrace() {
    System.err.println("Expected return type: " + Interpreter.reverseTypeMap.get(expected) + ", but got: " + Interpreter.reverseTypeMap.get(actual));
    super.printStackTrace();
  }
}
