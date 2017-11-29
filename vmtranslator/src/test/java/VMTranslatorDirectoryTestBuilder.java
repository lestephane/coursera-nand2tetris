import java.util.LinkedList;
import java.util.function.Consumer;

class VMTranslatorDirectoryTestBuilder implements VMTranslatorInput {
    private final String enclosingDirectoryName;
    private final LinkedList<InputFileBuilder> sourceFiles = new LinkedList<>();

    VMTranslatorDirectoryTestBuilder(String name) {
        this.enclosingDirectoryName = name;
    }

    VMTranslatorDirectoryTestBuilder withFile(Consumer<InputFileBuilder> consumer) {
        consumer.accept(makeSourceFile());
        return this;
    }

    protected InputFileBuilder makeSourceFile() {
        final InputFileBuilder file = new InputFileBuilder();
        this.sourceFiles.add(file);
        return file;
    }

    void ThenTheTranslatedCommandsAre(Consumer<AsmAsserter> asm) {
        new VMTranslatorAssertThat(this).translatesTo(asm);
    }

    public String translationUnitName() {
        return enclosingDirectoryName;
    }

    public String vmSourceCode() {
        StringBuffer sb = new StringBuffer();
        for (InputFileBuilder input : sourceFiles) {
            sb.append(input.content);
            sb.append('\n');
        }
        return sb.toString();
    }

    class InputFileBuilder {
        private String name;
        private String content;

        InputFileBuilder withName(String name) {
            this.name = name;
            return this;
        }

        InputFileBuilder withLines(String... lines) {
            this.content = String.join("\n", lines);
            return this;
        }
    }
}