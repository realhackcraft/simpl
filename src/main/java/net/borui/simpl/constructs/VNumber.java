package net.borui.simpl.constructs;

/**
 * A number in simpl.
 *
 * @param value the value of the number.
 */
public record VNumber(double value) implements Variable {

  /**
   * Gives a String representation of the number.
   * Should only be used when debugging.
   *
   * @return a String representation of the number specifying the data type.
   * @see #display()
   */
  @Override
  public String toString() {
    return "VNumber{" + value + "}";
  }

  /**
   * Gives a String representation of the number as should be outputted to the user when printing the variable.
   *
   * @return a String representation of the number meant for displaying to the user.
   */
  @Override
  public String display() {
    return value + "";
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
    if (!(o instanceof VNumber(double value1))) return false;

    return Double.compare(value, value1) == 0;
  }
}
