import java.io.IOException;
import java.util.function.Consumer;

class VMTranslatorSingleFileTestBuilder {
    private final TestFileBuilder file;

    VMTranslatorSingleFileTestBuilder(String name) {
        file = new TestFileBuilder().withName(name);
    }

    VMTranslatorSingleFileTestBuilder withLines(String... lines) {
        file.withLines(lines);
        return this;
    }

    VMTranslatorSingleFileTestBuilder ThenTheTranslatedCommandsAre
            (Consumer<AsmAsserter> asmConsumer) throws IOException {
        new VMTranslatorAssertThat(
                VMTranslatorSource.fromFile(file.build())).translatesTo(asmConsumer);
        return this;
    }
}
