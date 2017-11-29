import java.util.function.BiConsumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HackAssembler {
    private final Parser.Source parserSource;

    public HackAssembler(String source) {
        parserSource = new Parser.StringSource(source);
    }

    public HackAssembler(File sourceFile) {
        parserSource = new Parser.FileSource(sourceFile);
    }

    public void compileTo(Writer output) throws IOException {
        SymbolTable symbolTable = new SymbolTable();
        gatherSymbols(symbolTable);
        performCompilation(symbolTable, output);
    }

    private void gatherSymbols(SymbolTable symbolTable) {
        forEachCommand((pc, cmd) -> {
            if (cmd instanceof Commands.Label) {
                String l = ((Commands.Label) cmd).label();
                symbolTable.addSymbol(l, pc.value());
            }
        });
    }

    private void performCompilation(SymbolTable symbolTable, Writer output) {
        forEachCommand((pc, cmd) -> {
            String compiled = cmd.compile(pc, symbolTable);
            assert compiled.isEmpty() | compiled.trim().length() == 16 : compiled;
            try {
                output.write(compiled);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
    }

    private void forEachCommand(BiConsumer<ProgramCounter, Commands.Command> cmd) {
        Parser parser = parserSource.makeParser();
        ProgramCounter pc = new ProgramCounter();
        while (parser.hasMoreCommands()) {
            Commands.Command c = parser.command();
            cmd.accept(pc, c);
            if (c.isInstruction()) {
                pc.increment();
            }
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