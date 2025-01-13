package net.borui.simpl.ui;

import io.github.treesitter.jtreesitter.Language;
import io.github.treesitter.jtreesitter.Parser;
import io.github.treesitter.jtreesitter.Tree;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.foreign.Arena;
import java.lang.foreign.SymbolLookup;
import java.nio.file.Path;
import java.util.Scanner;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import net.borui.simpl.exceptions.InvalidVariableException;
import net.borui.simpl.exceptions.UnexpectedNodeTypeException;
import net.borui.simpl.exceptions.UnexpectedValueException;
import net.borui.simpl.exceptions.VariableNotFound;
import net.borui.simpl.interpreter.Interpreter;

/** GUI */
public class GUI extends JFrame {
  public JEditorPane codeArea = new JEditorPane();
  public JTextArea output = new JTextArea();

  static GUI instance;

  public static GUI getInstance() {
    if (instance == null)
      instance = new GUI();
    return instance;
  }

  private GUI() {
    this.setLayout(new BorderLayout());
    this.setTitle("Simpl IDE (SIMPLIDE)");
    this.setDefaultCloseOperation(EXIT_ON_CLOSE);
    JButton run = new JButton("Run");
    JButton load = new JButton("Load");
    JButton save = new JButton("Save");

    run.addActionListener(new RunButtonListener());
    load.addActionListener(new LoadButtonListener());
    save.addActionListener(new SaveButtonListener());

    JScrollPane scroll = new JScrollPane(codeArea);
    scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
    scroll.setPreferredSize(new Dimension(800, 600));
    scroll.setMinimumSize(new Dimension(10, 10));

    JScrollPane outputScroll = new JScrollPane(output);
    outputScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
    outputScroll.setPreferredSize(new Dimension(250, 145));
    outputScroll.setMinimumSize(new Dimension(10, 10));

    JPanel left = new JPanel(new BorderLayout(15, 15));
    left.add(scroll, BorderLayout.WEST);

    JPanel right = new JPanel(new BorderLayout());
    JPanel rightUp = new JPanel();
    rightUp.add(load);
    rightUp.add(save);
    right.add(rightUp, BorderLayout.NORTH);
    JPanel rightMiddle = new JPanel();
    rightMiddle.add(run);
    right.add(rightMiddle, BorderLayout.CENTER);

    JPanel rightDown = new JPanel(new BorderLayout());
    JLabel outputLabel = new JLabel("Output:");
    rightDown.add(outputLabel, BorderLayout.NORTH);
    rightDown.add(outputScroll, BorderLayout.SOUTH);
    right.add(rightDown, BorderLayout.SOUTH);

    this.add(left, BorderLayout.WEST);
    this.add(right, BorderLayout.EAST);
    this.pack();
    this.setVisible(true);

    // Hook up outout to GUI
    Interpreter.output = new GUIOut();
  }

  public void runButtonClicked() {
    output.setText("");
    run(codeArea.getText());
  }

  // Partially taken from
  // https://stackoverflow.com/questions/40255039/how-to-choose-file-in-java
  public void loadButtonClicked() {
    FileDialog dialog = new FileDialog(this, "Select .simpl File to Edit");
    dialog.setMode(FileDialog.LOAD);
    dialog.setVisible(true);
    File file = new File(dialog.getDirectory(), dialog.getFile());
    dialog.dispose();

    String program = "";
    try {
      Scanner myReader = new Scanner(file);
      while (myReader.hasNextLine()) {
        String data = myReader.nextLine();
        program += data + "\n";
      }
      myReader.close();
    } catch (FileNotFoundException e) {
      System.err.println("Unable to read program.");
      e.printStackTrace();
    }
    codeArea.setText(program);
  }

  // Partially taken from
  // https://stackoverflow.com/questions/40255039/how-to-choose-file-in-java
  public void saveButtonClicked() {
    FileDialog dialog = new FileDialog(this, "Save .simpl Code");
    dialog.setMode(FileDialog.SAVE);
    dialog.setVisible(true);
    File file = new File(dialog.getDirectory(), dialog.getFile());
    dialog.dispose();

    try {
      FileWriter fWriter = new FileWriter(file);
      fWriter.write(codeArea.getText());
      fWriter.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static void run(String code) {
    try (Arena arena = Arena.ofConfined()) {
      SymbolLookup symbols = SymbolLookup.libraryLookup(Path.of("./tree-sitter-simpl/simpl.dylib"), arena);
      Language language = Language.load(symbols, "tree_sitter_simpl");

      try (Parser parser = new Parser(language)) {
        try (Tree tree = parser.parse(code).get()) {
          try {
            Interpreter.getInstance().scope(tree.getRootNode().getChildren());
          } catch (UnexpectedNodeTypeException
              | InvalidVariableException
              | UnexpectedValueException
              | VariableNotFound e) {
            e.printStackTrace();
          }
        }
      }
    }
  }
}
