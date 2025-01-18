package net.borui.simpl.interpreter;

import io.github.treesitter.jtreesitter.Node;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.borui.simpl.constructs.VArray;
import net.borui.simpl.constructs.VBoolean;
import net.borui.simpl.constructs.VFunction;
import net.borui.simpl.constructs.VNumber;
import net.borui.simpl.constructs.VString;
import net.borui.simpl.constructs.VUnit;
import net.borui.simpl.constructs.Variable;
import net.borui.simpl.datastructure.ScopedMemory;
import net.borui.simpl.exceptions.IncorrectReturnTypeException;
import net.borui.simpl.exceptions.InvalidArgumentException;
import net.borui.simpl.exceptions.InvalidVariableException;
import net.borui.simpl.exceptions.UnexpectedNodeTypeException;
import net.borui.simpl.exceptions.UnexpectedValueException;
import net.borui.simpl.exceptions.VariableNotFound;

public class Interpreter {
  public static Map<String, Class<? extends Variable>> typeMap = new HashMap<>();
  public static Map<Class<? extends Variable>, String> reverseTypeMap = new HashMap<>();

  public static ProgramOutput output = new SystemOut();

  static {
    // Add mappings for type classes
    typeMap.put("array", VArray.class);
    typeMap.put("number", VNumber.class);
    typeMap.put("boolean", VBoolean.class);
    typeMap.put("string", VString.class);
    typeMap.put("()", VUnit.class);

    reverseTypeMap.put(VArray.class, "array");
    reverseTypeMap.put(VNumber.class, "number");
    reverseTypeMap.put(VBoolean.class, "boolean");
    reverseTypeMap.put(VString.class, "string");
    reverseTypeMap.put(VUnit.class, "()");
  }

  private Interpreter() {}

  private static class InterpreterSingletonFactory {
    private static final Interpreter INSTANCE = new Interpreter();
  }

  public static Interpreter getInstance() {
    return InterpreterSingletonFactory.INSTANCE;
  }

  public Variable scope(List<Node> nodes)
      throws UnexpectedNodeTypeException,
          InvalidVariableException,
          UnexpectedValueException,
          VariableNotFound {
    ScopedMemory memory = new ScopedMemory();
    return scope(nodes, memory);
  }

