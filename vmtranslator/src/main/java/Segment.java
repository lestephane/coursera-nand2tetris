enum Segment {
    LOCAL("LCL", true),
    ARGUMENT("ARG", true),
    THAT("THAT", true),
    THIS("THIS", true),
    TEMP("5", false),
    CONSTANT("", false);

    private final String symbol;
    private final boolean usesBasePointer;

    Segment(String symbol, boolean usesBasePointer) {
        this.symbol = symbol;
        this.usesBasePointer = usesBasePointer;
    }

    public static Segment forName(String segmentName) {
        return valueOf(segmentName.toUpperCase());
    }

    public String symbol() {
        return symbol;
    }

    public boolean usesBasePointer() {
        return this.usesBasePointer;
    }
}