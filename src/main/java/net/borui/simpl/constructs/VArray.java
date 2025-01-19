package net.borui.simpl.constructs;

import net.borui.simpl.datastructure.ScopedMemory;
import net.borui.simpl.exceptions.VariableNotFound;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * An array variable in simpl.
 * It can represent an array of any type of elements, even mixed.
 */
public class VArray implements Variable {
  /**
   * the inner storage of the array.
   */
  public ArrayList<Variable> value;
  /**
   * the methods which the array has.
   */
  private static final HashMap<String, VBuiltInFunction> methods = new HashMap<>();

  static {
    LinkedHashMap<String, Class<? extends Variable>> pushParam = new LinkedHashMap<>();
    pushParam.put("self", VArray.class);
    pushParam.put("value", Variable.class);
    methods.put("push", new VBuiltInFunction(pushParam, VBoolean.class, (ScopedMemory memory) -> {
      try {
        VArray self = (VArray) memory.get("self");
        return new VBoolean(self.value.add(memory.get("value")));
      } catch (VariableNotFound e) {
        System.err.println("Self not found");
        throw new RuntimeException(e);
      }
    }));

    LinkedHashMap<String, Class<? extends Variable>> getParam = new LinkedHashMap<>();
    getParam.put("self", VArray.class);
    getParam.put("index", VNumber.class);
    methods.put("get", new VBuiltInFunction(getParam, Variable.class, (ScopedMemory memory) -> {
      try {
        VArray self = (VArray) memory.get("self");
        return self.value.get((int) ((VNumber) memory.get("index")).value());
      } catch (VariableNotFound e) {
        System.err.println("Self not found");
        throw new RuntimeException(e);
      }
    }));

    LinkedHashMap<String, Class<? extends Variable>> popParam = new LinkedHashMap<>();
    popParam.put("self", VArray.class);
    methods.put("pop", new VBuiltInFunction(popParam, Variable.class, (ScopedMemory memory) -> {
      try {
        VArray self = (VArray) memory.get("self");
        return self.value.removeLast();
      } catch (VariableNotFound e) {
        System.err.println("Self not found");
        throw new RuntimeException(e);
      }
    }));

    LinkedHashMap<String, Class<? extends Variable>> removeElementParam = new LinkedHashMap<>();
    removeElementParam.put("self", VArray.class);
    removeElementParam.put("value", Variable.class);
    methods.put("remove_element", new VBuiltInFunction(removeElementParam, VBoolean.class, (ScopedMemory memory) -> {
      try {
        VArray self = (VArray) memory.get("self");
        return new VBoolean(self.value.remove(memory.get("value")));
      } catch (VariableNotFound e) {
        System.err.println("Self not found");
        throw new RuntimeException(e);
      }
    }));

    LinkedHashMap<String, Class<? extends Variable>> removeParam = new LinkedHashMap<>();
    removeParam.put("self", VArray.class);
    removeParam.put("index", Variable.class);
    methods.put("remove", new VBuiltInFunction(removeParam, Variable.class, (ScopedMemory memory) -> {
      try {
        VArray self = (VArray) memory.get("self");
        return self.value.remove((int) ((VNumber) memory.get("index")).value());
      } catch (VariableNotFound e) {
        System.err.println("Self not found");
        throw new RuntimeException(e);
      }
    }));

    LinkedHashMap<String, Class<? extends Variable>> lengthParam = new LinkedHashMap<>();
    lengthParam.put("self", VArray.class);
    methods.put("length", new VBuiltInFunction(lengthParam, VNumber.class, (ScopedMemory memory) -> {
      try {
        VArray self = (VArray) memory.get("self");
        return new VNumber(self.value.size());
      } catch (VariableNotFound e) {
        System.err.println("Self not found");
        throw new RuntimeException(e);
      }
    }));

    LinkedHashMap<String, Class<? extends Variable>> setParam = new LinkedHashMap<>();
    setParam.put("self", VArray.class);
    setParam.put("index", VNumber.class);
    setParam.put("value", Variable.class);
    methods.put("set", new VBuiltInFunction(setParam, VUnit.class, (ScopedMemory memory) -> {
      try {
        VArray self = (VArray) memory.get("self");
        return self.value.set((int) ((VNumber) memory.get("index")).value(), memory.get("value"));
      } catch (VariableNotFound e) {
        System.err.println("Self not found");
        throw new RuntimeException(e);
      }
    }));
  }

  /**
   * Creates a new VArray from the given initial array.
   *
   * @param value the initial array.
   */
  public VArray(ArrayList<Variable> value) {
    this.value = value;
  }

  /**
   * Gives a String representation of the array.
   * Should only be used when debugging.
   *
   * @return a String representation of the array specifying the data type.
   * @see #display()
   */
  @Override
  public String toString() {
    return "VArray{" + value + "}";
  }

  /**
   * Gives a String representation of the array as should be outputted to the user when printing the variable.
   *
   * @return a String representation of the array meant for displaying to the user.
   */
  @Override
  public String display() {
    StringBuilder result = new StringBuilder("[");
    for (int i = 0; i < value.size() - 1; i++) {
      result.append(value.get(i).display()).append(", ");
    }
    result.append(value.getLast().display());
    return result + "]";
  }

  /**
   * Gets the methods present in this variable type.
   *
   * @param identifier the name of the method.
   * @return the method function requested.
   */
  @Override
  public VFunction getMethod(String identifier) {
    return VArray.methods.get(identifier);
  }

  @Override
  public final boolean equals(Object o) {
    if (!(o instanceof VArray vArray)) return false;

    return value.equals(vArray.value);
  }
}
