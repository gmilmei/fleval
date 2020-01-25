package gemi.fl.scanner;

public enum TokenType {

    // special tokens
    EOF,
    ERROR,

    // literals
    CHARACTER,
    INTEGER,
    NAME,
    REAL,
    STRING,

    // symbols
    BRACE_LEFT,
    BRACE_RIGHT,
    PAREN_LEFT,
    PAREN_RIGHT,
    TILDE,
    SEQUENCE_BRACKET_LEFT,
    SEQUENCE_BRACKET_RIGHT,
    DOUBLE_QUOTE,
    BACK_QUOTE,
    COMMA,
    SEMICOLON,
    APPLICATION_SIGN,
    SIG_SIGN,
    PERIOD,
    PERIOD_EXPR,
    CONSTRUCTION_BRACKET_LEFT,
    CONSTRUCTION_BRACKET_RIGHT,
    EQUAL,
    PRIME,
    COMPOSITION,
    REVERSE_COMPOSITION,
    BACKSLASH,
    PREDICATE_CONSTRUCTION_LEFT,
    PREDICATE_CONSTRUCTION_RIGHT,
    DEFINITION_SYMBOL,
    ARROW_LEFT,
    ARROW_RIGHT,
    PREDICATE_APPEND_LEFT,
    PREDICATE_APPEND_RIGHT,
    AND,
    OR,

    // reserved words (keywords)
    ASN ("asn"),
    DEF ("def"),
    EXDEF ("exdef"),
    EXPORT ("export"),
    FALSE ("false"),
    FORALL ("forall"),
    HIDE ("hide"),
    LAMBDA ("lambda"),
    LIB ("lib"),
    META ("meta"),
    NRDEF ("nrdef"),
    PAT ("pat"),
    PF ("PF"),
    REC ("rec"),
    SIG ("sig"),
    TRUE ("true"),
    TYPE ("type"),
    UNION ("union"),
    USES ("uses"),
    WHERE ("where");

    public String name = null;
    public boolean keyword = false;

    private TokenType() {}

    private TokenType(String name) {
        this.name = name;
        this.keyword = true;
    }
}
