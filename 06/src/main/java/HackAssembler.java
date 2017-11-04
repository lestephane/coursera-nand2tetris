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
            String compiled = command.compile();
            assert compiled.isEmpty() | compiled.trim().length() == 16 : compiled;
            sw.append(compiled);
            parser.advance();
        }
        return sw.toString();
    }
}