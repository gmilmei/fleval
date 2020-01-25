package gemi.fl.scanner;

import static gemi.fl.scanner.TokenType.*;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

public final class Scanner {


    public final static int STRING_QUOTE = '\"';

    private ErrorHandler errorHandler;
    private Reader reader;
    private int col = -1;
    private int line = 0;
    private int ch;

    private final static Map<String,TokenType> keywords = new HashMap<>();

    public Scanner(Reader reader, ErrorHandler errorHandler) throws IOException {
        this.reader = reader;
        this.errorHandler = errorHandler;
        nextChar();
    }

    public final Token next() {
        START: while (true) {
            // swallow whitespace
            while (Character.isWhitespace(ch)) nextChar();

            // end of file
            if (ch < 0) return makeToken(EOF, "", line, col);

            // save start position of token (for error handling)
            int cur_line = line;
            int cur_col = col;

            switch (ch) {
            case '<':
                nextChar();
                if (ch == '-') {
                    nextChar();
                    if (ch == '|')
                        return makeTokenAndAdvance(PREDICATE_APPEND_RIGHT, "<-|", cur_line, cur_col);
                    else
                        return makeToken(ARROW_LEFT, "<-", cur_line, cur_col);
                }
                else if (ch == '=') {
                    nextChar();
                    if (ch == '|')
                        return makeTokenAndAdvance(NAME, Names.PATTERN_APPEND_RIGHT, cur_line, cur_col);
                    else {
                        error(line, col, "unexpected character");
                        return makeToken(SEQUENCE_BRACKET_LEFT, "<", cur_line, cur_col);
                    }
                }
                else {
                    return makeToken(SEQUENCE_BRACKET_LEFT, "<", cur_line, cur_col);
                }
            case '.':
                nextChar();
                if (isDigit(ch))
                    return number(1, true);
                else {
                    // expression must follow immediately
                    if (isIdentifierStartingCharacter(ch) || ch == '(' || ch == '[' || ch == '~' || ch == '⟦')
                        return makeToken(PERIOD_EXPR, ".", cur_line, cur_col);
                    else
                        return makeToken(PERIOD, ".", cur_line, cur_col);
                }
            case '[':
                nextChar();
                if (ch == '|')
                    return makeTokenAndAdvance(PREDICATE_CONSTRUCTION_LEFT, "[|", cur_line, cur_col);
                else
                    return makeToken(CONSTRUCTION_BRACKET_LEFT, "[", cur_line, cur_col);
            case ':':
                nextChar();
                if (ch == ':')
                    return makeTokenAndAdvance(SIG_SIGN, "::", cur_line, cur_col);
                else
                    return makeToken(APPLICATION_SIGN, ":", cur_line, cur_col);
            case '|':
                nextChar();
                if (ch == ']') {
                    return makeTokenAndAdvance(PREDICATE_CONSTRUCTION_RIGHT, "|]", cur_line, cur_col);
                }
                else if (ch == '=') {
                    nextChar();
                    if (ch == '>')
                        return makeTokenAndAdvance(NAME, Names.PATTERN_APPEND_LEFT, cur_line, cur_col);
                    else
                        return makeTokenAndAdvance(ERROR, "|="+ch, cur_line, cur_col);
                }
                else if (ch == '-') {
                    nextChar();
                    if (ch == '>')
                        return makeTokenAndAdvance(PREDICATE_APPEND_LEFT, "|->", cur_line, cur_col);
                    else {
                        error(line, col, "unexpected character");
                        return makeTokenAndAdvance(NAME, "|", cur_line, cur_col);
                    }
                }
                else {
                    return makeToken(NAME, "!", cur_line, cur_col);
                }
            case '=':
                nextChar();
                if (ch == '=') {
                    // ==
                    return makeTokenAndAdvance(DEFINITION_SYMBOL, "≡", cur_line, cur_col);
                }
                else if (ch == 'f') {
                    // =f
                    nextChar();
                    if (ch == '=') {
                        // =f=
                        nextChar();
                        if (ch == '>')
                            // =f=>
                            return makeTokenAndAdvance(NAME, Names.FUNCTION_META_PREDICATE, cur_line, cur_col);
                        else
                            return makeTokenAndAdvance(ERROR, "=f="+ch, cur_line, cur_col);
                    }
                    else {
                        return makeTokenAndAdvance(ERROR, "=f"+ch, cur_line, cur_col);
                    }
                }
                else {
                    return makeToken(NAME, Names.EQUAL, cur_line, cur_col);
                }
            case '+':
                nextChar();
                if (isDigit(ch))
                    return number(1, false);
                else
                    return makeToken(NAME, Names.ADD, cur_line, cur_col);
            case '-':
                nextChar();
                if (ch == '>')
                    return makeTokenAndAdvance(ARROW_RIGHT, "->", cur_line, cur_col);
                else if (isDigit(ch))
                    return number(-1, false);
                else
                    return makeToken(NAME, Names.SUBTRACT, cur_line, cur_col);
            case '\\':
                nextChar();
                if (ch == '/')
                    return makeTokenAndAdvance(NAME, Names.OR, cur_line, cur_col);
                else
                    return makeToken(BACKSLASH, "\\", cur_line, cur_col);
            case '/':
                nextChar();
                if (ch == '/') {
                    /// end of line comment
                    nextChar();
                    while (ch >= 0 && ch != '\n') nextChar();
                    continue START;
                }
                else if (ch == '*') {
                    // comment
                    nextChar();
                    while (ch >= 0) {
                        if (ch == '*') {
                            nextChar();
                            if (ch == '/') {
                                nextChar();
                                continue START;
                            }
                        }
                        else {
                            nextChar();
                        }
                    }
                    return makeToken(EOF, "", line, col);
                }
                else if (ch == '\\') {
                    return makeTokenAndAdvance(NAME, Names.AND, cur_line, cur_col);
                }
                else if (isIdentifierCharacter(ch)) {
                    Token token = identifier(cur_line, cur_col);
                    token.name = '/'+token.name;
                    return token;
                }
                return makeToken(NAME, Names.DIVIDE, cur_line, cur_col);
            case '\'':
                return makeTokenAndAdvance(PRIME, "'", cur_line, cur_col);
            case '÷':
                return makeTokenAndAdvance(NAME, Names.DIVIDE, cur_line, cur_col);
            case '≡':
                return makeTokenAndAdvance(DEFINITION_SYMBOL, "==", cur_line, cur_col);
            case '¬':
                return makeTokenAndAdvance(NAME, "Not", cur_line, cur_col);
            case '∧':
                return makeTokenAndAdvance(NAME, Names.AND, cur_line, cur_col);
            case 'λ':
                return makeTokenAndAdvance(LAMBDA, Names.LAMBDA, cur_line, cur_col);
            case '{':
                return makeTokenAndAdvance(BRACE_LEFT, "{", cur_line, cur_col);
            case '}':
                return makeTokenAndAdvance(BRACE_RIGHT, "}", cur_line, cur_col);
            case '↦':
                return makeTokenAndAdvance(PREDICATE_APPEND_LEFT, "|->", cur_line, cur_col);
            case '↤':
                return makeTokenAndAdvance(PREDICATE_APPEND_RIGHT, "<-|", cur_line, cur_col);
            case '⤇':
                return makeTokenAndAdvance(NAME, Names.PATTERN_APPEND_LEFT, cur_line, cur_col);
            case '⤆':
                return makeTokenAndAdvance(NAME, Names.PATTERN_APPEND_RIGHT, cur_line, cur_col);
            case ']':
                return makeTokenAndAdvance(CONSTRUCTION_BRACKET_RIGHT, "]", cur_line, cur_col);
            case 'v':
                return makeTokenAndAdvance(NAME, Names.OR, cur_line, cur_col);
            case '→':
                return makeTokenAndAdvance(ARROW_RIGHT, "->", cur_line, cur_col);
            case '*':
                return makeTokenAndAdvance(NAME, Names.MULTIPLY, cur_line, cur_col);
            case '∘':
                return makeTokenAndAdvance(NAME, Names.COMPOSE, cur_line, cur_col);
            case '@':
                return makeTokenAndAdvance(NAME, Names.COMPOSE, cur_line, cur_col);
            case '!':
                return makeTokenAndAdvance(NAME, "!", cur_line, cur_col);
            case '←':
                return makeTokenAndAdvance(ARROW_LEFT, "<-", cur_line, cur_col);
            case '>':
                return makeTokenAndAdvance(SEQUENCE_BRACKET_RIGHT, ">", cur_line, cur_col);
            case ',':
                return makeTokenAndAdvance(COMMA, ",", cur_line, cur_col);
            case ';':
                return makeTokenAndAdvance(SEMICOLON, ";", cur_line, cur_col);
            case '(':
                return makeTokenAndAdvance(PAREN_LEFT, "(", cur_line, cur_col);
            case ')':
                return makeTokenAndAdvance(PAREN_RIGHT, ")", cur_line, cur_col);
            case '~':
                return makeTokenAndAdvance(TILDE, "~", cur_line, cur_col);
            case '⟦':
                return makeTokenAndAdvance(PREDICATE_CONSTRUCTION_LEFT, "~", cur_line, cur_col);
            case '⟧':
                return makeTokenAndAdvance(PREDICATE_CONSTRUCTION_RIGHT, "~", cur_line, cur_col);
            }

            // number
            if (isDigit(ch)) return number(1, false);

            // string
            if (ch == STRING_QUOTE) return string();

            // character
            if (ch == '`') return character();

            // identifiers and keywords
            if (isIdentifierStartingCharacter(ch)) return identifier(cur_line, cur_col);

            error(line, col, "unexpected character "+(char)ch+" ("+ch+")");
            nextChar();
        }
    }
    
