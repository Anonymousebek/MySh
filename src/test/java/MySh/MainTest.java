package MySh;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

public class MainTest {
  @Test
  void shouldPrintPrompt_whenStarted() {
    ProcessBuilder pb = new ProcessBuilder().command("java", "-cp", "target/MySh-1.0-SNAPSHOT.jar", "MySh.Main");
    StringBuilder outputBuilder = new StringBuilder();
    int c;

    try {
      Process proc = pb.start();

      OutputStreamWriter writer = new OutputStreamWriter(proc.getOutputStream());
      writer.write(Chars.CTRL_D);
      writer.flush();

      InputStream in = proc.getInputStream();

      while ((c = in.read()) != -1) {
        outputBuilder.append((char) c);
      }

      String actual = outputBuilder.toString();
      String expected = "$ ";
      String msg = String.format("Expected to start with %s, but actual is %s", expected, actual);

      assertTrue(actual.startsWith(expected), msg);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Test
  void shouldPrintPrompt_afterEachCommand() {
    ProcessBuilder pb = new ProcessBuilder().command("java", "-cp", "target/MySh-1.0-SNAPSHOT.jar", "MySh.Main");

    try {
      Process proc = pb.start();
      BufferedReader buffer = new BufferedReader(new InputStreamReader(proc.getInputStream()));

      String text1 = "\n";
      String text2 = "test 2\n";
      String text3 = "line 3\n";

      OutputStreamWriter writer = new OutputStreamWriter(proc.getOutputStream());
      writer.write(text1);
      writer.write(text2);
      writer.write(text3);
      writer.write(Chars.CTRL_D);
      writer.flush();

      String line1 = buffer.readLine();
      String line2 = buffer.readLine();
      String line3 = buffer.readLine();

      assertEquals("$ ", line1);
      assertEquals("test 2", line2);
      assertEquals("line 3", line3);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Test
  void shouldClose_afterExitCommand() {
    ProcessBuilder pb = new ProcessBuilder().command("java", "-cp",
        "target/MySh-1.0-SNAPSHOT.jar", "MySh.Main");

    try {
      Process proc = pb.start();

      OutputStreamWriter writer = new OutputStreamWriter(proc.getOutputStream());
      writer.write("exit\r");
      writer.flush();

      boolean exited = proc.waitFor(3, java.util.concurrent.TimeUnit.SECONDS);

      if (!exited) {
        proc.destroyForcibly();
        throw new AssertionError("Expected shell to close after exit command");
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Test
  void shouldReportError_forUnknownCommand() {
    ProcessBuilder pb = new ProcessBuilder().command("java", "-cp",
        "target/MySh-1.0-SNAPSHOT.jar", "MySh.Main");
    StringBuilder outputBuilder = new StringBuilder();
    int c;

    try {
      Process proc = pb.start();
      String cmd = "unknown";

      InputStreamReader reader = new InputStreamReader(proc.getInputStream());

      try (OutputStream os = proc.getOutputStream()) {
        os.write((cmd + "\r").getBytes());
        os.write(Chars.CTRL_D);
      }

      while ((c = reader.read()) != -1) {
        outputBuilder.append((char) c);
      }

      String expected = String.format("%s: command not found", cmd);
      String message = String.format("Expected output '%s', but was '%s'", expected, outputBuilder.toString());

      assertTrue(outputBuilder.toString().contains(expected), message);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
