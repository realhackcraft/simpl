package net.borui.simpl.interpreter;

public class SystemOut extends ProgramOutput {
  @Override
  public void println(String a) {
    System.out.println(a);
  }

  private static SystemOut instance;

  @Override
  public ProgramOutput getInstance() {
    if (instance == null) instance = new SystemOut();
    return instance;
  }
}
