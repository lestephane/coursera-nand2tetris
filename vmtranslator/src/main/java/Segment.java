enum Segment {
    LOCAL("LCL"),
    ARGUMENT("ARG"),
    THAT("THAT"),
    THIS("THIS"),
    TEMP("5"),
    CONSTANT(null);

    private final String symbol;

    Segment(String symbol) {
        this.symbol = symbol;
    }

    public static Segment forName(String segmentName) {
        return valueOf(segmentName.toUpperCase());
    }

    public String symbol() {
        return symbol;
    }

    public boolean usesBasePointer() {
        // temp is the only segment that does not use a base pointer
        return this != TEMP;
    }
}