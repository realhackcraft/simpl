package net.borui.simpl.datastructure;

import java.util.HashMap;
import java.util.Map;
import net.borui.simpl.constructs.Variable;
import net.borui.simpl.exceptions.VariableNotFound;

/** ScopedMemory */
public class ScopedMemory {
  private Map<String, Variable> currentScope = new HashMap<>();
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

  public void set(String name, Variable value) throws VariableNotFound {
    if (currentScope.containsKey(name)) {
      currentScope.put(name, value);
    } else if (parentScope != null) {
      parentScope.set(name, value);
    } else {
      throw new VariableNotFound(name);
    }
  }
}
