package net.borui.simpl.constructs;

import java.util.ArrayList;

// TODO: implement
public class VArray implements Variable {
  public ArrayList<Variable> value = new ArrayList<>();

  @Override
  public String toString() {
    return "VArray{" + value + "}";
  }

  @Override
  public String display() {
    String result = "[";
    for (Variable variable : value) {
      result += variable.display() + ", ";
    }
    return result + "]";
  }
}
