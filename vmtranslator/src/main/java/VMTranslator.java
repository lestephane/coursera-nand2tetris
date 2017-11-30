import java.io.*;
import java.nio.file.Path;
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

    private final VMTranslatorSource input;

    public VMTranslator(VMTranslatorSource input) {
        this.input = input;
    }

    public static void main(String[] args) throws IOException {
        String sourceName = args[0];
        File sourceFile = new File(sourceName);
        if (sourceFile.isFile()) {
            LOGGER.info(() -> "source file:" + sourceName);
            processFile(sourceFile);
        } else {
            LOGGER.info(() -> "source directory:" + sourceName);
            processDirectory(sourceFile.toPath());
        }
    }

    private static void processFile(File srcFile) throws IOException {
        String targetFileName = srcFile.getName().replace(".vm", ".asm");
        LOGGER.info(() -> "target file:" + targetFileName);
        try (FileWriter w = new FileWriter(targetFileName)) {
            String compilationUnitName = srcFile.getName().replace(".vm", "");
            new VMTranslator(VMTranslatorSource.fromFile(srcFile)).translateTo(compilationUnitName, w);
        }
    }

    private static void processDirectory(Path srcDir) throws IOException {
        Path targetPath = srcDir.resolve(srcDir.getFileName() + ".asm");
        LOGGER.info(() -> "target file:" + targetPath);
        File targetFile = targetPath.toFile();
        try (FileWriter w = new FileWriter(targetFile)) {
            String compilationUnitName = srcDir.toFile().getName();
            new VMTranslator(VMTranslatorSource.fromDirectory(srcDir)).translateTo(compilationUnitName, w);
        }
    }

    public String translate(String unitName) {
        StringWriter output = new StringWriter();
        translateTo(unitName, output);
        return output.toString();
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
        Parser parser = new Parser(new BufferedReader(input.open()));
        while (parser.hasMoreCommands()) {
            Commands.Command c = parser.command();
            if (c != null) {
                cmd.accept(c);
            }
            parser.advance();
        }
    }
}