package net.borui.simpl.constructs;

import net.borui.simpl.datastructure.ScopedMemory;

/**
 * The Java code to run when a built-in function is called.
 */
public interface VBuiltInFunctionCode {
  /**
   * Runs the java code.
   *
   * @param memory the memory which the Java code has access to.
   * @return the result of evaluating the Java code.
   */
  Variable run(ScopedMemory memory);
}
