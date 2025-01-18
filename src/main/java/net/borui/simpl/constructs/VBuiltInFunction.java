package net.borui.simpl.constructs;

import net.borui.simpl.datastructure.ScopedMemory;
import net.borui.simpl.exceptions.InvalidArgumentException;

import java.util.LinkedHashMap;

public class VBuiltInFunction extends VFunction {
  private final VBuiltInFunctionCode code;

  public VBuiltInFunction(LinkedHashMap<String, Class<? extends Variable>> parameters, Class<? extends Variable> returnType, VBuiltInFunctionCode code) {
    // Doesn't have scope: uses a lambda instead
    super(null, parameters, returnType);
    this.code = code;
  }

  @Override
  public Variable run(Variable[] arguments, ScopedMemory memory) throws InvalidArgumentException {

    if (!validateArguments(arguments)) throw new InvalidArgumentException();

    ScopedMemory initialMemory = assignArguments(memory, arguments);
    return this.code.run(initialMemory);
  }
}
