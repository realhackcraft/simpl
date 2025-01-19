package net.borui.simpl.interpreter;

import io.github.treesitter.jtreesitter.Node;
import net.borui.simpl.constructs.*;
import net.borui.simpl.datastructure.ScopedMemory;
import net.borui.simpl.exceptions.*;

import java.util.*;

/**
 * The interpreter for the simpl programming language.
 */
public class Interpreter {
  /**
   * A hashmap from a String to its corresponding type of variable.
   */
  public static final Map<String, Class<? extends Variable>> typeMap = new HashMap<>();
  /**
   * A hashmap from a type of variable to its corresponding String representation.
   */
  public static final Map<Class<? extends Variable>, String> reverseTypeMap = new HashMap<>();

  /**
   * The method of output for the program.
   */
  public static ProgramOutput output = new SystemOut();

  static {
    // Add mappings for type classes
    typeMap.put("array", VArray.class);
    typeMap.put("any", Variable.class);
    typeMap.put("number", VNumber.class);
    typeMap.put("boolean", VBoolean.class);
    typeMap.put("string", VString.class);
    typeMap.put("unit", VUnit.class);

    reverseTypeMap.put(VArray.class, "array");
    reverseTypeMap.put(Variable.class, "any");
    reverseTypeMap.put(VNumber.class, "number");
    reverseTypeMap.put(VBoolean.class, "boolean");
    reverseTypeMap.put(VString.class, "string");
    reverseTypeMap.put(VUnit.class, "unit");
  }

  private Interpreter() {
  }

  /**
   * Gets an instance of the interpreter. Creates a new one if it doesn't exist.
   *
   * @return the instance of the interpreter.
   */
  public static Interpreter getInstance() {
    return InterpreterSingletonFactory.INSTANCE;
  }

  /**
   * Creates and execute a scope from a list of tree-sitter nodes.
   *
   * @param nodes the notes that are part of the scope.
   * @throws UnexpectedNodeTypeException when encountering a type of tree-sitter node which cannot be understood by the interpreter.
   * @throws UnexpectedValueException    when a value of a variable is not understandable by the interpreter.
   * @throws VariableNotFound            when a variable doesn't exist.
   */
  public void scope(List<Node> nodes) throws UnexpectedNodeTypeException, UnexpectedValueException, VariableNotFound {
    ScopedMemory memory = new ScopedMemory();
    scope(nodes, memory);
  }

