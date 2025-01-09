#! /bin/bash

mvn package
java -cp target/simpl-1.0-SNAPSHOT-jar-with-dependencies.jar --enable-native-access=ALL-UNNAMED net.borui.simpl.Main ./tree-sitter-simpl/test.simpl
