package gemi.fl.scanner;

import java.util.HashMap;
import java.util.Map;

public final class Char {

    private final static Map<String,Character> escapeCodeToChar = new HashMap<>();
    private final static Map<Character,String> charToEscapeCode = new HashMap<>();

    public static Character unescapeChar(String s) {
        return escapeCodeToChar.get(s);
    }

    public static String escapeChar(char c) {
        String code = charToEscapeCode.get(c);
        if (code == null)
            return Character.toString(c);
        else
            return "\\"+code+"\\";
    }

    public static String escapeString(String s) {
        StringBuilder buf = new StringBuilder();
        for (char c : s.toCharArray()) buf.append(escapeChar(c));
        return buf.toString();
    }

    static {
        escapeCodeToChar.put("", '\\');
        escapeCodeToChar.put("NUL", (char)0);
        escapeCodeToChar.put("n", '\n');
        escapeCodeToChar.put("LF", '\n');
        escapeCodeToChar.put("NL", '\n');
        escapeCodeToChar.put("t", '\t');
        escapeCodeToChar.put("HT", '\t');
        escapeCodeToChar.put("TAB", '\t');
        escapeCodeToChar.put("'", '\'');
        escapeCodeToChar.put("a", (char)0x7);
        escapeCodeToChar.put("BEL", (char)0x7);
        escapeCodeToChar.put("b", (char)0x8);
        escapeCodeToChar.put("BS", (char)0x8);
        escapeCodeToChar.put("e", (char)0x1b);
        escapeCodeToChar.put("ESC", (char)0x1b);
        escapeCodeToChar.put("v", (char)0x0b);
        escapeCodeToChar.put("VT", (char)0x0b);
        escapeCodeToChar.put("f", (char)0x0c);
        escapeCodeToChar.put("FF", (char)0x0c);
        escapeCodeToChar.put("r", (char)0x0d);
        escapeCodeToChar.put("CR", (char)0x0d);
        escapeCodeToChar.put("FS", (char)0x1c);
        escapeCodeToChar.put("GS", (char)0x1d);
        escapeCodeToChar.put("RS", (char)0x1e);
        escapeCodeToChar.put("US", (char)0x1f);
        escapeCodeToChar.put("SP", (char)0x20);
        escapeCodeToChar.put("SPACE", (char)0x20);
        escapeCodeToChar.put("\"", '"');
        escapeCodeToChar.put("DEL", (char)0x7f);

        charToEscapeCode.put('\\', "");
        charToEscapeCode.put((char)0, "NUL");
        charToEscapeCode.put('\n', "LF");
        charToEscapeCode.put('\t', "HT");
        charToEscapeCode.put('\'', "\'");
        charToEscapeCode.put((char)0x7, "BEL");
        charToEscapeCode.put((char)0x8, "BS");
        charToEscapeCode.put((char)0x1b, "ESC");
        charToEscapeCode.put((char)0x0b, "VT");
        charToEscapeCode.put((char)0x0c, "FF");
        charToEscapeCode.put((char)0x0d, "CR");
        charToEscapeCode.put((char)0x1c, "FS");
        charToEscapeCode.put((char)0x1d, "GS");
        charToEscapeCode.put((char)0x1e, "RS");
        charToEscapeCode.put((char)0x1f, "US");
        charToEscapeCode.put('"', "\"");
        charToEscapeCode.put((char)0x7f, "DEL");
    }
}
