package net.borui.simpl;

import net.borui.simpl.ui.GUI;

class Main {
  public static void main(String[] args) {
    GUI.getInstance();
    // String program = "";
    // try {
    // File myObj = new File(args[0]);
    // Scanner myReader = new Scanner(myObj);
    // while (myReader.hasNextLine()) {
    // String data = myReader.nextLine();
    // program += data + "\n";
    // }
    // myReader.close();
    // } catch (FileNotFoundException e) {
    // System.err.println("Unable to read program.");
    // throw new RuntimeException(e);
    // }
    // try (Arena arena = Arena.ofConfined()) {
    // SymbolLookup symbols =
    // SymbolLookup.libraryLookup(Path.of("./tree-sitter-simpl/simpl.dylib"),
    // arena);
    // Language language = Language.load(symbols, "tree_sitter_simpl");
    //
    // try (Parser parser = new Parser(language)) {
    // try (Tree tree = parser.parse(program).get()) {
    // try {
    // Interpreter.getInstance().scope(tree.getRootNode().getChildren());
    // } catch (UnexpectedNodeTypeException
    // | InvalidVariableException
    // | UnexpectedValueException e) {
    // throw new RuntimeException(e);
    // }
    // }
    // }
    // }
  }
}
