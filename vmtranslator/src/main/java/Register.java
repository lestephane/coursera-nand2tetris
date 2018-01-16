import java.util.function.Function;

public enum Register {
    D("D", Locality.Builtin),
    D2("R13", Locality.Memory),
    D3("R14", Locality.Memory),
    D4("R15", Locality.Memory),
    LCL("LCL",Locality.Memory),
    ARG("ARG",Locality.Memory),
    THAT("THAT",Locality.Memory),
    THIS("THIS",Locality.Memory);

    private final String name;
    private final Locality where;

    Register(String name, Locality builtin) {
        this.name = name;
        this.where = builtin;
    }

    public boolean builtin() {
        return where == Locality.Builtin;
    }

    public String lookAt() {
        return where.load(name);
    }

    public boolean memory() {
        return where == Locality.Memory;
    }

    private enum Locality {
        Memory((n) -> "@" + n),
        Builtin((n) -> null);

        private final Function<String, String> f;

        Locality(Function<String,String> f) {
            this.f = f;
        }

        public String load(String name) {
            return f.apply(name);
        }
    }
}