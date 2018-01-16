import java.util.Arrays;
import java.util.Stack;

class Assigner {
    private static final String[] sarr = new String[0];
    private String from;
    private String to;
    private Register fromRegister;
    private Register toRegister;
    private final Stack<String> result;

    public Assigner() {
        result = new Stack<>();
    }

    public static Assigner assign() {
        return new Assigner();
    }

    public static Assigner assign(Register reg) {
        Assigner r = new Assigner();
        r.from(reg);
        return r;
    }

    private Assigner from(Register reg) {
        fromRegister = reg;
        return this;
    }

    public Assigner fromM() {
        from = "M";
        return this;
    }

    public Assigner toA() {
        to = "A";
        return this;
    }

    public Assigner fromA() {
        from = "A";
        return this;
    }

    public Assigner toD() {
        to = "D";
        return this;
    }

    public String toString() {
        return to + "=" + from;
    }

    public Assigner fromD() {
        from = "D";
        return this;
    }

    public Assigner toM() {
        to = "M";
        return this;
    }

    public Assigner mPlusOne() {
        from = "M+1";
        return this;
    }

    public Assigner andToM() {
        to += "M";
        return this;
    }

    public Assigner aMinusOne() {
        from = "A-1";
        return this;
    }

    public Assigner mMinusOne() {
        from = "M-1";
        return this;
    }

    public Assigner aPlusD() {
        from = "D+A";
        return this;
    }

    public Assigner mPlusD() {
        from = "D+M";
        return this;
    }

    public Assigner mMinusD() {
        from = "M-D";
        return this;
    }

    public Assigner negatedM() {
        from = "-M";
        return this;
    }

    public Assigner notM() {
        from = "!M";
        return this;
    }

    public Assigner to(Register reg) {
        toRegister = reg;
        return this;
    }

    public String[] toStrings() {
        assert from != null ^ fromRegister != null;
        assert to != null ^ toRegister != null;
        if (toRegister != null && fromRegister !=null) {
            if (fromRegister == toRegister) {
                return sarr;
            } else if (toRegister.memory() && fromRegister.memory()) {
                result.addAll(Arrays.asList(assign(fromRegister).to(Register.D).toStrings()));
                result.addAll(Arrays.asList(assign(Register.D).to(toRegister).toStrings()));
                return result.toArray(new String[result.size()]);
            }
        }
        from = part(result, fromRegister, from);
        to = part(result, toRegister, to);
        result.push(String.format("%s=%s", to, from));
        return result.toArray(sarr);
    }

    private String part(Stack<String> result, Register r, String s) {
        if (r != null) {
            if (r.builtin()) {
                return r.name();
            } else {
                result.push(r.lookAt());
                return "M";
            }
        }
        return s;
    }
}