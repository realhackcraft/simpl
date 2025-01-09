package net.borui.simpl.constructs;

/** VBoolean */
public class VBoolean implements Variable {
  public boolean value;

  public VBoolean(boolean value) {
    this.value = value;
  }

  @Override
  public String display() {
    return value ? "true" : "false";
  }
}