    private final Token identifier(int cur_line, int cur_col) {
        StringBuilder buf = new StringBuilder();
        buf.append((char)ch);
        nextChar();
        while (isIdentifierCharacter(ch)) {
            buf.append((char)ch);
            nextChar();
        }
        String text = buf.toString();
        int maxlength = 31;
        if (text.length() > maxlength) {
            error(cur_line, cur_col, "identifier '"+text+"' too long, truncated to "+maxlength+" characters");
            text = text.substring(0, maxlength);
        }

        Token token = new Token(NAME, text, cur_line, cur_col);
        lookupKeyword(token);
        return token;
    }

    private final Token number(int sign, boolean fractional) {
        int cur_col = col;
        int cur_line = line;
        long m = 0;
        double f = 0;
        int e = 0;
        if (fractional) {
            double factor = 1/10.0;
            while (isDigit(ch)) {
                f = f+(ch-'0')*factor;
                factor /= 10.0;
                nextChar();
            }
        }
        else {
            while (isDigit(ch)) {
                long d = ch-'0';
                m = m*10+d;
                nextChar();
            }
            if (ch == '.') {
                nextChar();
                fractional = true;
                double factor = 1/10.0;
                while (isDigit(ch)) {
                    f = f+(ch-'0')*factor;
                    factor /= 10.0;
                    nextChar();
                }
            }
        }

        if (ch == 'e' || ch =='E') fractional = true;

        if (!fractional) {
            return new Token(INTEGER, sign*m, cur_line, cur_col);
        }

        double number = (m+f)*sign;
        if (ch == 'e' || ch == 'E') {
            int esign = 1;
            nextChar();
            if (ch == '+') {
                esign = 1;
                nextChar();
            }
            else if (ch == '-') {
                esign = -1;
                nextChar();
            }
            if (isDigit(ch)) {
                while (isDigit(ch)) {
                    e = e*10+(ch-'0');
                    nextChar();
                }
            }
            else {
                errorHandler.error(line, col, "unexpected character: "+(char)ch);
                return new Token(REAL, number, cur_line, cur_col);
            }
            e = esign*e;
            number = number*Math.pow(10, e);
        }
        return new Token(REAL, number, cur_line, cur_col);
    }

