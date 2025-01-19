package net.borui.simpl.constructs;

import io.github.treesitter.jtreesitter.Node;
import net.borui.simpl.datastructure.ScopedMemory;
import net.borui.simpl.exceptions.*;
import net.borui.simpl.interpreter.Interpreter;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * a variable in simple representing a function.
 */
public class VFunction implements Variable {
  /**
   * The parameters of the function.
   */
  public final LinkedHashMap<String, Class<? extends Variable>> parameters;
  /**
   * The return type of the function.
   */
  public final Class<? extends Variable> returnType;
  /**
   * The scope to evaluate when running the function.
   */
  private final List<Node> scope;

  /**
   * Creates a new built-in function.
   *
   * @param scope      the scope to evaluate when the function is called.
   * @param parameters the parameters of the function.
   * @param returnType the return type of the function.
   */
  public VFunction(List<Node> scope, LinkedHashMap<String, Class<? extends Variable>> parameters, Class<? extends Variable> returnType) {
    this.scope = scope;
    this.parameters = parameters;
    this.returnType = returnType;
  }

  /**
   * Validates the argument given with the functions parameters.
   *
   * @param arguments the arguments to the function.
   * @return True if valid, false otherwise.
   */
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

  /**
   * assigns the arguments to a {@link ScopedMemory}.
   *
   * @param memory    the initial memory.
   * @param arguments the arguments of the function.
   * @return a new ScopedMemory containing the arguments in its current scope and the provided memory as its parent scope.
   */
  protected ScopedMemory assignArguments(ScopedMemory memory, Variable[] arguments) {
    int index = 0;
    ScopedMemory newMemory = new ScopedMemory(memory);
    for (String key : parameters.keySet()) {
      newMemory.define(key, arguments[index]);
      index++;
    }
    return newMemory;
  }

  /**
   * Executes the scope of the function.
   *
   * @param arguments the arguments to give the function.
   * @param memory    the memory that the function has access to.
   * @return the result of evaluating the function.
   * @throws InvalidArgumentException the argument of the function doesn't match its parameter specification.
   */
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
    } catch (VariableNotFound e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Gives a String representation of the function.
   * Should only be used when debugging.
   *
   * @return a String representation of the function specifying the data type.
   * @see #display()
   */
  @Override
  public String toString() {
    return "VFunction{}";
  }

  /**
   * Gives a String representation of the function as should be outputted to the user when printing the variable.
   *
   * @return a String representation of the function meant for displaying to the user.
   */
  @Override
  public String display() {
    return "";
  }

  /**
   * Gets the methods present in this variable type.
   *
   * @param identifier the name of the method.
   * @return the method function requested.
   */
  @Override
  public VFunction getMethod(String identifier) {
    return null;
  }
}
