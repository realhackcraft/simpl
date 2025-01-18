package net.borui.simpl.constructs;

public record VNumber(double value) implements Variable {

  @Override
  public String toString() {
    return "VNumber{" + value + "}";
  }

  @Override
  public String display() {
    return value + "";
  }

  @Override
  public VFunction getMethod(String identifier) {
    return null;
  }
}