  /**
   * Creates and execute a scope from a list of tree-sitter nodes.
   *
   * @param nodes  the notes that are part of the scope.
   * @param memory the memory which the scope can access.
   * @throws UnexpectedNodeTypeException when encountering a type of tree-sitter node which cannot be understood by the interpreter.
   * @throws UnexpectedValueException    when a value of a variable is not understandable by the interpreter.
   * @throws VariableNotFound            when a variable doesn't exist.
   */
  public Variable scope(List<Node> nodes, ScopedMemory memory) throws UnexpectedNodeTypeException, UnexpectedValueException, VariableNotFound {

    ScopedMemory map = new ScopedMemory(memory);
    for (Node child : nodes) {
      Node statement = child.getChild(0).get();
      switch (statement.getType()) {
        case "let_declaration" -> {
          String identifier = statement.getChild(1).get().getText();
          Node value = statement.getChild(3).get();
          try {
            Variable computedExpression = computeExpression(value, map);
            map.define(identifier, computedExpression);
          } catch (UnexpectedNodeTypeException e) {
            throw new RuntimeException(e);
          }
        }
        case "assign_statement" -> {
          String identifier = statement.getChild(0).get().getText();
          Node value = statement.getChild(2).get();
          try {
            Variable computedExpression = computeExpression(value, map);
            map.assign(identifier, computedExpression);
          } catch (UnexpectedNodeTypeException e) {
            throw new RuntimeException(e);
          }
        }
        case "print_statement" -> {
          Node printExpression = statement.getChild(1).get();
          try {
            output.println(computeExpression(printExpression, map).display());
          } catch (UnexpectedNodeTypeException e) {
            throw new RuntimeException(e);
          }
        }
        case "return_statement" -> {
          if (statement.getChild(1).get().getType().equals(";")) {
            return new VUnit();
          }
          Node returnExpression = statement.getChild(1).get();
          try {
            return computeExpression(returnExpression, map);
          } catch (UnexpectedNodeTypeException e) {
            throw new RuntimeException(e);
          }
        }
        case "scope" -> {
          List<Node> statements = new ArrayList<>(child.getChild(0).get().getChildren());
          statements.removeFirst();
          statements.removeLast();
          // Now statement is just a list of statements
          // No `return` because the programmer is not storing the returned value
          scope(statements, map);
        }
        case "fn_declaration" -> {
          String fnName = statement.getChild(1).get().getText();
          // Loop over all the parameters, skipping commas and stopping at ")"
          LinkedHashMap<String, Class<? extends Variable>> parameters = new LinkedHashMap<>();

          // Iterator variable needed outside of loop
          int i = 3;
          // First parameter
          boolean skipParams = Objects.equals(statement.getChild(i).get().getText(), ")");
          if (!skipParams) {
            for (; !statement.getChild(i - 1).get().getType().equals(")"); i += 2) {
              if (!statement.getChild(i).get().getType().equals("typed_identifier")) {
                throw new UnexpectedNodeTypeException("typed_identifier", statement.getChild(i).get().getType());
              }

              Node typedId = statement.getChild(i).get();
              // 0 is var name; 1 is colon; 2 is var type
              String name = typedId.getChild(0).get().getText();
              String type = typedId.getChild(2).get().getText();

              // Get class
              Class<? extends Variable> typeClass = Interpreter.typeMap.get(type);
              parameters.put(name, typeClass);
            }
          }

          Class<? extends Variable> returnType = VUnit.class;
          Node nextToken = statement.getChild(i).get();

          if (nextToken.getType().equals("return_type")) {
            returnType = Interpreter.typeMap.get(nextToken.getChild(1).get().getText());
          } else if (!skipParams) {
            i -= 1;
          }
          nextToken = statement.getChild(i + 1).get();
          ArrayList<Node> scope = new ArrayList<>(nextToken.getChildren());
          scope.removeFirst();
          scope.removeLast();
          VFunction vFunction = new VFunction(scope, parameters, returnType);
          map.define(fnName, vFunction);
        }
        case "if_statement" -> {
          Variable expression = computeExpression(statement.getChild(2).get(), map);
          if (!(expression instanceof VBoolean))
            throw new UnexpectedValueException(expression, "boolean", reverseTypeMap.get(expression.getClass()));

          boolean ifExpressionResult = ((VBoolean) expression).value();
          if (ifExpressionResult) {
            ArrayList<Node> ifScope = new ArrayList<>(statement.getChild(4).get().getChildren());
            ifScope.removeFirst();
            ifScope.removeLast();
            scope(ifScope, map);
          }
        }
        case "fn_call_statement" -> {
          // Look up the function with the function name
          String fn_name = statement.getChild(0).get().getText();
          Variable var = map.get(fn_name);
          if (var == null) throw new VariableNotFound(fn_name);
          VFunction function = (VFunction) var;

          // Create a list of arguments (resolve all expressions first)
          List<Node> children = statement.getChildren();

          int argumentCount = 0;
          for (Node node : children) {
            if (node.getType().equals("expression")) argumentCount++;
          }
          Variable[] arguments = new Variable[argumentCount];
          int argumentIndex = 0;

          for (int j = 2; j < children.size() - 2; j += 2) {
            Variable expressionResult = computeExpression(children.get(j), map);
            arguments[argumentIndex] = expressionResult;
            argumentIndex++;
          }

          // Call the run method on the function with a list of arguments
          try {
            function.run(arguments, map);
          } catch (InvalidArgumentException e) {
            throw new RuntimeException(e);
          } catch (IncorrectReturnTypeException e) {
            System.err.println("The function \"" + fn_name + "\" expects a return type of \"" + e.expected + "\" but got \"" + e.actual);
            throw new RuntimeException(e);
          }
        }
        case "method_call_statement" -> {
          String identifier = statement.getChild(0).get().getText();
          Node methodFn = statement.getChild(2).get();
          String methodFnName = methodFn.getChild(0).get().getText();

          Variable variable = map.get(identifier);
          VFunction method = variable.getMethod(methodFnName);

          // Create a list of arguments (resolve all expressions first)
          List<Node> argumentNodes = methodFn.getChildren();

          int argumentCount = 1;
          for (Node node : argumentNodes) {
            if (node.getType().equals("expression")) argumentCount++;
          }
          Variable[] arguments = new Variable[argumentCount];
          arguments[0] = variable;
          int argumentIndex = 1;

          // -1 seems to work here whereas -2 works with functions
          for (int j = 2; j < argumentNodes.size() - 1; j += 2) {
            Variable expressionResult = computeExpression(argumentNodes.get(j), map);
            arguments[argumentIndex] = expressionResult;
            argumentIndex++;
          }

          try {
            method.run(arguments, map);
          } catch (InvalidArgumentException | IncorrectReturnTypeException e) {
            throw new RuntimeException(e);
          }
        }
        case "while_loop" -> {
          ArrayList<Node> whileScope = new ArrayList<>(statement.getChild(4).get().getChildren());
          whileScope.removeFirst();
          whileScope.removeLast();
          Variable whileCondition = computeExpression(statement.getChild(2).get(), map);
          if (whileCondition instanceof VBoolean) {
            while (((VBoolean) whileCondition).value()) {
              scope(whileScope, map);
              // Re-evaluate condition
              whileCondition = computeExpression(statement.getChild(2).get(), map);
            }
          }
        }
        default -> {
        }
      }
    }

    return new VUnit();
  }

