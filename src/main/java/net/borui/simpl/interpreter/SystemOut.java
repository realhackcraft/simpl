package net.borui.simpl.interpreter;

/**
 * A Singleton class to display the output of a simpl program to the system output stream.
 */
public class SystemOut implements ProgramOutput {

  /**
   * Print to the output.
   *
   * @param a the String to print.
   */
  @Override
  public void println(String a) {
    System.out.println(a);
  }

  /**
   * The instance of the output.
   */
  private static SystemOut instance;

  /**
   * Get the instance of the program output.
   *
   * @return the instance of the program output.
   */
  @Override
  public ProgramOutput getInstance() {
    if (instance == null) instance = new SystemOut();
    return instance;
  }
}
