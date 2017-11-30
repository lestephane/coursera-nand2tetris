import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

public class TestDirectoryBuilder {
    private final static Path enclosingDirectory;
    private final static Thread fileSystemCleaner;

    static {
        try {
            enclosingDirectory = Files.createTempDirectory("coursera");
            fileSystemCleaner = new Thread(() -> {
                if (enclosingDirectory == null) {
                    return;
                }
                try {
                    Files.walk(enclosingDirectory)
                            .sorted(Comparator.reverseOrder())
                            .map(Path::toFile)
                            .peek(System.out::println)
                            .forEach(File::delete);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
            Runtime.getRuntime().addShutdownHook(fileSystemCleaner);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private final String name;
    private final List<TestFileBuilder> testFiles = new LinkedList<>();

    public TestDirectoryBuilder(String name) {
        this.name = name;
    }

    public TestDirectoryBuilder withFile(Consumer<TestFileBuilder> consumer) {
        TestFileBuilder file = new TestFileBuilder()
                .withParent(enclosingDirectory().resolve(name));
        testFiles.add(file);
        consumer.accept(file);
        return this;
    }

    public Path build() throws IOException {
        Path testDirectory = enclosingDirectory.resolve(name);
        Files.createDirectory(testDirectory);
        for (TestFileBuilder f: testFiles) {
            f.build();
        }
        return testDirectory;
    }

    public static Path enclosingDirectory() {
        return enclosingDirectory;
    }
}