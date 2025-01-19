package net.borui.simpl.constructs;

/**
 * Variables in the simpl language.
 */
public interface Variable {
  /**
   * Converts the content of this variable into a displayable representation, whether that is in a GUI or in the output stream.
   *
   * @return a string representation of the variable to be used when printing to the screen.
   */
  String display();

  /**
   * Gets the methods present in this variable type.
   *
   * @param identifier the name of the method.
   * @return the method function requested.
   */
  VFunction getMethod(String identifier);
}
