import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

class TestFileBuilder {
    private String name;
    private String content;

    TestFileBuilder withName(String name) {
        this.name = name;
        return this;
    }

    TestFileBuilder withLines(String... lines) {
        content = String.join(System.lineSeparator(), lines);
        return this;
    }

    void build(Path parent) throws IOException {
        Path testFile = parent.resolve(name);
        Files.write(testFile, content.getBytes());
    }
}