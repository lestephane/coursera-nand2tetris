import java.io.StringWriter;

public class HackAssembler {
    private final Parser parser;

    public HackAssembler(String source) {
        parser = new Parser(source);
    }

    public String compile() {
        StringWriter sw = new StringWriter();
        while (parser.hasMoreCommands()) {
            Parser.Command command = parser.command();
            sw.append(command.compile());
            parser.advance();
        }
        return sw.toString();
    }
}