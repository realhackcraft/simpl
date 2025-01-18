package net.borui.simpl.datastructure;

import net.borui.simpl.constructs.Variable;
import net.borui.simpl.exceptions.VariableNotFound;

import java.util.HashMap;
import java.util.Map;

/**
 * ScopedMemory
 */
public class ScopedMemory {
  private final Map<String, Variable> currentScope = new HashMap<>();
  private ScopedMemory parentScope;

  public ScopedMemory() {
  }

  public ScopedMemory(ScopedMemory parentScope) {
    this.parentScope = parentScope;
  }

  public Variable get(String name) throws VariableNotFound {
    Variable returnValue = currentScope.get(name);

    if (returnValue == null) {
      if (parentScope != null) {
        returnValue = parentScope.get(name);
      } else {
        throw new VariableNotFound(name);
      }
    }
    return returnValue;
  }

  public void define(String name, Variable value) {
    // if var not in current scope: put in current scope
    if (!currentScope.containsKey(name)) {
      currentScope.put(name, value);
    } else {
      parentScope.define(name, value);
    }
  }

  public void assign(String name, Variable value) throws VariableNotFound {
    // if var in current: put in current scope.
    // If no var in current scope and no var in parent scope: %@&^@^%&^@!
    // Else if var in parent scope but not current scope: assign in parent scope
    if (currentScope.containsKey(name)) {
      currentScope.put(name, value);
    } else if (parentScope.containsKey(name)) {
      parentScope.assign(name, value);
    } else {
      throw new VariableNotFound(name);
    }
  }


  public boolean containsKey(String name) {
    return currentScope.containsKey(name) || (parentScope != null && parentScope.containsKey(name));
  }

  @Override
  public String toString() {
    return currentScope + "\n" + (parentScope != null ? "-> " + parentScope : "");
  }
}
