class Commands {

    public interface Command {
        String compile(SymbolTable symbols);
    }

    private static Comment COMMENT = new Comment();

    public static Command comment() {
        return COMMENT;
    }

    public static Command ainst(String line) {
        return new AInstruction(line);
    }

    public static Command cinst(String line) {
        return new CInstruction(line);
    }

    public static class Comment implements Command {
        public String compile(SymbolTable symbols) {
            return "";
        }
    }

    public static class AInstruction implements Command {
        private final String symbol;

        public AInstruction(String line) {
            symbol = line.substring(1);
        }

        public String compile(SymbolTable symbols) {
            Integer value = symbols.lookup(symbol);
            if (value == null) {
                value = Integer.valueOf(symbol);
            }
            String spacePaddedValue = String.format("%16s", Integer.toBinaryString(value));
            String zeroPaddedValue = spacePaddedValue.replace(' ', '0');
            return zeroPaddedValue + '\n';
        }
    }

    public static class CInstruction implements Command {
        private final String dest;
        private final String comp;
        private final String jmp;


        public CInstruction(String line) {
            int posEqualSign = line.indexOf('=');
            if (posEqualSign != -1) {
                dest = line.substring(0, posEqualSign);
            } else {
                dest = "";
            }
            int posComp = posEqualSign + 1;
            int posSemiColon = line.indexOf(';');
            if (posSemiColon != -1) {
                comp = line.substring(posComp, posSemiColon);
                jmp = line.substring(posSemiColon + 1);
            } else {
                comp = line.substring(posComp);
                jmp = "";
            }
        }

        public String compile(SymbolTable symbols) {
            return "111" +
                    compileM() +
                    compileComp() +
                    compileDest() +
                    compileJmp() + "\n";
        }

        private String compileM() {
            return comp.contains("M")? "1" : "0";
        }

        private String compileComp() {
            switch (comp.replace("M" ,"A")) {
                case "0": return "101010";
                case "1": return "111111";
                case "-1": return "111010";
                case "D": return "001100";
                case "A": return "110000";
                case "!D": return "001101";
                case "!A": return "110001";
                case "-D": return "001111";
                case "-A": return "110011";
                case "D+1": return "011111";
                case "A+1": return "110111";
                case "D-1": return "001110";
                case "A-1": return "110010";
                case "D+A": return "000010";
                case "D-A": return "010011";
                case "A-D": return "000111";
                case "D&A": return "000000";
                case "D|A": return "010101";
            }
            return null;
        }

        private String compileJmp() {
            switch(jmp) {
                case "":  return "000";
                case "JGT": return "001";
                case "JEQ": return "010";
                case "JGE": return "011";
                case "JLT": return "100";
                case "JNE": return "101";
                case "JLE": return "110";
                case "JMP": return "111";
            }
            return null;
        }

        private String compileDest() {
            StringBuilder b = new StringBuilder();
            b.append(dest.contains("A")? '1' : '0');
            b.append(dest.contains("D")? '1' : '0');
            b.append(dest.contains("M")? '1' : '0');
            return b.toString();
        }
    }
}
