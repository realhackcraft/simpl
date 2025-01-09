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
  Made functions able to have return value.
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
