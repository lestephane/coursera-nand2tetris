import java.io.*;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class VMTranslator {
    private final Parser.Source parserSource;
    public VMTranslator(String input) {
        parserSource = new Parser.StringSource(input);
    }

    public VMTranslator(File input) {
        parserSource = new Parser.FileSource(input);
    }

    public void translateTo(String compilationUnitName, Writer output) {
        CodeWriter codeOutput = new CodeWriter(compilationUnitName, output);
        try {
            performTranslationTo(codeOutput);
        } finally {
            codeOutput.close();
        }
    }

    private void performTranslationTo(CodeWriter output) {
        forEachCommand((cmd) -> {
            try {
                cmd.translateTo(output);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
    }

    private void forEachCommand(Consumer<Commands.Command> cmd) {
        Parser parser = parserSource.makeParser();
        while (parser.hasMoreCommands()) {
            Commands.Command c = parser.command();
            if (c != null) {
                cmd.accept(c);
            }
            parser.advance();
        }
    }

    public static void main(String[] args) throws IOException {
        final Logger log = Logger.getLogger(VMTranslator.class.getSimpleName());
        log.setLevel(Level.INFO);
        log.info(() -> "source file:" + args[0]);
        final String targetFileName = args[0].replace(".vm", ".asm");
        log.info(() -> "target file:" + targetFileName);
        final FileWriter w = new FileWriter(targetFileName);
        final String compilationUnitName = args[0].replace(".vm", "");
        new VMTranslator(new File(args[0])).translateTo(compilationUnitName, w);
        w.close();
    }
}