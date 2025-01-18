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

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof VString(String value1))) return false;

    return value.equals(value1);
  }
}
