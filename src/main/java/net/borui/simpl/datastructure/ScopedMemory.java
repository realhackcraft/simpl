package net.borui.simpl.datastructure;

import net.borui.simpl.constructs.Variable;
import net.borui.simpl.exceptions.VariableNotFound;

import java.util.HashMap;
import java.util.Map;

/**
 * A memory storage which is scoped.
 * There is a hierarchy of ScopedMemories with the innermost ScopedMemory being able to access all variables of its parents.
 * The parents may not access variables in its children. It shouldn't even be aware of them.
 */
public class ScopedMemory {
  /**
   * The scope of variables owned by this ScopedMemory.
   */
  private final Map<String, Variable> currentScope = new HashMap<>();
  /**
   * The parent of this ScopedMemory.
   * Nullable: indicates that there isn't a parent.
   */
  private ScopedMemory parentScope;

  /**
   * Creates a new ScopedMemory without a parent.
   */
  public ScopedMemory() {
  }

  /**
   * Creates a ScopedMemory with a parent.
   *
   * @param parentScope the parent of this ScopedMemory.
   */
  public ScopedMemory(ScopedMemory parentScope) {
    this.parentScope = parentScope;
  }

  /**
   * Gets a variable based on its name from either this scope or its parents.
   *
   * @param name the name of the variable.
   * @return the value of the variable, if it exists.
   * @throws VariableNotFound when the variable is not found in the current nor the parent scopes.
   */
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

  /**
   * Defines a new variable in the current scope.
   *
   * @param name  the name of the new variable.
   * @param value the value of the new variable.
   */
  public void define(String name, Variable value) {
    // if var not in current scope: put in current scope
    if (!currentScope.containsKey(name)) {
      currentScope.put(name, value);
    } else {
      parentScope.define(name, value);
    }
  }

  /**
   * Assigns a variable in the current or the parent scope to a new value.
   *
   * @param name  the name of the variable.
   * @param value the value to change the variable to.
   * @throws VariableNotFound when the variable cannot be found.
   */
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


  /**
   * Whether this or the parent scope contains a variable.bb
   *
   * @param name the name of the variable to search for.
   * @return True if the variable is located in this scope or its parent scope, false otherwise.
   */
  public boolean containsKey(String name) {
    return currentScope.containsKey(name) || (parentScope != null && parentScope.containsKey(name));
  }

  /**
   * Gives a String representation of the scope and its parents, showing the hierarchy of scopes and the values of every variable in them.
   *
   * @return a String representation of the scope and its parents.
   */
  @Override
  public String toString() {
    return currentScope + "\n" + (parentScope != null ? "-> " + parentScope : "");
  }
}
