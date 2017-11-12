import java.io.*;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class VmTranslator {
    private final Parser.Source parserSource;
    public VmTranslator(String input) {
        parserSource = new Parser.StringSource(input);
    }

    public VmTranslator(File input) {
        parserSource = new Parser.FileSource(input);
    }

    public void translateTo(Writer output) {
        CodeWriter codeOutput = new CodeWriter(output);
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
        Logger log = Logger.getLogger(VmTranslator.class.getSimpleName());
        log.setLevel(Level.INFO);
        log.info(() -> "source file:" + args[0]);
        String target = args[0].replace(".vm", ".asm");
        log.info(() -> "target file:" + target);
        FileWriter w = new FileWriter(target);
        new VmTranslator(new File(args[0])).translateTo(w);
        w.close();
    }
}