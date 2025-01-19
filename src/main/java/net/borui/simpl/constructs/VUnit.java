package net.borui.simpl.constructs;

/**
 * A variable type in simpl representing a lack of value.
 */
public class VUnit implements Variable {
  /**
   * Gives a String representation of unit.
   * Should only be used when debugging.
   *
   * @return a String representation of unit specifying the data type.
   * @see #display()
   */
  @Override
  public String toString() {
    return "VUnit{}";
  }

  /**
   * Gives a String representation of unit as should be outputted to the user when printing the variable.
   *
   * @return a String representation of unit meant for displaying to the user.
   */
  @Override
  public String display() {
    return "";
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
  public boolean equals(Object obj) {
    return obj instanceof VUnit;
  }
}
