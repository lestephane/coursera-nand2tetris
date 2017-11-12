import java.io.FileNotFoundException;

public class ParseException extends RuntimeException {
    public ParseException(FileNotFoundException e) {
        super(e);
    }
}
