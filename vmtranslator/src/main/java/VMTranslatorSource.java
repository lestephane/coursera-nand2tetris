import java.io.*;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

interface VMTranslatorSource {
    static VMTranslatorSource fromDirectory(Path inputDirectory) {
        return new DirectorySource(inputDirectory);
    }

    static VMTranslatorSource fromFile(Path inputFile) {
        return new FileSource(inputFile);
    }

    static VMTranslatorSource fromString(String inputName, String inputString) {
        return new StringSource(inputName, inputString);
    }

    String translationUnitName();

    Reader open();

    default boolean needsBootstrap() {
        return false;
    }

    class StringSource implements VMTranslatorSource {
        private final String name;
        private final String input;

        StringSource(String name, String input) {
            this.name = name;
            this.input = input;
        }

        public Reader open() {
            return new StringReader(input);
        }

        public String translationUnitName() {
            return name;
        }
    }

    class FileSource implements VMTranslatorSource {
        private final Path input;

        FileSource(Path input) {
            this.input = input;
        }

        public Reader open() {
            try {
                return new FileReader(input.toFile());
            } catch (FileNotFoundException e) {
                throw new UncheckedIOException(e);
            }
        }

        public String translationUnitName() {
            return input.getFileName().toString().replaceFirst(".vm", "");
        }
    }

    class DirectorySource implements VMTranslatorSource {
        private final Path dir;

        DirectorySource(Path dir) {
            this.dir = dir;
        }

        public Reader open() {
            try {
                String content = readAllFiles();
                return new StringReader(content);
            } catch (IOException e) {
                throw new ParseException(e);
            }
        }

        private String readAllFiles() throws IOException {
            List<String> result = new LinkedList<>();
            List<Path> filesInAlphabeticalOrder = new ArrayList<>();
            try(DirectoryStream<Path> stream = Files.newDirectoryStream(dir, "*.vm")) {
                stream.forEach(filesInAlphabeticalOrder::add);
                filesInAlphabeticalOrder.sort(Comparator.comparing(Path::toString));
            }
            for (Path p: filesInAlphabeticalOrder) {
                result.addAll(Files.readAllLines(p));
            }
            return result.stream().collect(Collectors.joining(System.lineSeparator()));
        }

        public String translationUnitName() {
            return dir.toFile().getName();
        }

        public boolean needsBootstrap() {
            return true;
        }
    }
}