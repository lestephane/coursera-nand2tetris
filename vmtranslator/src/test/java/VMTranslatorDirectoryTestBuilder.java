import java.io.IOException;
import java.util.function.Consumer;

class VMTranslatorDirectoryTestBuilder {
    private final TestDirectoryBuilder dir;

    VMTranslatorDirectoryTestBuilder(String name) {
        dir = new TestDirectoryBuilder(name);
    }

    VMTranslatorDirectoryTestBuilder withFile(Consumer<TestFileBuilder> consumer) {
        dir.withFile(consumer);

        return this;
    }

    void ThenTheTranslatedCommandsAre(Consumer<AsmAsserter> asm) throws IOException {
        new VMTranslatorAssertThat(VMTranslatorSource.fromDirectory(dir.build())).translatesTo(asm);
    }
}