    private final Token string() {
        Token token = new Token(STRING, 0, line, col);
        nextChar();
        if (ch < 0) {
            error(line, col, "unexpected end of line in string");
            return makeToken(EOF, "", line, col);
        }

        StringBuilder buf = new StringBuilder();
        while (ch >= 0 && ch != STRING_QUOTE) {
            if (ch == '\n' || ch == '\r') {
                error(line, col, "unexpected newline in string");
                nextChar();
            }
            else if (ch == '\\') {
                buf.append((char)escapedCharacter());
            }
            else {
                buf.append((char)ch);
                nextChar();
            }
        }

        if (ch != STRING_QUOTE)
            error(line, col, "string not terminated by \"");
        else
            nextChar();
        token.string = buf.toString();
        return token;
    }

    private final Token character() {
        nextChar();
        if (ch < 0) {
            error(line, col, "unexpected end of file in character literal");
            return makeToken(EOF, "", line, col);
        }
        else if (ch == '\\') {
            return makeToken(CHARACTER, escapedCharacter(), line, col);
        }
        else if (Character.isWhitespace((char)ch)) {
            error(line, col, "invalid character literal with code "+(int)ch);
            return makeTokenAndAdvance(CHARACTER, ' ', line, col);
        }
        else if (Character.isISOControl(ch)) {
            error(line, col, "invalid character literal with code "+(int)ch);
            return makeTokenAndAdvance(CHARACTER, ' ', line, col);
        }
        else {
            return makeTokenAndAdvance(CHARACTER, (char)ch, line, col);
        }
    }

