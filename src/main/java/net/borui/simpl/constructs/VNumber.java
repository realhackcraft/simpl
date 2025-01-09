package net.borui.simpl.constructs;

public class VNumber implements Variable {
  public double value = 0;

  public VNumber(double value) {
    this.value = value;
  }

  @Override
  public String toString() {
    return "VNumber{" + value + "}";
  }

  @Override
  public String display() {
    return value + "";
  }
}
