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
        } else if (originalLine.matches("push (constant|local|argument|this|that|temp) \\d+")) {
            return Commands.commented(originalLine, Commands.pushCommand(effectiveLine));
        } else if (originalLine.matches("pop (local|argument|this|that|temp) \\d+")) {
            return Commands.commented(originalLine, Commands.popCommand(effectiveLine));
        } else if (originalLine.equals("add")) {
            return Commands.commented(originalLine, Commands.addCommand());
        } else if (originalLine.equals("sub")) {
            return Commands.commented(originalLine, Commands.subCommand());
        } else if (!originalLine.isEmpty()){
            return Commands.comment("UNSUPPORTED: " + originalLine);
        } else {
            return Commands.comment("");
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
