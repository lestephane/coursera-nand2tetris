import java.io.*;
import java.util.function.Consumer;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class VMTranslator {
    private static final Logger LOGGER = Logger.getLogger( Parser.class.getName() );
    static {
        try {
            LogManager.getLogManager().readConfiguration(new ByteArrayInputStream((
                    "handlers=java.util.logging.ConsoleHandler\n" +
                    ".level=INFO\n" +
                    "java.util.logging.ConsoleHandler.level=FINE\n" +
                    "java.util.logging.ConsoleHandler.formatter=java.util.logging.SimpleFormatter\n" +
                    "java.util.logging.SimpleFormatter.format=[%1$tF %1$tr] %3$s %4$s:  %5$s %n").getBytes()));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
    private final Parser.Source parserSource;
    public VMTranslator(String input) {
        this(new Parser.StringSource(input));
    }

    private VMTranslator(Parser.Source source) {
        parserSource = source;
    }

    public VMTranslator(File input) {
            this(new Parser.FileSource(input));
    }

    public static void main(String[] args) throws IOException {
        final String sourceName = args[0];
        final File sourceFile = new File(sourceName);
        if (sourceFile.isFile()) {
            LOGGER.info(() -> "source file:" + sourceName);
            processFile(sourceFile);
        } else {
            LOGGER.info(() -> "source directory:" + sourceName);
            processDirectory(sourceFile);
        }
    }

    private static void processFile(File srcFile) throws IOException {
        final String targetFileName = srcFile.getName().replace(".vm", ".asm");
        LOGGER.info(() -> "target file:" + targetFileName);
        try (FileWriter w = new FileWriter(targetFileName)) {
            final String compilationUnitName = srcFile.getName().replace(".vm", "");
            new VMTranslator(srcFile).translateTo(compilationUnitName, w);
        }
    }

    private static void processDirectory(File srcDir) throws IOException {
        final File targetFileName = new File(srcDir.getParent(), srcDir.getName() + ".asm");
        LOGGER.info(() -> "target file:" + targetFileName);
        try (FileWriter w = new FileWriter(targetFileName)) {
            final String compilationUnitName = srcDir.getName();
            final Parser.DirectorySource directorySource = new Parser.DirectorySource(srcDir);
            new VMTranslator(directorySource).translateTo(compilationUnitName, w);
        }
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
}