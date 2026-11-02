package MySh;

import java.util.List;
import java.io.IOException;
import java.util.ArrayList;

class UnixParser {
  private static Mode mode = Mode.NORMAL_MODE;
  private static Mode returnToMode = Mode.NORMAL_MODE;
  private static StringBuilder arg = new StringBuilder();
  private static List<String> args = new ArrayList<>();
  private static String[] chars = {};

  public List<String> parse(String s) throws IOException {
    chars = s.split("");

    for (int i = 0; i < chars.length; i++) {
      char c = chars[i].charAt(0);

      switch (mode) {
        case NORMAL_MODE:
          if (c == ' ' || c == '\n') {
            args.add(arg.toString());
            arg.setLength(0);

            if (c == '\n') {
              List<String> output = args;
              args = new ArrayList<>();
              return output;
            }
          } else {
            arg.append(c);
          }
          break;
        case STRONG_MODE:
          break;
        case WEAK_MODE:
          if (c == '\\') {
            mode = Mode.ESCAPE_MODE;
            returnToMode = Mode.ESCAPE_MODE;
          }
          break;
        case ESCAPE_MODE:
          mode = returnToMode;
          returnToMode = Mode.NORMAL_MODE;
          break;
        case REDIRECT_MODE:
          break;
      }
    }

    return args;
  }
}
