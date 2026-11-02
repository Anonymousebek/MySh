package MySh;

import java.io.BufferedReader;
import java.io.FilterReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;

public final class Shell {
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

  private static StringBuilder input = new StringBuilder();

  private class RawReader extends FilterReader {
    public RawReader(Reader in) {
      super(in);
    }

    @Override
    public int read() throws IOException {
      int c = super.read();
      switch (c) {
        case Signals.CTRL_C:
          return -1;
        case Signals.BACKSPACE1:
        case Signals.BACKSPACE2:
          if (input.length() > 0) {
            input.deleteCharAt(input.length() - 1);
            System.out.print("\b \b");
          }
          return 0;
        case Signals.TAB:
          System.out.write(Signals.BELL_SIGNAL);
          System.out.flush();
          return 0;
        case Signals.RETURN:
          input.append('\n');
          return (int) '\n';
        default:
          input.append((char) c);
          System.out.print((char) c);
          return c;
      }
    }
  }

  public void println(Object x) {
    System.out.print("\r\n");
    System.out.print(x);
    System.out.print("\r\n\n");
    System.out.print("$ ");
  }

  public void println(char c) {
    println(String.valueOf(c));
  }

  public void run() {
    setRawMode();

    UnixParser parser = new UnixParser();
    int c;

    try (Reader reader = new RawReader(new BufferedReader(new InputStreamReader(System.in)))) {
      System.out.print("$ ");
      while ((c = reader.read()) != -1) {
        if (c == '\n') {
          List<String> args = parser.parse(input.toString());
          input.setLength(0);
          println(args);
        }
      }
    } catch (IOException ex) {
      ex.printStackTrace();
    }
  }
}
