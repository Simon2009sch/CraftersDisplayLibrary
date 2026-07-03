# CraftersDisplayLibrary

## Build

**Do NOT build the project from the terminal or via shell commands.**

This project is built using **Maven through IntelliJ IDEA** only. The Maven
runtime is bundled inside IntelliJ and is not available as a standalone
`mvn` command on the system PATH. Attempting to run `mvn` from a terminal
will fail.

- Do not run `mvn compile`, `mvn package`, `mvn install`, etc.
- Do not attempt to invoke IntelliJ's bundled Maven from the command line.
- Compilation and building is handled by the developer inside IntelliJ.

If you need to verify that code compiles, just inform the user and let them
build it in IntelliJ themselves.
