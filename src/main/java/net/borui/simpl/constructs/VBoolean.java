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

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof VBoolean(boolean value1))) return false;

    return value == value1;
  }
}
