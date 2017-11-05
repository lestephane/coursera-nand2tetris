import java.io.*;

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
        final String line = trimOfWhitespaceAndComments(currentLine);

        if (line.startsWith("//") || line.isEmpty()) {
            return Commands.comment();
        } else if (line.startsWith("@")) {
            return Commands.ainst(line);
        } else if (line.startsWith("(") && line.endsWith(")")) {
            return Commands.label(line);
        } else {
            return Commands.cinst(line);
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