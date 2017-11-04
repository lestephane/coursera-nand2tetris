import java.io.*;

public class Parser {
    private final BufferedReader input;
    private String currentLine;

    public Parser(String input) {
        this(new BufferedReader(new StringReader(input)));
    }

    private Parser(File sourceFile) throws FileNotFoundException {
        this(new BufferedReader(new FileReader(sourceFile)));
    }

    private Parser(BufferedReader r) {
        this.input = r;
        this.advance();
    }

    public static Parser createParser(File sourceFile) {
        try {
            return new Parser(sourceFile);
        } catch (FileNotFoundException e) {
            throw new ParseException(e);
        }
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

    public Command command() {
        if (currentLine == null) return null;
        final String line = trimOfWhitespaceAndComments(currentLine);

        if (line.startsWith("//") || line.isEmpty()) {
            return Commands.comment();
        } else if (line.startsWith("@")) {
            return Commands.ainst(line);
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
}