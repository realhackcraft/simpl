package net.borui.simpl.constructs;

// TODO: implement
public class VString implements Variable {
  public String value = "";

  public VString(String value) {
    this.value = value;
  }

  @Override
  public String toString() {
    return "VString{" + value + "}";
  }

  @Override
  public String display() {
    return value;
  }
}
