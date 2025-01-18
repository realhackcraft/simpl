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
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.foreign.Arena;
import java.lang.foreign.SymbolLookup;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
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
  public Parser parser;
  public Path tempDir;

  public static GUI getInstance() {
    if (instance == null) instance = new GUI();
    return instance;
  }

  private GUI() {
    // Initialize cached parser
    Arena arena = Arena.global();
    try {
      // https://www.baeldung.com/java-temp-directories
      Path systemTempDir = Paths.get(System.getProperty("java.io.tmpdir"));
      tempDir = systemTempDir.resolve("net.borui.simpl");
      if (!Files.exists(tempDir)) {
        tempDir = Files.createDirectory(systemTempDir.resolve("net.borui.simpl"));
      }

      // Allow java-tree-sitter library to discover native binary required
      // without hard-coding path
      System.setProperty(
          "java.library.path",
          System.getProperty("java.library.path") + ":" + tempDir.toAbsolutePath());

      extractNativeLibrary("libtree-sitter.dylib", tempDir);

      SymbolLookup symbols =
          SymbolLookup.libraryLookup(extractNativeLibrary("simpl.dylib", tempDir), arena);
      Language language = Language.load(symbols, "tree_sitter_simpl");
      parser = new Parser(language);
    } catch (IOException e) {
      e.printStackTrace();
      System.exit(-1);
    }

    this.setLayout(new BorderLayout());
    this.setTitle("Simpl IDE (SIMPLIDE)");
    this.setDefaultCloseOperation(EXIT_ON_CLOSE);
    JButton run = new JButton("Run");
    JButton load = new JButton("Load");
    JButton save = new JButton("Save");
    JButton uninstall = new JButton("Uninstall");

    run.addActionListener(new RunButtonListener());
    load.addActionListener(new LoadButtonListener());
    save.addActionListener(new SaveButtonListener());
    uninstall.addActionListener(new UninstallButtonListener());

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
    rightUp.add(uninstall);
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

  public void uninstallButtonClicked() {
    // https://docs.oracle.com/javase/tutorial/uiswing/components/dialog.html
    Object[] options = {
      "Yes", "No",
    };
    int n =
        JOptionPane.showOptionDialog(
            this,
            "Are you sure you want to uninstall simpl?\n"
                + "This will remove supporting libraries after SIMPLIDE is closed, and will be"
                + " reinstalled the next time you open SIMPLIDE.",
            "Uninstall Simpl",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null,
            options,
            options[1]);
    // Accepted
    if (n == 0) {
      // Do not allow temporary files to live after program termination
      tempDir.toFile().deleteOnExit();
    }
  }

  /**
   * Runs a give piece of simpl source code
   *
   * @param code the code to execute
   */
  public void run(String code) {
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

  /**
   * Extracts the resourcePath from within the jar file and puts it in tempDir. Will reuse library
   * if already exists in tempDir.
   *
   * @param libName the path of the library inside the jar
   * @param tempDir the path to store the libraries
   * @return the path to the extracted library
   * @throws IOException
   */
  private static Path extractNativeLibrary(String libName, Path tempDir) throws IOException {
    // Gets library from insude the jar, which is put there by the maven build
    // config

    Path libPath = tempDir.resolve(libName);

    if (!Files.exists(libPath)) {
      // Prepare streaming data from jar
      InputStream libStream = GUI.class.getClassLoader().getResourceAsStream(libName);
      if (libStream == null) {
        throw new FileNotFoundException("Library " + libName + " not found in jar.");
      }

      // Write binary library inside the jar to outside as it is streamed in
      try (OutputStream out = Files.newOutputStream(libPath)) {
        byte[] buffer = new byte[8192];
        int bytesRead;
        while ((bytesRead = libStream.read(buffer)) != -1) {
          out.write(buffer, 0, bytesRead);
        }
      }

      // Allow execution
      libPath.toFile().setExecutable(true);
    }

    return libPath;
  }
}
