package net.borui.simpl.constructs;

/**
 * A boolean variable in simpl.
 *
 * @param value the value of the boolean.
 */
public record VBoolean(boolean value) implements Variable {
  /**
   * Gives a String representation of the boolean as should be outputted to the user when printing the variable.
   *
   * @return a String representation of the boolean meant for displaying to the user.
   */
  @Override
  public String display() {
    return value ? "true" : "false";
  }

  /**
   * Gets the methods present in this variable type.
   *
   * @param identifier the name of the method.
   * @return the method function requested.
   */
  @Override
  public VFunction getMethod(String identifier) {
    return null;
  }

  /**
   * Gives a String representation of the boolean.
   * Should only be used when debugging.
   *
   * @return a String representation of the boolean specifying the data type.
   * @see #display()
   */
  @Override
  public String toString() {
    return "VBoolean{" + value + "}";
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof VBoolean(boolean value1))) return false;

    return value == value1;
  }
}
