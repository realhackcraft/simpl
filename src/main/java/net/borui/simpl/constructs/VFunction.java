package net.borui.simpl.constructs;

import io.github.treesitter.jtreesitter.Node;
import java.util.LinkedHashMap;
import java.util.List;
import net.borui.simpl.datastructure.ScopedMemory;
import net.borui.simpl.exceptions.IncorrectReturnTypeException;
import net.borui.simpl.exceptions.InvalidArgumentException;
import net.borui.simpl.exceptions.InvalidVariableException;
import net.borui.simpl.exceptions.UnexpectedNodeTypeException;
import net.borui.simpl.exceptions.UnexpectedValueException;
import net.borui.simpl.exceptions.VariableNotFound;
import net.borui.simpl.interpreter.Interpreter;

public class VFunction implements Variable {
  private List<Node> scope;
  public final LinkedHashMap<String, Class<? extends Variable>> parameters;
  public final Class<? extends Variable> returnType;

  public VFunction(
      List<Node> scope,
      LinkedHashMap<String, Class<? extends Variable>> parameters,
      Class<? extends Variable> returnType) {
    this.scope = scope;
    this.parameters = parameters;
    this.returnType = returnType;
  }

  private boolean validateArguments(Variable[] arguments) {
    if (arguments.length != parameters.size()) {
      return false; // Argument count mismatch
    }

    int index = 0;
    for (Class<? extends Variable> expectedType : parameters.values()) {
      if (!expectedType.isInstance(arguments[index])) {
        return false; // Type mismatch at position `index`
      }
      index++;
    }
    return true; // All arguments match
  }

  private ScopedMemory assignArugments(ScopedMemory memory, Variable[] arguements) {
    int index = 0;
    ScopedMemory newMemory = new ScopedMemory(memory);
    try {
      for (String key : parameters.keySet()) {
        newMemory.set(key, arguements[index]);
        index++;
      }
    } catch (VariableNotFound e) {
      e.printStackTrace();
      System.exit(1);
    }
    return newMemory;
  }

  public Variable run(Variable[] arguments, ScopedMemory memory)
      throws InvalidArgumentException, IncorrectReturnTypeException, UnexpectedValueException {
    if (!validateArguments(arguments))
      throw new InvalidArgumentException();

    ScopedMemory initialMemory = assignArugments(memory, arguments);
    try {
      Variable returnValue = Interpreter.getInstance().scope(this.scope, initialMemory);
      if (this.returnType.isInstance(returnValue)) {
        return returnValue;
      } else {
        throw new IncorrectReturnTypeException(this.returnType, returnValue.getClass());
      }
    } catch (UnexpectedNodeTypeException e) {
      System.err.println("The scope List<Node> contains unexcecutable tokens.");
      e.printStackTrace();
      System.exit(1);
    } catch (InvalidVariableException e) {
      System.err.println("The variable \"" + e.variable + "\" cannot be found in the scope.");
      e.printStackTrace();
      System.exit(1);
    } catch (VariableNotFound e) {
      e.printStackTrace();
      System.exit(1);
    }

    // This should never be excecuted
    return new VUnit();
  }

  @Override
  public String toString() {
    return "VFunction{}";
  }

  @Override
  public String display() {
    return "";
  }
}
