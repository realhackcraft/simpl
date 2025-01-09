package net.borui.simpl.exceptions;

import net.borui.simpl.constructs.Variable;

public class IncorrectReturnTypeException extends Exception {
  public final Class<? extends Variable> expected;
  public final Class<? extends Variable> actual;
  public Variable variable;

  public IncorrectReturnTypeException(
      Class<? extends Variable> expected, Class<? extends Variable> actual) {
    this.expected = expected;
    this.actual = actual;
  }

  public IncorrectReturnTypeException(
      Class<? extends Variable> expected, Class<? extends Variable> actual, Variable variable) {
    this.expected = expected;
    this.actual = actual;
    this.variable = variable;
  }

  @Override
  public void printStackTrace() {
    if (this.variable != null)
      System.err.println("Value of returned variable: " + this.variable);
    super.printStackTrace();
  }
}
