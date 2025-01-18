# Setup

## libtree-sitter

### macOS and Linux

1. `cd` into `tree-sitter/` and run `make -j` to create the shared library

### Windows

1. (on Windows, run the following commands):

   ```sh
   # Set environment variables
   export CC=clang
   export WASM=OFF

   # Build the shared library
   cmake -S lib -B build/shared \
          -DBUILD_SHARED_LIBS=ON \
          -DCMAKE_BUILD_TYPE=Debug \
          -DCMAKE_COMPILE_WARNING_AS_ERROR=ON \
          -DTREE_SITTER_FEATURE_WASM=$WASM
    cmake --build build/shared --verbose
   ```

2. The file is in build/share.
   Rename it without the versions, e.g. `libtree-sitter.dll`.

> [!NOTE]
> Now, you should have a shared library in the root, under the name of
> `libtree-sitter` with an extension of `.so`, `.dylib` or `.dll`.
>
> This is the library powering the parsing of the text. By binding it to Java,
> we can call native functions (machine code) from a higher-level API. This
> library is in charge of reading the incoming text and parse it into tokens.

## Grammar

1. Install tree-sitter CLI from
   [here](https://github.com/tree-sitter/tree-sitter/releases/tag/v0.24.6)
   (tested using `0.24.3`).
2. `cd` into `tree-sitter-simpl/` and run
   ```sh
   tree-sitter generate
   tree-sitter build
   ```
   To generate and build the grammar.
   This will output another shared library, and should have the same extension
   as `libtree-sitter`.

> [!NOTE]
> While `libtree-sitter` parses the file into tokens, it doesn't know how to,
> because different programming languages provides different arrangements of the
> tokens. A piece of text that is valid in one language might not be in the
> other, or interpreted in different ways. Take the following code:
>
> ```rust
> fn foo() -> number {
>   return 1;
> }
> ```
>
> It can produce valid tokens for both rust and simpl, yet their structure would
> be different. This is because the `libtree-sitter` parses the input code and
> creates the syntax tree, while the grammar defines the rules for a specific
> language.

3.  Since the Java program only looks for `simpl.dylib`, you can rename the file
    extension to `.dylib`.
    If that doesn't work, you can go into
    `src/main/java/net/borui/simpl/Main.java`, search for
    `./tree-sitter-simpl/simpl.dylib`, and change that to the same extension as
    on `libtree-sitter`.

## Compiling the Interpreter

1. Install [maven](https://maven.apache.org)
2. There are two ways to proceed:
   - Run `./test.sh`
   - Or this two-step process:
     1. Run `maven package` once to compile
     2. Run `java -cp target/simpl-1.0-SNAPSHOT-jar-with-dependencies.jar
--enable-native-access=ALL-UNNAMED net.borui.simpl.Main`.
