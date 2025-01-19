package net.borui.simpl.constructs;

import net.borui.simpl.datastructure.ScopedMemory;
import net.borui.simpl.exceptions.InvalidArgumentException;

import java.util.LinkedHashMap;

/**
 * A variable in simpl representing a built-in function.
 */
public class VBuiltInFunction extends VFunction {
  /**
   * The code to execute when called.
   */
  private final VBuiltInFunctionCode code;

  /**
   * Creates a new built-in function.
   *
   * @param parameters the parameters of the built-in function.
   * @param returnType the return type of the built-in function.
   * @param code       the code to run when the built-in fucntion is executed.
   */
  public VBuiltInFunction(LinkedHashMap<String, Class<? extends Variable>> parameters, Class<? extends Variable> returnType, VBuiltInFunctionCode code) {
    // Doesn't have scope: uses a lambda instead
    super(null, parameters, returnType);
    this.code = code;
  }

  /**
   * Executes the code of the built-in function.
   *
   * @param arguments the arguments to give the built-in function.
   * @param memory    the memory that the built-in function has access to.
   * @return the result of evaluating the built-in function.
   * @throws InvalidArgumentException the argument of the built-in function doesn't match its parameter specification.
   */
  @Override
  public Variable run(Variable[] arguments, ScopedMemory memory) throws InvalidArgumentException {

    if (!validateArguments(arguments)) throw new InvalidArgumentException();

    ScopedMemory initialMemory = assignArguments(memory, arguments);
    return this.code.run(initialMemory);
  }
}
