public class HackCompiledCommand {
    private final String cmd;

    enum Bits {
        cinst,reserved1,reserved2,a,c1,c2,c3,c4,c5,c6,d1,d2,d3,j1,j2,j3,crlf
    }

    public HackCompiledCommand(String cmd) {
        this.cmd = cmd;
    }

    public boolean ainst() {
        return ! isSet(Bits.cinst);
    }

    public int ainstValue() {
        return Integer.valueOf(cmd.substring(1,16), 2);
    }

    public boolean cinst() {
        return isSet(Bits.cinst) & isSet(Bits.reserved1) & isSet(Bits.reserved2);
    }

    private boolean isSet(Bits position) {
        return '1' == cmd.charAt(position.ordinal());
    }

    public String destDecodedString() {
        return  (isSet(Bits.d1)? "A" : "") +
                (isSet(Bits.d2)? "D" : "") +
                (isSet(Bits.d3)? "M" : "");
    }

    public String compDecodedString() {
        switch (compBinaryString()) {
            case "101010": return "0";
            case "111111": return "1";
            case "111010": return "-1";
            case "001100": return "D";
            case "110000": return isSet(Bits.a)? "M": "A";
            case "001101": return "!D";
            case "110001": return isSet(Bits.a)? "!M": "!A";
            case "001111": return "-D";
            case "110011": return isSet(Bits.a)? "-M": "-A";
            case "011111": return "D+1";
            case "110111": return isSet(Bits.a)? "M+1": "A+1";
            case "001110": return "D-1";
            case "110010": return isSet(Bits.a)? "M-1": "A-1";
            case "000010": return isSet(Bits.a)? "D+M": "D+A";
            case "010011": return isSet(Bits.a)? "D-M": "D-A";
            case "000111": return isSet(Bits.a)? "M-D": "A-D";
            case "000000": return isSet(Bits.a)? "D&M": "D&A";
            case "010101": return isSet(Bits.a)? "D|M": "D|A";
        }
        return null;
    }

    private String compBinaryString() {
        return cmd.substring(Bits.c1.ordinal(), Bits.d1.ordinal());
    }

    public String jmpDecodedString() {
        switch(jmpBinaryString()) {
            case "000": return null;
            case "001": return "JGT";
            case "010": return "JEQ";
            case "011": return "JGE";
            case "100": return "JLT";
            case "101": return "JNE";
            case "110": return "JLE";
            case "111": return "JMP";
        }
        assert false : jmpBinaryString();
        return "XXX";
    }

    private String jmpBinaryString() {
        return cmd.substring(Bits.j1.ordinal(), Bits.crlf.ordinal());
    }


    @Override
    public String toString() {
        return "HackCompiledCommand{" +
                "cmd='" + cmd + '\'' +
                '}';
    }
}
