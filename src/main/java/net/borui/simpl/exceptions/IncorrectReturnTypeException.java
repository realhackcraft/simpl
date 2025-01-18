package net.borui.simpl.exceptions;

import net.borui.simpl.constructs.Variable;
import net.borui.simpl.interpreter.Interpreter;

@SuppressWarnings("CallToPrintStackTrace")
public class IncorrectReturnTypeException extends Exception {
  public final Class<? extends Variable> expected;
  public final Class<? extends Variable> actual;

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
