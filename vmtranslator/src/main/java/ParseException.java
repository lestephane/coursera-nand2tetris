import java.io.IOException;

public class ParseException extends RuntimeException {
    public ParseException(IOException e) {
        super(e);
    }
}
