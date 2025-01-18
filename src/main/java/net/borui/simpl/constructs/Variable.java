package net.borui.simpl.constructs;

public interface Variable {
  String display();

  VFunction getMethod(String identifier);
}
