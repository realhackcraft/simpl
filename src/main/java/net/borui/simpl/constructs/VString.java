package net.borui.simpl.constructs;

public record VString(String value) implements Variable {

  @Override
  public String toString() {
    return "VString{" + value + "}";
  }

  @Override
  public String display() {
    return value;
  }

  @Override
  public VFunction getMethod(String identifier) {
    return null;
  }
}
