import java.io.*;
import java.util.logging.Logger;

public class Parser {
    private final BufferedReader input;
    private String currentLine;

    private Parser(BufferedReader r) {
        this.input = r;
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

    public Commands.Command command() {
        if (currentLine == null) return null;
        final String originalLine =  currentLine.trim();
        final String effectiveLine = trimOfWhitespaceAndComments(currentLine);

        Logger log = Logger.getLogger(VmTranslator.class.getSimpleName());
        log.info(() -> "line: " + originalLine);
        if (originalLine.startsWith("//")) {
            return Commands.comment(originalLine);
        } else if (originalLine.startsWith("push constant")) {
            return Commands.commented(originalLine, Commands.pushConstant(effectiveLine));
        } else if (originalLine.matches("pop (local|argument|this|that) \\d+")) {
            return Commands.commented(originalLine, Commands.popCommand(effectiveLine));
        } else {
            return Commands.comment("UNSUPPORTED: " + originalLine);
        }
    }

    private String trimOfWhitespaceAndComments(final String line) {
        int pos = line.indexOf("//");
        if (pos != -1) {
            return line.substring(0, pos).trim();
        } else {
            return line.trim();
        }
    }

    public interface Source {
        Parser makeParser();
    }

    public static class StringSource implements Source {
        private final String input;

        public StringSource(String input) {
            this.input = input;
        }

        public Parser makeParser() {
            return new Parser(new BufferedReader(new StringReader(input)));
        }
    }

    public static class FileSource implements Source {
        private final File input;

        public FileSource(File input) {
            this.input = input;
        }

        public Parser makeParser() {
            try {
                return new Parser(new BufferedReader(new FileReader(input)));
            } catch (FileNotFoundException e) {
                throw new ParseException(e);
            }
        }
    }
}
