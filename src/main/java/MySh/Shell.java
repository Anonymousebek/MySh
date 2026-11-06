package MySh;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;

public final class Shell {
  private final InputStream in;
  private final PrintStream out;

  Shell(InputStream in, PrintStream out) {
    this.in = in;
    this.out = out;
  }

  private boolean isOpen = true;

  private void setRawMode() {
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

  private void execute(List<String> args) {
    if (args.size() == 0) {
      return;
    }

    String cmd = args.get(0);

    if (cmd.length() == 0) {
      return;
    }

    if (cmd.equals("exit")) {
      isOpen = false;
      return;
    } else if (cmd.equals("echo")) {
      if (args.size() == 1) {
        out.print("\r\n");
        return;
      }

      String content = String.join(" ", Arrays.copyOfRange(args.toArray(String[]::new), 1, args.size()));
      out.println("\r" + content);
    } else {
      out.println("\r" + cmd + ": command not found");
    }
  }

  private String[] getCompletionList() {
    return new String[] {};
  }

  private static StringBuilder input = new StringBuilder();

  public void run() {
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

          List<String> args = parser.parse(input.toString());

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
