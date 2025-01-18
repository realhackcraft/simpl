package net.borui.simpl.ui;

import io.github.treesitter.jtreesitter.Language;
import io.github.treesitter.jtreesitter.Parser;
import io.github.treesitter.jtreesitter.Tree;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
    if (instance == null)
      instance = new GUI();

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

      extractLibTreeSitter(tempDir);

      SymbolLookup symbols = SymbolLookup.libraryLookup(extractNativeLibrary("simpl.dylib", tempDir), arena);
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
    int n = JOptionPane.showOptionDialog(
        this,
        "Are you sure you want to uninstall simpl?\n"
            + "This will IMMEDIATELY remove supporting libraries and EXIT THE PROGRAM. \n"
            + " The libraries will be reinstalled the next time you open SIMPLIDE.",
        "Uninstall Simpl",
        JOptionPane.YES_NO_OPTION,
        JOptionPane.QUESTION_MESSAGE,
        null,
        options,
        options[1]);
    // Accepted
    if (n == 0) {
      // Find current libtree-sitter location and delete it
      Path libTreeSitterPath = Path.of(System.getProperty("user.dir")).resolve(System.mapLibraryName("tree-sitter"));
      try {
        Files.delete(libTreeSitterPath);
      } catch (IOException e) {
        e.printStackTrace();
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
      } catch (UnexpectedNodeTypeException
          | InvalidVariableException
          | UnexpectedValueException
          | VariableNotFound e) {
        e.printStackTrace();
      }
    }
  }

  /**
   * Extracts the resourcePath from within the jar file and puts it in tempDir.
   * Will reuse library
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

  private static Path extractLibTreeSitter(Path tempDir) throws IOException {
    // Unable to load libtree-sitter at arbitrary path. It must only be loaded
    // at $CWD

    // Remove old libtree-sitter if exists

    // This is a file contening a path to the last libtree-sitter location
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
            return extractLibTreeSitterInternal();
          }
        }
        // Lib in store file doesn't exist. Do normal process
      }
    }
    // Store file doesn't exist. Do normal process

    Path libPath = extractLibTreeSitterInternal();

    try (BufferedWriter bw = Files.newBufferedWriter(oldLibStore)) {
      bw.write(System.getProperty("user.dir"));
    }

    return libPath;
  }

  private static Path extractLibTreeSitterInternal() throws IOException {
    // Gets library from inside the jar, which is put there by the maven build
    // config
    String libName = System.mapLibraryName("tree-sitter");

    // CWD
    Path libPath = Path.of(System.getProperty("user.dir")).resolve(libName).toAbsolutePath();

    if (!Files.exists(libPath)) {

      // Prepare streaming data from jar
      // https://stackoverflow.com/questions/320542/how-to-get-the-path-of-a-running-jar-file
      try {
        String jarPath = new File(GUI.class.getProtectionDomain().getCodeSource().getLocation().toURI())
            .getPath();
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
          e.printStackTrace();
        }

      } catch (URISyntaxException e) {
        e.printStackTrace();
      }
    }

    return libPath;
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
  private static InputStream resolveSymlinkIfExists(String filePath, JarFile jarFile)
      throws IOException {
    JarEntry entry = jarFile.getJarEntry(filePath);
    if (entry == null) {
      throw new IllegalArgumentException("File not found: " + filePath);
    }

    System.out.println(isSymlink(jarFile, entry));
    if (!isSymlink(jarFile, entry))
      return jarFile.getInputStream(entry);
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
   * @param jarFile   The JAR file.
   * @param entryPath The path of the entry inside the JAR.
   * @return true if the file is a symlink, false otherwise.
   * @throws Exception if an error occurs while reading the entry.
   */
  public static boolean isSymlink(JarFile jarFile, JarEntry entry) {
    // Heuristic: Check the file size
    long length = entry.getSize();

    // Symlinks typically have smaller lengths (e.g., 0 or a few bytes) compared to
    // actual files
    // Adjust the threshold based on your JAR structure
    return length > 0
        && length < 1023; // Assuming symlinks are smaller than 1023 bytes as that is the max path length
    // across all systems.
  }
}
