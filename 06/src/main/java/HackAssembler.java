import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HackAssembler {
    private final Parser parser;

    public HackAssembler(String source) {
        parser = new Parser(source);
    }

    public HackAssembler(File sourceFile) {
        parser = Parser.createParser(sourceFile);
    }

    public void compileTo(Writer w) throws IOException {
        SymbolTable symbolTable = new SymbolTable();
        while (parser.hasMoreCommands()) {
            Commands.Command command = parser.command();
            String compiled = command.compile(symbolTable);
            assert compiled.isEmpty() | compiled.trim().length() == 16 : compiled;
            w.write(compiled);
            parser.advance();
        }
    }

    public static void main(String[] args) throws IOException {
        Logger log = Logger.getLogger(HackAssembler.class.getSimpleName());
        log.setLevel(Level.INFO);
        log.info(() -> "source file:" + args[0]);
        String target = args[0].replace(".asm", ".hack");
        log.info(() -> "target file:" + target);
        FileWriter w = new FileWriter(target);
        new HackAssembler(new File(args[0])).compileTo(w);
        w.close();
    }
}