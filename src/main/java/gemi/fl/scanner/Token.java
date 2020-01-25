package gemi.fl.scanner;


public class Token {

    public TokenType type;

    // identifier and names
    public String name = null;
    // literal string (as array of words)
    public String string = null;
    // literal real number
    public double real = 0;
    // literal integer number
    public long integer = 0;
    // literal character
    public char character;
    // token position
    public int line = 0;
    public int col = 0;

    public Token(TokenType type, char character, int line, int col) {
        this.type = type;
        this.character = character;
        this.line = line;
        this.col = col;
    }

    public Token(TokenType type, String name, int line, int col) {
        this.type = type;
        this.name = name;
        this.line = line;
        this.col = col;
    }

    public Token(TokenType type, double real, int line, int col) {
        this.type = type;
        this.real = real;
        this.line = line;
        this.col = col;
    }

    public Token(TokenType type, long integer, int line, int col) {
        this.type = type;
        this.integer = integer;
        this.line = line;
        this.col = col;
    }

    @Override
    public final String toString() {
        switch (type) {
        case CHARACTER:
            return type.toString()+"["+character+"]";
        case STRING:
            return type.toString()+"["+string+"]";
        case INTEGER:
            return type.toString()+"["+integer+"]";
        case REAL:
            return type.toString()+"["+real+"]";
        case NAME:
            return type.toString()+"["+name+"]";
        case EOF:
            return type.toString();
        default:
            return type.toString()+"["+name+"]";
        }
    }
}
