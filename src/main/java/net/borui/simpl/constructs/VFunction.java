package net.borui.simpl.constructs;

import io.github.treesitter.jtreesitter.Node;
import net.borui.simpl.datastructure.ScopedMemory;
import net.borui.simpl.exceptions.*;
import net.borui.simpl.interpreter.Interpreter;

import java.util.LinkedHashMap;
import java.util.List;

public class VFunction implements Variable {
  public final LinkedHashMap<String, Class<? extends Variable>> parameters;
  public final Class<? extends Variable> returnType;
  private final List<Node> scope;

  public VFunction(List<Node> scope, LinkedHashMap<String, Class<? extends Variable>> parameters, Class<? extends Variable> returnType) {
    this.scope = scope;
    this.parameters = parameters;
    this.returnType = returnType;
  }

  // https://stackoverflow.com/questions/215497/what-is-the-difference-between-public-protected-package-private-and-private-in
  protected boolean validateArguments(Variable[] arguments) {
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

  protected ScopedMemory assignArguments(ScopedMemory memory, Variable[] arguments) {
    int index = 0;
    ScopedMemory newMemory = new ScopedMemory(memory);
    for (String key : parameters.keySet()) {
      newMemory.define(key, arguments[index]);
      index++;
    }
    return newMemory;
  }

  public Variable run(Variable[] arguments, ScopedMemory memory) throws InvalidArgumentException, IncorrectReturnTypeException, UnexpectedValueException {
    if (!validateArguments(arguments)) throw new InvalidArgumentException();

    ScopedMemory initialMemory = assignArguments(memory, arguments);
    try {
      Variable returnValue = Interpreter.getInstance().scope(this.scope, initialMemory);
      if (this.returnType.isInstance(returnValue)) {
        return returnValue;
      } else {
        throw new IncorrectReturnTypeException(this.returnType, returnValue.getClass());
      }
    } catch (UnexpectedNodeTypeException e) {
      System.err.println("The scope List<Node> contains unexcecutable tokens.");
      throw new RuntimeException(e);
    } catch (InvalidVariableException e) {
      System.err.println("The variable \"" + e.variable + "\" cannot be found in the scope.");
      throw new RuntimeException(e);
    } catch (VariableNotFound e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public String toString() {
    return "VFunction{}";
  }

  @Override
  public String display() {
    return "";
  }

  @Override
  public VFunction getMethod(String identifier) {
    return null;
  }
}
