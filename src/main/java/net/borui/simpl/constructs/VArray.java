package net.borui.simpl.constructs;

import java.util.ArrayList;

// TODO: implement
public class VArray implements Variable {
  public ArrayList<Variable> value = new ArrayList<>();

  public VArray(ArrayList<Variable> value) {
    this.value = value;
  }

  @Override
  public String toString() {
    return "VArray{" + value + "}";
  }

  @Override
  public String display() {
    String result = "[";
    for (int i = 0; i < value.size() - 1; i++) {
      result += value.get(i).display() + ", ";
    }
    result += value.getLast().display();
    return result + "]";
  }
}
