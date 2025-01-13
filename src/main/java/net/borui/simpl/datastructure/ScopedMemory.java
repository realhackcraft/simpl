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
    // if var in current: put in current scope.
    // If no var in current scope and no var in parent scope: put var in current
    // scope.
    // Else if has var in parent scope but not current scope: set in parent scope
    if (currentScope.containsKey(name) || (parentScope == null || !parentScope.containsKey(name))) {
      currentScope.put(name, value);
    } else {
      parentScope.set(name, value);
    }
  }

  public boolean containsKey(String name) {
    if (currentScope.containsKey(name) || (parentScope != null && parentScope.containsKey(name))) {
      return true;
    } else {
      return false;
    }
  }
}
