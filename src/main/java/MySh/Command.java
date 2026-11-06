package MySh;

@FunctionalInterface
public interface Command {
  void run(String[] args);
}
