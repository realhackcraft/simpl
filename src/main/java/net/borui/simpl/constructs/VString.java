package net.borui.simpl.constructs;

/**
 * A string variable in simpl.
 *
 * @param value the value of the string.
 */
public record VString(String value) implements Variable {

  /**
   * Gives a String representation of the string.
   * Should only be used when debugging.
   *
   * @return the string which this variable represents with the data type.
   * @see #display()
   */
  @Override
  public String toString() {
    return "VString{" + value + "}";
  }

  /**
   * Gives the string with format as should be outputted to the user when printing the variable.
   *
   * @return the string with format meant for displaying to the user.
   */
  @Override
  public String display() {
    return value;
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

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof VString(String value1))) return false;

    return value.equals(value1);
  }
}
