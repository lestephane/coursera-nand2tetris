import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

class TestFileBuilder {
    private String name;
    private String content;
    private Path parent;

    TestFileBuilder() {
        this.parent = TestDirectoryBuilder.enclosingDirectory();
    }

    TestFileBuilder withName(String name) {
        this.name = name;
        return this;
    }

    TestFileBuilder withParent(Path parent) {
        this.parent = parent;
        return this;
    }

    TestFileBuilder withLines(String... lines) {
        content = String.join(System.lineSeparator(), lines);
        return this;
    }

    Path build() throws IOException {
        Path testFile = parent.resolve(name);
        Files.write(testFile, content.getBytes());
        return testFile;
    }
}