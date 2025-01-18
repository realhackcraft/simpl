package net.borui.simpl.constructs;

public class VUnit implements Variable {
  @Override
  public String toString() {
    return "VUnit{}";
  }

  @Override
  public String display() {
    return "()";
  }

  @Override
  public VFunction getMethod(String identifier) {
    return null;
  }

  @Override
  public boolean equals(Object obj) {
    return obj instanceof VUnit;
  }
}
