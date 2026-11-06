package MySh;

import java.util.List;
import java.util.ArrayList;

class Parser {
  List<String> parse(String input) {
    Mode mode = Mode.NORMAL_MODE;
    Mode backToMode = Mode.NORMAL_MODE;
    StringBuilder arg = new StringBuilder();
    List<String> args = new ArrayList<>();

    for (int i = 0; i < input.length(); i++) {
      char c = input.charAt(i);

      switch (mode) {
        case NORMAL_MODE:
          if (c == ' ' || c == '\n' || c == '\r') {
            args.add(arg.toString());
            arg.setLength(0);
          } else if (c == '\'') {
            mode = Mode.STRONG_MODE;
          } else if (c == '"') {
            mode = Mode.WEAK_MODE;
          } else if (c == '\\') {
            mode = Mode.ESCAPE_MODE;
            backToMode = Mode.NORMAL_MODE;
          } else {
            arg.append(c);
          }
          break;
        case STRONG_MODE:
        case WEAK_MODE:
        case ESCAPE_MODE:
          arg.append(c);
          mode = backToMode;
          backToMode = Mode.NORMAL_MODE;
          break;
        case REDIRECT_MODE:
          break;
      }
    }

    return args;
  }
}
