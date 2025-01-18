package net.borui.simpl.constructs;

public record VBoolean(boolean value) implements Variable {

  @Override
  public String display() {
    return value ? "true" : "false";
  }

  @Override
  public VFunction getMethod(String identifier) {
    return null;
  }
}
