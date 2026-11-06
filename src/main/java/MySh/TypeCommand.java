package MySh;

class TypeCommand implements Command {
  @Override
  public void run(String[] arguments) {
    if (arguments.length == 1) {
      System.out.println("Error: not enough arguments");
      return;
    }

    String command = arguments[1];

    if (Shell.isBuiltInCommand(command)) {
      System.out.println(command + " is a shell builtin");
      return;
    }

    String cmdPath = Shell.locateExecutable(command);

    if (cmdPath == null) {
      System.out.println(command + ": not found");
      return;
    }

    System.out.println(command + " is " + cmdPath);
  }
}