    private final void nextChar() {
        try {
            ch = reader.read();
            if (ch == '\n') {
                col = -1;
                line++;
            }
            else {
                col++;
            }
        } catch (IOException e) {
            error(line, col, "I/O");
            ch = -1;
        }
    }

    private final void error(int line, int col, String text) {
        errorHandler.error(line, col, text);
    }

    private final Token makeTokenAndAdvance(TokenType tokenType, String text, int line, int col) {
        Token token = new Token(tokenType, text, line, col);
        nextChar();
        return token;
    }

    private final Token makeTokenAndAdvance(TokenType tokenType, char character, int line, int col) {
        Token token = new Token(tokenType, character, line, col);
        nextChar();
        return token;
    }

    private static Token makeToken(TokenType tokenType, String text, int line, int col) {
        Token token = new Token(tokenType, text, line, col);
        return token;
    }

    private static Token makeToken(TokenType tokenType, char character, int line, int col) {
        Token token = new Token(tokenType, character, line, col);
        return token;
    }

    private static boolean isDigit(int ch) {
        return ch >='0' && ch <= '9';
    }

    private static boolean isIdentifierStartingCharacter(int ch) {
        return (ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z') || (ch >= 'Α' && ch <= 'ω') || ch == '/'
                || ch == '$' || ch =='%' || ch == '#' || ch == '_' || ch == '?'
                || ch == '↑' || ch == '↓' || ch == '^' || ch == '¬';
    }

    private static boolean isIdentifierCharacter(int ch) {
        return isIdentifierStartingCharacter(ch) || (ch >= '0' && ch <= '9');
    }

    private final char escapedCharacter() {
        nextChar();
        char character = ' ';

        if (ch == 'U' || ch == 'u') {
            // unicode code point
            character = 0;
            nextChar();
            while (true) {
                if (ch >= '0' && ch <= '9') {
                    character = (char)(character*16+(ch-'0'));
                }
                else if (ch >= 'a' && ch <= 'f') {
                    character = (char)(character*16+(10+ch-'a'));
                }
                else if (ch >= 'A' && ch <= 'F') {
                    character = (char)(character*16+(10+ch-'A'));
                }
                else {
                    break;
                }
                nextChar();
            }
            if (ch == '\\')
                nextChar();
            else
                error(line, col, "character not terminated by \\");
            return character;
        }

        StringBuilder buf = new StringBuilder();
        while (ch >= 0 && ch != '\\' && ch != '\n') {
            buf.append((char)ch);
            nextChar();
        }
        if (ch < 0) {
            error(line, col, "character not terminated by \\");
            return ' ';
        }
        else if (ch == '\n') {
            error(line, col, "character not terminated by \\");
            return ' ';
        }
        else if (ch == '\\') {
            nextChar();
            String s = buf.toString();
            if (s.matches("[0-9]+")) {
                character = 0;
                for (char c : s.toCharArray()) {
                    character = (char)(character*10+c-'0');
                }
                return character;
            }
            Character c = Char.unescapeChar(s);
            if (c == null) {
                error(line, col, "unknown character \\"+buf+"\\");
                character = ' ';
            }
            else {
                character = c;
            }
            return character;
        }
        else {
            error(line, col, "character not terminated by \\");
            return ' ';
        }
    }

    private static final void lookupKeyword(Token token) {
        TokenType tokenType = keywords.get(token.name);
        if (tokenType != null) token.type = tokenType;
    }

    static {
        for (TokenType tokenType : TokenType.values()) {
            if (tokenType.keyword) {
                keywords.put(tokenType.name, tokenType);
            }
        }
    }

    public static void main(String[] args) throws Exception {
        String filename = "test.fl";
        ErrorHandler errorHandler = new ErrorHandler(filename, System.err);
        Scanner scanner = new Scanner(new FileReader(filename), errorHandler);
        Token token = scanner.next();
        while (token.type != EOF) {
            System.out.println(token);
            token = scanner.next();
        }
    }
}
