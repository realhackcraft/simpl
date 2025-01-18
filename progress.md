Sources:

1. https://stackoverflow.com/questions/215497/what-is-the-difference-between-public-protected-package-private-and-private-in
   Helped me understand how I could allow inheritance of methods while keeping them private.
2. https://www.baeldung.com/java-temp-directories
   Helped me create folders in a temporary directory, so I can extract the libraries there.
3. https://www.formdev.com/flatlaf/
   A library to create a nice look and feel (UI style) that is cross-platform. I used this mainly to fix JSplitPane's
   separator not fully rendering on the macOS look and feel.
4. https://docs.oracle.com/javase/tutorial/uiswing/layout/gridbag.html
   Helped me understand and use ridBagLayouts, which allowed for a more flexible UI.
5. https://stackoverflow.com/questions/320542/how-to-get-the-path-of-a-running-jar-file
   I used this to store library files in the jar and copy them to the temp file location.
6. https://stackoverflow.com/questions/40255039/how-to-choose-file-in-java
   I used this to make a native file picker menu for save and load, because JFileChooser doesn't look good.
7. https://docs.oracle.com/javase/tutorial/uiswing/components/dialog.html
   I used this to create a confirmation pop up when uninstalling libraries.
8. https://stackoverflow.com/questions/20281835/how-to-delete-a-folder-with-files-using-java
   This outlined how to delete a folder with files in them, since Java refuses to delete them.
9. https://github.com/tree-sitter/tree-sitter-rust/blob/master/grammar.js
   I took inspiration from the Rust tree-sitter repo to understand how they parsed strings.
10. https://tree-sitter.github.io/tree-sitter/
    I used this library to do the parsing of the language. It is a native library with a Java binding, which means that
    it runs fast and uses less memory. It also simplifies the task of writing grammar for the language since it's
    well-thought-out as it is made by people working at GitHub.

Daily log:

- 2024-10-21:
  Came up with the idea of an interpreted multithreaded language with a simple
  DX.
  I chose tree-sitter as a parser because I thought it would be easy to bind
  with Java, performant, and also because I'm not very familiar with other tools
  such as LLVM.
- 2024-10-22:
  Tried to get tree-sitter working.
  I could compile the grammar but the Java tree-sitter binding wasn't accepting
  it.
- 2024-10-28:
  Tried to get tree-sitter working.
  I asked on the tree-sitter repo on GitHub about the Java binding.
  The thread is
  [here](https://github.com/tree-sitter/tree-sitter/discussions/3841).
- 2024-10-30:
  I got a response and figured out how the binding worked.
  I was expecting `libtree-sitter` to be built into the Java binding (as with
  the python binding, which I used for last year's project), but apparently not.
- 2024-11-08:
  I asked on
  [GitHub](https://github.com/tree-sitter/tree-sitter/discussions/3894) (once
  again) for some help in getting the `libtree-sitter` library.
  Experimentation over the last several days suggested that the releases in the
  GitHub repo was for the CLI and not the shared library.
- 2024-11-09:
  I got a hint from the thread that I should read the GitHub actions pipeline,
  as there was a script made to automate the compilation and testing of the
  library on different platforms.
  After reading the horrible syntax of GitHub Actions, I figured out the correct
  command to run, and got my hands on a `libtree-sitter` shared library.
- 2024-11-10:
  Figured out the path to put the library.
  The installation script suggested putting it in `/usr/share/`, but on my
  system, I don't have write access there.
  As a test, I ran
  `System.out.println(System.getProperty("java.library.path"));` to see where
  Java was looking for shared libraries.
  I found `.`, which indicates the current working directory, a.k.a.
  the folder that the `java` CLI is called.
  Since I'm calling the Java interpreter in the project root, that is where the
  `libtree-sitter` file should be put.
  After writing a simple test, tree-sitter could parse and tokenize a simple
  piece of code.
- 2024-11-15:
  Added numerical variables and a memory to store the variables.
  Also added a print statement to debug better.
- 2024-11-17:
  Added expressions in the form of `+`, `-`, `*` and `/`.
  I had to make a recursive method called `compute_expression` that computes any
  chain of operations.
  PEMDAS IS NOT IMPLEMENTED, IT LOOPS THROUGH FROM LEFT TO RIGHT.
  I had to change the print statement from accepting a number to an expression
  as then, I could print the result of any operations.
- 2024-11-18:
  Made the loader code read from a file instead of hard-coding the simpl code in
  a String.
- 2024-11-20:
  Changed the file path to be read from `args` instead of hard-coded.
- 2024-11-23:
  Implemented scopes, which are just a confinement of the lifetime of variables
  (variables declared in them are erased after the end of the scope).
  This is a building block to many future features, such as functions, if
  statements, while loops, and the new and exciting `batch` loop.
- 2024-11-27:
  Started writing functions.
  They are just variables but have a special `run` method which can execute
  scopes.
- 2024-12-18:
  Made functions able to have return value (although they aren't passed to the caller).
- 2024-12-28:
  Finished writing function declaration parsing.
- 2024-12-29:
  Changed how the VFunction stores the return type from diamond operator to a
  constructor parameter.
- 2024-12-31:
  Finished writing function call.
  Now functions can be declared and called.
- 2025-01-07:
  Added booleans and comparisons.
- 2025-01-08:
  Added GUI, added if statements.
- 2025-01-09:
  Fixed scopes not erasing bug and functions not having recursion bug.
- 2025-01-10:
  Planned ScopedMemory class for better memory structure and avoid cloning data
  for each scope. It should contain a hash map of strings to variables and a pointer to a potential parent ScopedMemory.
- 2025-01-13:
  Finish implementing ScopedMemory
- 2025-01-15:
  Added strings. I had to look at the tree-sitter-rust's implementation of the grammar because I could not figure out
  the way to parse strings myself.
- 2025-01-17:
  Added the shared libs inside the jar.
  On IDE boot, copy the libs to a usable location to actually run it.
  For libtree-sitter, do some additional logic as it can only be placed at the
  CWD (current working directory, aka where the jar file is being executed)
- 2025-01-17:
  Added uninstallation button to remove libraries.
- 2025-01-18:
  Account for when libtree-sitter Is stored in the jar as a symlink.
- 2025-01-18:
  Created arrays, methods, and allowed functions to return stuff for real. To create the methods, I had to make a new
  type of function called built-in functions, which instead of running a scope, can run normal java code behind the
  scene. A method `array.length()` is equivalent to built-in method `length(array)`.
- 2025-01-18:
  Implemented assignment to finally fix uncanny way of modifying variables (was
  `let a = 1; let a = 2;` to reassign variable, now `let a = 1; a = 2`)
- 2025-01-18:
  Added a library for a more consistent and modern look and feel of the UI.