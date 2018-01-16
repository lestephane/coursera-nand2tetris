import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class TemporaryAsmFile implements AutoCloseable {

    private final Path file;

    TemporaryAsmFile() throws IOException {
        file = Files.createTempFile(null, ".asm");
    }

    Path getFile() {
        return file;
    }

    public void close() throws IOException {
        Files.deleteIfExists(file);
    }
}
