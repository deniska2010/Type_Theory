package Utils;

public class ParseException extends Exception {

    public ParseException(String s) {
        super(s);
    }

    public ParseException(String message, int position) {
        super("Error occurred at " + position + ", error message: " + message);
    }
}
