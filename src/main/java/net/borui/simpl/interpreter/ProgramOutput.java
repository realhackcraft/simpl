package net.borui.simpl.interpreter;

/**
 * An interface for Singleton classes displaying the output of a simpl program.
 */
public interface ProgramOutput {
  /**
   * Print to the output.
   *
   * @param a the String to print.
   */
  void println(String a);

  /**
   * Get the instance of the program output.
   *
   * @return the instance of the program output.
   */
  ProgramOutput getInstance();
}
