import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

public class Parser {
    private final BufferedReader input;
    private String currentLine;

    public Parser(String input) {
        this.input = new BufferedReader(new StringReader(input));
        this.advance();
    }

    public boolean hasMoreCommands() {
        return this.currentLine != null;
    }

    public void advance() {
        try {
            this.currentLine = input.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class Commands {
        private static Comment COMMENT = new Comment();

        public static Command comment() {
            return COMMENT;
        }

        public static Command ainst(String line) {
            return new AInstruction(line);
        }
    }

    public Command command() {
        if (currentLine == null) return null;

        final String trimmedLine = currentLine.trim();
        if (trimmedLine.startsWith("//") || trimmedLine.isEmpty()) {
            return Commands.comment();
        } else if (trimmedLine.startsWith("@")) {
            return Commands.ainst(trimmedLine);
        }
        return null;
    }

    public interface Command {
        public String compile();

    }

    private static class Comment implements Command {
        public String compile() {
            return "";
        }
    }

    private static class AInstruction implements Command {
        private final int value;

        public AInstruction(String line) {
            this.value = Integer.valueOf(line.substring(1));
        }

        public String compile() {
            String spacePaddedValue = String.format("%16s", Integer.toBinaryString(value));
            String zeroPaddedValue = spacePaddedValue.replace(' ', '0');
            return zeroPaddedValue + '\n';
        }
    }

    private class RawCommand implements Command {
        private final String rawCompiledForm;

        public RawCommand(String rawCompiledForm) {
            this.rawCompiledForm = rawCompiledForm;
        }

        public String compile() {
            return rawCompiledForm;
        }
    }
}