  public Variable scope(List<Node> nodes, ScopedMemory memory)
      throws UnexpectedNodeTypeException,
          InvalidVariableException,
          UnexpectedValueException,
          VariableNotFound {

    ScopedMemory map = new ScopedMemory(memory);
    for (Node child : nodes) {
      Node statement = child.getChild(0).get();
      switch (statement.getType()) {
        case "let_declaration":
          String identifier = statement.getChild(1).get().getText();
          Node value = statement.getChild(3).get();
          try {
            Variable computedExpression = computeExpression(value, map);
            map.set(identifier, computedExpression);
          } catch (UnexpectedNodeTypeException e) {
            e.printStackTrace();
          }
          break;

        case "print_statement":
          Node printExpression = statement.getChild(1).get();
          try {
            output.println(computeExpression(printExpression, map).display());
          } catch (UnexpectedNodeTypeException e) {
            e.printStackTrace();
          }
          break;

        case "return_statement":
          if (statement.getChild(1).get().getType().equals(";")) {
            return new VUnit();
          }
          Node returnExpression = statement.getChild(1).get();
          try {
            return computeExpression(returnExpression, map);
          } catch (UnexpectedNodeTypeException e) {
            e.printStackTrace();
          }
          break;

        case "scope":
          List<Node> statements = new ArrayList<>(child.getChild(0).get().getChildren());
          statements.removeFirst();
          statements.removeLast();
          // Now statement is just a list of statements
          // No `return` because the programmer is not storing the returned value
          scope(statements, map);
          break;

        case "fn_declaration":
          String fnName = statement.getChild(1).get().getText();
          // Loop over all the parameters, skipping commas and stopping at ")"
          LinkedHashMap<String, Class<? extends Variable>> parameters = new LinkedHashMap<>();

          // Iterator variable needed outside of loop
          int i = 3;
          // First parameter
          boolean skipParams = statement.getChild(i).get().getText().equals(")");
          if (!skipParams) {
            for (; !statement.getChild(i - 1).get().getType().equals(")"); i += 2) {
              if (!statement.getChild(i).get().getType().equals("typed_indentifier")) {
                throw new UnexpectedNodeTypeException(
                    "typed_indentifier", statement.getChild(i).get().getType());
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
          map.set(fnName, vFunction);
          break;

        case "if_statement":
          Variable expression = computeExpression(statement.getChild(2).get(), map);
          if (!(expression instanceof VBoolean))
            throw new UnexpectedValueException(
                expression, "boolean", reverseTypeMap.get(expression.getClass()));

          boolean ifExpressionResult = ((VBoolean) expression).value;
          if (ifExpressionResult) {
            ArrayList<Node> ifScope = new ArrayList<>(statement.getChild(4).get().getChildren());
            ifScope.removeFirst();
            ifScope.removeLast();
            scope(ifScope, map);
          }
          break;
        case "fn_call":
          // TODO: look up the function with the function name
          String fn_name = statement.getChild(0).get().getText();
          Variable var = map.get(fn_name);
          if (var == null) throw new InvalidVariableException(fn_name);
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
            e.printStackTrace();
            System.exit(1);
          } catch (IncorrectReturnTypeException e) {
            System.err.println(
                "The function \""
                    + fn_name
                    + "\" expects a return type of \""
                    + e.expected
                    + "\" but got \""
                    + e.actual);
            e.printStackTrace();
            System.exit(1);
          }
          break;
        case "while_loop":
          ArrayList<Node> whileScope = new ArrayList<>(statement.getChild(4).get().getChildren());
          whileScope.removeFirst();
          whileScope.removeLast();
          Variable whileCondition = computeExpression(statement.getChild(2).get(), map);
          if (whileCondition instanceof VBoolean) {
            while (((VBoolean) whileCondition).value) {
              scope(whileScope, map);
              // Re-evaluate condition
              whileCondition = computeExpression(statement.getChild(2).get(), map);
            }
          }

          break;

        default:
          break;
      }
    }

    return new VUnit();
  }

  public Variable computeExpression(Node node, ScopedMemory memory)
      throws UnexpectedNodeTypeException,
          InvalidVariableException,
          UnexpectedValueException,
          VariableNotFound {
    if (!node.getType().equals("expression"))
      throw new UnexpectedNodeTypeException("expression", node.getType());

    Node child = node.getChild(0).get();
    switch (child.getType()) {
      case "number":
        Double value = Double.parseDouble(child.getText());
        return new VNumber(value);
      case "string":
        ArrayList<Node> stringContents = new ArrayList<>(child.getChildren());
        stringContents.removeFirst();
        stringContents.removeLast();
        String resultString = "";
        for (Node stringContentsNode : stringContents) {
          resultString += stringContentsNode.getText();
        }
        return new VString(resultString);

      case "boolean":
        return new VBoolean(child.getText().equals("true") ? true : false);

      case "identifier":
        return memory.get(child.getText());

      case "scope":
        List<Node> statements = new ArrayList<>(child.getChildren());
        statements.removeFirst();
        statements.removeLast();
        // Now statement is just a list of statements
        return scope(statements, memory);

      case "operation":
        Node operation = child.getChild(0).get();
        Node left = operation.getChild(0).get();
        Node operator = operation.getChild(1).get();
        Node right = operation.getChild(2).get();

        Variable leftValue = computeExpression(left, memory);
        Variable rightValue = computeExpression(right, memory);

        if (leftValue instanceof VNumber && rightValue instanceof VNumber) {
          double leftNum = ((VNumber) leftValue).value;
          double rightNum = ((VNumber) rightValue).value;

          switch (operation.getType()) {
            case "addition":
              return new VNumber(leftNum + rightNum);

            case "subtraction":
              return new VNumber(leftNum - rightNum);

            case "multiplication":
              return new VNumber(leftNum * rightNum);

            case "division":
              if (rightNum == 0) {
                throw new RuntimeException("Division by zero");
              }
              return new VNumber(leftNum / rightNum);
            case "less_than":
              return new VBoolean(leftNum < rightNum);
            case "less_than_or_equal_to":
              return new VBoolean(leftNum <= rightNum);
            case "greater_than":
              return new VBoolean(leftNum > rightNum);
            case "greater_than_or_equal_to":
              return new VBoolean(leftNum >= rightNum);
            case "equal_to":
              return new VBoolean(leftNum == rightNum);
            case "not_equal_to":
              return new VBoolean(leftNum != rightNum);

            default:
              throw new RuntimeException("Unknown operation: " + operator.getType());
          }
        } else if (leftValue instanceof VString) {
          VString leftValueString = (VString) leftValue;
          if (rightValue instanceof VString) {
            switch (operation.getType()) {
              case "addition" -> {
                return new VString(leftValueString.value + ((VString) rightValue).value);
              }

              case "equal_to" -> {
                return new VBoolean(leftValueString.value.equals(((VString) rightValue).value));
              }

              case "not_equal_to" -> {
                return new VBoolean(!leftValueString.value.equals(((VString) rightValue).value));
              }

              default -> {
                throw new RuntimeException("Unknown operation: " + operator.getType());
              }
            }
          } else if (rightValue instanceof VNumber) {
            switch (operation.getType()) {
              case "addition" -> {
                return new VString(leftValueString.value + ((VNumber) rightValue).value);
              }

              default -> {}
            }
          }
        } else {
          throw new RuntimeException("Operands must be numbers" + leftValue + rightValue);
        }
    }
    return new VNumber(10);
  }
}
