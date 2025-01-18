package net.borui.simpl.ui;

import com.formdev.flatlaf.FlatLightLaf;
import io.github.treesitter.jtreesitter.Language;
import io.github.treesitter.jtreesitter.Parser;
import io.github.treesitter.jtreesitter.Tree;
import net.borui.simpl.exceptions.InvalidVariableException;
import net.borui.simpl.exceptions.UnexpectedNodeTypeException;
import net.borui.simpl.exceptions.UnexpectedValueException;
import net.borui.simpl.exceptions.VariableNotFound;
import net.borui.simpl.interpreter.Interpreter;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.lang.foreign.Arena;
import java.lang.foreign.SymbolLookup;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class GUI extends JFrame {
  static GUI instance;
  public final JEditorPane codeArea = new JEditorPane();
  public final JTextArea output = new JTextArea();
  public Parser parser;
  public Path tempDir;

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

      extractLibTreeSitter(tempDir);

      SymbolLookup symbols = SymbolLookup.libraryLookup(extractNativeLibrary("simpl.dylib", tempDir), arena);
      Language language = Language.load(symbols, "tree_sitter_simpl");
      parser = new Parser(language);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    // https://www.formdev.com/flatlaf/
    FlatLightLaf.setup();

    try {
      UIManager.setLookAndFeel(new FlatLightLaf());
    } catch (Exception ex) {
      System.err.println("Failed to initialize LaF");
    }

    this.setLayout(new GridLayout(1, 2));
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

    JScrollPane outputScroll = new JScrollPane(output);
    outputScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

    // https://docs.oracle.com/javase/tutorial/uiswing/layout/gridbag.html
    GridBagConstraints editC = new GridBagConstraints();
    editC.gridy = 0;
    editC.gridx = 0;
    editC.weighty = 1;
    editC.weightx = 1;
    editC.fill = GridBagConstraints.BOTH;

    JPanel left = new JPanel(new GridBagLayout());
    left.add(scroll, editC);
    left.setPreferredSize(new Dimension(480, 480));

    JPanel right = new JPanel(new GridBagLayout());
    JPanel rightUp = new JPanel(new GridBagLayout());

    GridBagConstraints loadC = new GridBagConstraints();
    loadC.gridy = 0;
    loadC.gridx = 0;
    loadC.weightx = 0.3333;
    loadC.weighty = 0.5;
    loadC.insets = new Insets(4, 6, 4, 6);
    rightUp.add(load, loadC);

    GridBagConstraints saveC = new GridBagConstraints();
    saveC.gridy = 0;
    saveC.gridx = 1;
    saveC.weightx = 0.3333;
    saveC.weighty = 0.5;
    saveC.insets = new Insets(4, 6, 4, 6);
    rightUp.add(save, saveC);

    GridBagConstraints uninstallC = new GridBagConstraints();
    uninstallC.gridy = 0;
    uninstallC.gridx = 2;
    uninstallC.weightx = 0.3333;
    uninstallC.weighty = 0.5;
    uninstallC.insets = new Insets(4, 6, 4, 6);
    rightUp.add(uninstall, uninstallC);

    GridBagConstraints runC = new GridBagConstraints();
    runC.gridy = 1;
    runC.gridx = 1;
    runC.anchor = GridBagConstraints.PAGE_END;
    runC.weightx = 1;
    runC.weighty = 0.5;
    runC.insets = new Insets(4, 6, 4, 6);
    rightUp.add(run, runC);

    GridBagConstraints rightUpC = new GridBagConstraints();
    rightUpC.gridy = 0;
    rightUpC.gridx = 0;
    rightUpC.weighty = 0.2;
    rightUpC.weightx = 1;
    rightUpC.anchor = GridBagConstraints.PAGE_START;
    right.add(rightUp, rightUpC);

    JPanel rightDown = new JPanel(new GridBagLayout());

    JLabel outputLabel = new JLabel("                          Output:                          ");
    GridBagConstraints labelC = new GridBagConstraints();
    labelC.gridy = 0;
    labelC.gridx = 0;
    labelC.anchor = GridBagConstraints.PAGE_START;
    rightDown.add(outputLabel, labelC);

    GridBagConstraints scrollC = new GridBagConstraints();
    scrollC.weighty = 1;
    scrollC.weightx = 1;
    scrollC.insets = new Insets(0, 10, 0, 0);
    scrollC.fill = GridBagConstraints.BOTH;
    scrollC.gridy = 1;
    scrollC.gridx = 0;
    rightDown.add(outputScroll, scrollC);

    GridBagConstraints rightDownC = new GridBagConstraints();
    rightDownC.weighty = 0.9;
    rightDownC.gridy = 2;
    rightDownC.gridx = 0;
    rightDownC.fill = GridBagConstraints.BOTH;
    right.add(rightDown, rightDownC);

    this.add(new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, right));
    this.pack();
    this.setVisible(true);

    // Hook up output to GUI
    Interpreter.output = new GUIOut();
  }

  public static GUI getInstance() {
    if (instance == null) instance = new GUI();

    return instance;
  }

  /**
   * Extracts the resourcePath from within the jar file and puts it in tempDir.
   * Will reuse library
   * if already exists in tempDir.
   *
   * @param libName the path of the library inside the jar
   * @param tempDir the path to store the libraries
   * @return the path to the extracted library
   * @throws IOException cannot find the library
   */
  private static Path extractNativeLibrary(String libName, Path tempDir) throws IOException {
    // Gets library from inside the jar, which is put there by the maven build
    // config

    Path libPath = tempDir.resolve(libName);

    if (!Files.exists(libPath)) {
      // Prepare streaming data from jar
      try (InputStream libStream = GUI.class.getClassLoader().getResourceAsStream(libName)) {
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
    }

    return libPath;
  }

  private static void extractLibTreeSitter(Path tempDir) throws IOException {
    // Unable to load libtree-sitter at arbitrary path. It must only be loaded
    // at $CWD

    // Remove old libtree-sitter if exists

    // This is a file contenting a path to the last libtree-sitter location
    Path oldLibStore = tempDir.resolve("old-lib-store");
    Path oldLibPath;

    if (oldLibStore.toFile().exists()) {
      // Store file exists
      try (BufferedReader br = Files.newBufferedReader(oldLibStore)) {
        oldLibPath = Path.of(br.readLine());
        // Lib exists at path in the store file
        if (oldLibStore.toFile().exists()) {
          // Lib isn't in current dir
          if (!oldLibPath.equals(Path.of(System.getProperty("user.dir")))) {
            try {
              Files.delete(oldLibPath.resolve(System.mapLibraryName("tree-sitter")));
            } catch (NoSuchFileException e) {
              System.err.println("old libtree-sitter already deleted");
            }
          } else {
            // Lib is in current dir
            extractLibTreeSitterInternal();
            return;
          }
        }
        // Lib in store file doesn't exist. Do normal process
      }
    }
    // Store file doesn't exist. Do normal process

    try (BufferedWriter bw = Files.newBufferedWriter(oldLibStore)) {
      bw.write(System.getProperty("user.dir"));
    }

  }

  private static void extractLibTreeSitterInternal() {
    // Gets library from inside the jar, which is put there by the maven build
    // config
    String libName = System.mapLibraryName("tree-sitter");

    // CWD
    Path libPath = Path.of(System.getProperty("user.dir")).resolve(libName).toAbsolutePath();

    if (!Files.exists(libPath)) {

      // Prepare streaming data from jar
      // https://stackoverflow.com/questions/320542/how-to-get-the-path-of-a-running-jar-file
      try {
        String jarPath = new File(GUI.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getPath();
        try (JarFile jarFile = new JarFile(jarPath)) {
          InputStream libStream = resolveSymlinkIfExists(libName, jarFile);
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
        } catch (IOException e) {
          throw new RuntimeException(e);
        }

      } catch (URISyntaxException e) {
        throw new RuntimeException(e);
      }
    }
  }

  /**
   * Resolves the given path in the jar and account for symlink
   *
   * <p>
   * Code calling this MUST be in a jar file.
   *
   * @param filePath the name of the potential symlink
   * @return the InputStream of the file
   */
  private static InputStream resolveSymlinkIfExists(String filePath, JarFile jarFile) throws IOException {
    JarEntry entry = jarFile.getJarEntry(filePath);
    if (entry == null) {
      throw new IllegalArgumentException("File not found: " + filePath);
    }

    System.out.println(isSymlink(entry));
    if (!isSymlink(entry)) return jarFile.getInputStream(entry);
    JarEntry targetEntry;
    // Read the symlink content
    try (InputStream symlinkStream = jarFile.getInputStream(entry)) {
      String targetPath = new String(symlinkStream.readAllBytes()).trim();

      // Resolve the real file
      targetEntry = jarFile.getJarEntry(targetPath);
      if (targetEntry == null) {
        throw new IllegalArgumentException("Target file not found: " + targetPath);
      }
    }
    // Return an InputStream for the real file
    return jarFile.getInputStream(targetEntry);
  }

  /**
   * Checks if the file is a symlink based on its length Assumes symlinks have
   * significantly shorter
   * lengths compared to actual files
   *
   * @param entry The JarEntry to check for symlink.
   * @return true if the file is a symlink, false otherwise.
   */
  public static boolean isSymlink(JarEntry entry) {
    // Heuristic: Check the file size
    long length = entry.getSize();

    // Symlinks typically have smaller lengths (e.g., 0 or a few bytes) compared to
    // actual files
    // Adjust the threshold based on your JAR structure
    return length > 0 && length < 1023; // Assuming symlinks are smaller than 1023 bytes as that is the max path length
    // across all systems.
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

    StringBuilder program = new StringBuilder();
    try {
      Scanner myReader = new Scanner(file);
      while (myReader.hasNextLine()) {
        String data = myReader.nextLine();
        program.append(data).append("\n");
      }
      myReader.close();
    } catch (FileNotFoundException e) {
      System.err.println("Unable to read program.");
      throw new RuntimeException(e);
    }
    codeArea.setText(program.toString());
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
      throw new RuntimeException(e);
    }
  }

  public void uninstallButtonClicked() {
    // https://docs.oracle.com/javase/tutorial/uiswing/components/dialog.html
    Object[] options = {"Yes", "No",};
    int n = JOptionPane.showOptionDialog(this, """
        Are you sure you want to uninstall simpl?
        This will IMMEDIATELY remove supporting libraries and EXIT THE PROGRAM.\s
         The libraries will be reinstalled the next time you open SIMPLIDE.\
        """, "Uninstall Simpl", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[1]);
    // Accepted
    if (n == 0) {
      // Find current libtree-sitter location and delete it
      Path libTreeSitterPath = Path.of(System.getProperty("user.dir")).resolve(System.mapLibraryName("tree-sitter"));
      try {
        Files.delete(libTreeSitterPath);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
      deleteDir(tempDir.toFile());
      System.exit(0);
    }
  }

  // https://stackoverflow.com/questions/20281835/how-to-delete-a-folder-with-files-using-java
  private void deleteDir(File file) {
    File[] contents = file.listFiles();
    if (contents != null) {
      for (File f : contents) {
        deleteDir(f);
      }
    }
    file.delete();
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
      } catch (UnexpectedNodeTypeException | InvalidVariableException | UnexpectedValueException | VariableNotFound e) {
        throw new RuntimeException(e);
      }
    }
  }
}
