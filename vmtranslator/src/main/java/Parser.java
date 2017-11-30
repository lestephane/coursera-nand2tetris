import java.io.BufferedReader;
import java.io.IOException;
import java.util.logging.Logger;

public class Parser {
    private static final Logger LOGGER = Logger.getLogger( Parser.class.getName() );
    private final BufferedReader input;
    private String currentLine;

    Parser(BufferedReader r) {
        input = r;
        advance();
    }

    public void advance() {
        try {
            currentLine = input.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean hasMoreCommands() {
        return currentLine != null;
    }

    public Commands.Command command() {
        if (currentLine == null) return null;
        String originalLine =  currentLine.trim();
        LOGGER.fine(() -> "line: " + originalLine);
        if (originalLine.startsWith("//")) {
            return Commands.comment(originalLine);
        }
        String effectiveLine = trimOfWhitespaceAndComments(currentLine);
        return makeCommand(effectiveLine);
    }

    private String trimOfWhitespaceAndComments(String line) {
        int pos = line.indexOf("//");
        if (pos != -1) {
            return line.substring(0, pos).trim();
        } else {
            return line.trim();
        }
    }

    private Commands.Command makeCommand(String line) {
        if (line.matches("push (constant|local|argument|this|that|temp|static|pointer) \\d+")) {
            return Commands.commented(line, Commands.pushCommand(line));
        } else if (line.matches("pop (local|argument|this|that|temp|static|pointer) \\d+")) {
            return Commands.commented(line, Commands.popCommand(line));
        } else if (line.equals("add")) {
            return Commands.commented(line, Commands.addCommand());
        } else if (line.equals("sub")) {
            return Commands.commented(line, Commands.subCommand());
        } else if (line.equals("eq")) {
            return Commands.commented(line, Commands.eqCommand());
        } else if (line.equals("gt")) {
            return Commands.commented(line, Commands.gtCommand());
        } else if (line.equals("lt")) {
            return Commands.commented(line, Commands.ltCommand());
        } else if (line.equals("neg")) {
            return Commands.commented(line, Commands.negCommand());
        } else if (line.equals("not")) {
            return Commands.commented(line, Commands.notCommand());
        } else if (line.equals("and")) {
            return Commands.commented(line, Commands.andCommand());
        } else if (line.equals("or")) {
            return Commands.commented(line, Commands.orCommand());
        } else if (line.startsWith("label")) {
            return Commands.commented(line, Commands.labelCommand(line));
        } else if (line.startsWith("goto")) {
            return Commands.commented(line, Commands.goTo(line));
        } else if (line.startsWith("if-goto")) {
            return Commands.commented(line, Commands.ifGoto(line));
        } else if (line.startsWith("function")) {
            return Commands.commented(line, Commands.function(line));
        } else if (line.startsWith("return")) {
            return Commands.commented(line, new Commands.ReturnCommand());
        } else if (!line.isEmpty()){
            LOGGER.warning(()-> "unsupported: " + line);
            return Commands.comment("UNSUPPORTED: " + line);
        } else {
            return Commands.comment("");
        }
    }
}