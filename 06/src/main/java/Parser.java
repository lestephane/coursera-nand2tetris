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

        final String trimmedLine = currentLine.trim();
        if (trimmedLine.startsWith("//") || trimmedLine.isEmpty()) {
            return Commands.comment();
        } else if (trimmedLine.startsWith("@")) {
            return Commands.ainst(trimmedLine);
        } else {
            return Commands.cinst(trimmedLine);
        }
    }

}