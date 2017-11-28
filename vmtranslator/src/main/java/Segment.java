import java.util.function.BiFunction;

enum Segment {
    PROGRAM_COUNTER("PC"),
    LOCAL("LCL"),
    ARGUMENT("ARG"),
    THAT("THAT"),
    THIS("THIS"),
    TEMP((n,i)-> "5", false, true),
    CONSTANT(null, false, false),
    STATIC((n,i)-> n.replace("/", "_") + '.' + i, false, false),
    POINTER((n,i)-> {
        switch (i) {
            case 0: return "THIS";
            case 1: return "THAT";
            default: throw new IllegalArgumentException("invalid pointer index: " + i);
        }
    }, false, false);

    private final BiFunction<String, Integer, String> memoryLocationProvider;
    private final boolean usesBasePointer;
    private final boolean usesPointerArithmetic;

    Segment(String name) {
        this((n,i) -> name, true, true);
    }

    Segment(BiFunction<String, Integer, String> symbol, boolean usesBasePointer, boolean usesPointerArithmetic) {
        this.memoryLocationProvider = symbol;
        this.usesBasePointer = usesBasePointer;
        this.usesPointerArithmetic = usesPointerArithmetic;
    }

    public static Segment forName(String segmentName) {
        return valueOf(segmentName.toUpperCase());
    }

    public String memoryLocation(String compilationUnitName, int i) {
        return memoryLocationProvider.apply(compilationUnitName, i);
    }

    public boolean usesBasePointer() {
        return usesBasePointer;
    }

    public boolean usesPointerArithmetic() {
        return usesPointerArithmetic;
    }
}