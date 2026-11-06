package MySh;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public final class Shell {
  private Shell() {
  }

  private static InputStream in = System.in;
  private static PrintStream out = System.out;

  private static boolean isOpen = true;

  public final static Map<String, Command> builtInCommands = new HashMap<>() {
    {
      put("exit", (args) -> isOpen = false);
      put("echo", (args) -> {
        if (args.length == 1) {
          out.print("\r\n");
          return;
        }

        String content = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        out.println("\r" + content);
      });
      put("type", new TypeCommand());
    }
  };

  public static boolean isBuiltInCommand(String cmd) {
    return builtInCommands.containsKey(cmd);
  }

  private static void setRawMode() {
    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      try {
        String[] command = { "/bin/sh", "-c", "stty sane </dev/tty" };
        new ProcessBuilder().command(command).inheritIO().start().waitFor();
      } catch (Exception ex) {
        ex.printStackTrace();
      }
    }));

    try {
      String[] command = { "/bin/sh", "-c", "stty -echo raw </dev/tty" };
      new ProcessBuilder().command(command).inheritIO().start().waitFor();
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  static String locateExecutable(String cmd) {
    String[] syspath = System.getenv("PATH").split(File.pathSeparator);
    for (String path : syspath) {
      String cmdPath = path + File.separatorChar + cmd;
      if (Files.isExecutable(Path.of(cmdPath))) {
        return cmdPath;
      }
    }
    return null;
  }

  private static void execute(String[] args) {
    if (args.length == 0) {
      return;
    }

    String cmd = args[0];

    if (cmd.length() == 0) {
      return;
    }

    boolean isBuildIn = builtInCommands.containsKey(cmd);

    if (isBuildIn) {
      Command command = builtInCommands.get(cmd);
      command.run(args);
    } else {
      out.println("\r" + cmd + ": command not found");
    }
  }

  private static String[] getCompletionList() {
    return new String[] {};
  }

  private static StringBuilder input = new StringBuilder();

  public static void run(InputStream stdin, PrintStream stdout) {
    in = stdin;
    out = stdout;

    setRawMode();

    Parser parser = new Parser();

    int c;

    out.print("$ ");
    out.flush();

    try {
      while (isOpen) {
        c = in.read();

        if (c == -1) {
          isOpen = false;
          break;
        }

        if (c == Chars.CTRL_D) {
          out.print(Chars.CTRL_D);
          out.close();
          break;
        }

        if (c == Chars.CTRL_C) {
          out.print("\r\n\n$ ");
          input.setLength(0);
          continue;
        }

        if (c == Chars.BACKSPACE1 || c == Chars.BACKSPACE2) {
          if (input.length() > 0) {
            input.deleteCharAt(input.length() - 1);
            out.print("\b \b");
          }
          continue;
        }

        if (c == Chars.RETURN) {
          out.print("\r\n");
          input.append('\n');

          String[] args = parser.parse(input.toString());

          input.setLength(0);

          execute(args);

          if (!isOpen) {
            out.print(Chars.CTRL_D);
            break;
          }

          out.print("\r\n" + "$ ");
        } else if (c == Chars.TAB) {
          if (getCompletionList().length > 0) {
            out.write(Chars.BELL_SIGNAL);
            out.flush();
          } else {
            String spaces = " ";
            out.print(spaces);
            input.append(spaces);
          }
        } else {
          out.print((char) c);
          input.append((char) c);
        }
      }
    } catch (IOException ex) {
      ex.printStackTrace();
    } finally {
      try {
        in.close();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }
}