  /**
   * Computes an expression based on the given tree-sitter node and the memory state.
   *
   * @param node   the note to evaluate the expression of.
   * @param memory the memory to pull additional information from.
   * @return a variable that is the result of the expression.
   * @throws UnexpectedNodeTypeException when encountering a type of tree-sitter node which cannot be understood by the interpreter.
   * @throws UnexpectedValueException    when a value of a variable is not understandable by the interpreter.
   * @throws VariableNotFound            when a variable doesn't exist.
   */
  public Variable computeExpression(Node node, ScopedMemory memory) throws UnexpectedNodeTypeException, UnexpectedValueException, VariableNotFound {
    if (!node.getType().equals("expression")) throw new UnexpectedNodeTypeException("expression", node.getType());

    Node child = node.getChild(0).get();
    switch (child.getType()) {
      case "number" -> {
        double value = Double.parseDouble(Objects.requireNonNull(child.getText()));
        return new VNumber(value);
      }
      case "string" -> {
        ArrayList<Node> stringContents = new ArrayList<>(child.getChildren());
        stringContents.removeFirst();
        stringContents.removeLast();
        StringBuilder resultString = new StringBuilder();
        for (Node stringContentsNode : stringContents) {
          resultString.append(stringContentsNode.getText());
        }
        return new VString(resultString.toString());
      }
      case "boolean" -> {
        return new VBoolean(child.getText().equals("true"));
      }
      case "array" -> {
        ArrayList<Node> arrayElements = new ArrayList<>(child.getChildren());
        arrayElements.removeFirst();
        arrayElements.removeLast();

        ArrayList<Variable> arrayVariables = new ArrayList<>();
        for (int i = 0; i < arrayElements.size(); i += 2) {
          Node expressionNode = arrayElements.get(i);
          Variable expressionValue = computeExpression(expressionNode, memory);
          arrayVariables.add(expressionValue);
        }

        return new VArray(arrayVariables);
      }
      case "identifier" -> {
        return memory.get(child.getText());
      }
      case "fn_call" -> {
        // Look up the function with the function name
        String fn_name = child.getChild(0).get().getText();
        Variable var = memory.get(fn_name);
        if (var == null) throw new VariableNotFound(fn_name);
        VFunction function = (VFunction) var;

        // Create a list of arguments (resolve all expressions first)
        List<Node> children = child.getChildren();

        int argumentCount = 0;
        for (Node childrenNode : children) {
          if (childrenNode.getType().equals("expression")) argumentCount++;
        }
        Variable[] arguments = new Variable[argumentCount];
        int argumentIndex = 0;

        // -2 with fn_call_statement
        for (int j = 2; j < children.size() - 1; j += 2) {
          Variable expressionResult = computeExpression(children.get(j), memory);
          arguments[argumentIndex] = expressionResult;
          argumentIndex++;
        }

        // Call the run method on the function with a list of arguments
        try {
          return function.run(arguments, memory);
        } catch (InvalidArgumentException e) {
          throw new RuntimeException(e);
        } catch (IncorrectReturnTypeException e) {
          System.err.println("The function \"" + fn_name + "\" expects a return type of \"" + e.expected + "\" but got \"" + e.actual);
          throw new RuntimeException(e);
        }
      }
      case "method_call" -> {
        String identifier = child.getChild(0).get().getText();
        Node methodFn = child.getChild(2).get();
        String methodFnName = methodFn.getChild(0).get().getText();

        Variable variable = memory.get(identifier);
        VFunction method = variable.getMethod(methodFnName);

        // Create a list of arguments (resolve all expressions first)
        List<Node> argumentNodes = methodFn.getChildren();

        int argumentCount = 1;
        for (Node argumentNode : argumentNodes) {
          if (argumentNode.getType().equals("expression")) argumentCount++;
        }
        Variable[] arguments = new Variable[argumentCount];
        arguments[0] = variable;
        int argumentIndex = 1;

        // Method_call_statement requires -1 for ")" and -1 for ";", but because the ";"
        // doesn't
        // exist here, it is only -1.
        for (int j = 2; j < argumentNodes.size() - 1; j += 2) {
          Variable expressionResult = computeExpression(argumentNodes.get(j), memory);
          arguments[argumentIndex] = expressionResult;
          argumentIndex++;
        }

        try {
          return method.run(arguments, memory);
        } catch (InvalidArgumentException | IncorrectReturnTypeException e) {
          throw new RuntimeException(e);
        }
      }
      case "scope" -> {
        List<Node> statements = new ArrayList<>(child.getChildren());
        statements.removeFirst();
        statements.removeLast();
        // Now statement is just a list of statements
        return scope(statements, memory);
      }
      case "operation" -> {
        Node operation = child.getChild(0).get();
        Node left = operation.getChild(0).get();
        Node operator = operation.getChild(1).get();
        Node right = operation.getChild(2).get();

        Variable leftValue = computeExpression(left, memory);
        Variable rightValue = computeExpression(right, memory);

        // Equality check with different types
        if (!leftValue.getClass().equals(rightValue.getClass())) {
          switch (operation.getType()) {
            case "equal_to" -> {
              return new VBoolean(false);
            }
            case "not_equal_to" -> {
              return new VBoolean(true);
            }
          }
        }

        switch (operation.getType()) {
          case "equal_to" -> {
            return new VBoolean(leftValue.equals(rightValue));
          }
          case "not_equal_to" -> {
            return new VBoolean(!leftValue.equals(rightValue));
          }
        }


        if (leftValue instanceof VNumber && rightValue instanceof VNumber) {
          double leftNum = ((VNumber) leftValue).value();
          double rightNum = ((VNumber) rightValue).value();

          return switch (operation.getType()) {
            case "addition" -> new VNumber(leftNum + rightNum);
            case "subtraction" -> new VNumber(leftNum - rightNum);
            case "multiplication" -> new VNumber(leftNum * rightNum);
            case "division" -> {
              if (rightNum == 0) throw new RuntimeException("Division by zero");
              yield new VNumber(leftNum / rightNum);
            }
            case "less_than" -> new VBoolean(leftNum < rightNum);
            case "less_than_or_equal_to" -> new VBoolean(leftNum <= rightNum);
            case "greater_than" -> new VBoolean(leftNum > rightNum);
            case "greater_than_or_equal_to" -> new VBoolean(leftNum >= rightNum);
            default -> throw new RuntimeException("Unknown operation: " + operator.getType());
          };
        } else if (leftValue instanceof VString(String value)) {
          switch (rightValue) {
            case VString vString -> {
              if (operation.getType().equals("addition")) {
                return new VString(value + vString.value());
              } else {
                throw new RuntimeException("Unknown operation: " + operator.getType());
              }
            }
            case VNumber(double doubleValue) -> {
              if (operation.getType().equals("addition")) {
                return new VString(value + doubleValue);
              }
              throw new RuntimeException("Unknown operation: " + operator.getType());
            }
            case VBoolean(boolean booleanValue) -> {
              if (operation.getType().equals("addition")) {
                return new VString(value + booleanValue);
              }
              throw new RuntimeException("Unknown operation: " + operator.getType());
            }
            case VArray arrayValue -> {
              if (operation.getType().equals("addition")) {
                return new VString(value + arrayValue.display());
              }
              throw new RuntimeException("Unknown operation: " + operator.getType());
            }
            default ->
                throw new RuntimeException("Cannot perform any operations between " + reverseTypeMap.get(leftValue.getClass()) + " and " + reverseTypeMap.get(rightValue.getClass()));
          }
        } else {
          throw new RuntimeException("Operands must be numbers" + leftValue + rightValue);
        }
      }
    }
    // When this number appears in code, you know something's gone wrong with
    // variables
    return new VNumber(1337);
  }

  /**
   * A Singleton factory to create and store a new interpreter on demand.
   */
  private static class InterpreterSingletonFactory {
    /**
     * The single instance of the interpreter.
     */
    private static final Interpreter INSTANCE = new Interpreter();
  }
}
