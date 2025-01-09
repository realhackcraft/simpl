package net.borui.simpl.exceptions;

public class InvalidVariableException extends Exception {
  public final String variable;

  public InvalidVariableException(String variable) {
    this.variable = variable;
  }
}
