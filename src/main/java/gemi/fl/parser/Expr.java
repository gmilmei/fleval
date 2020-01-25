package gemi.fl.parser;

import static gemi.fl.parser.Utilities.indentln;

import gemi.fl.parser.Patterns.PatOrExpr;

public final class Expr {

    private ExprType type;

    private String  name = null;
    private String  string = null;
    private long    integer;
    private double  real;
    private boolean truth = false;
    private char    character = 0;

    private Expr expr1 = null;
    private Expr expr2 = null;
    private Expr expr3 = null;

    private Expr[] exprs = null;

    private int primes = 0;
    
    private Env env = null;

    public int line = 0;
    public int col = 0;

    private PatOrExpr patOrExpr = null;
    
    private static int nameCount = 0;

    public static Expr makeCharacter(int line, int col, char character) {
        Expr expr = new Expr(line, col, ExprType.CHARACTER);
        expr.character = character;
        expr.patOrExpr = PatOrExpr.EXPR;
        return expr;
    }

    public static Expr makeString(int line, int col, String string) {
        Expr expr = new Expr(line, col, ExprType.STRING);
        expr.string = string;
        expr.patOrExpr = PatOrExpr.EXPR;
        return expr;
    }

    public static Expr makeReal(int line, int col, double real) {
        Expr expr = new Expr(line, col, ExprType.REAL);
        expr.real = real;
        expr.patOrExpr = PatOrExpr.EXPR;
        return expr;
    }

    public static Expr makeInteger(int line, int col, long integer) {
        Expr expr = new Expr(line, col, ExprType.INTEGER);
        expr.integer = integer;
        expr.patOrExpr = PatOrExpr.EXPR;
        return expr;
    }

    public static Expr makeTruth(int line, int col, boolean truth) {
        Expr expr = new Expr(line, col, ExprType.TRUTH);
        expr.truth = truth;
        expr.patOrExpr = PatOrExpr.EXPR;
        return expr;
    }

    public static Expr makeSequence(int line, int col, Expr expr1, Expr expr2) {
        Expr expr = new Expr(line, col, ExprType.SEQ);
        expr.exprs = new Expr[] { expr1, expr2 };
        Patterns.patOrExpr(expr);
        return expr;
    }

    public static Expr makeSequence(int line, int col, Expr expr1, Expr expr2, Expr expr3) {
        Expr expr = new Expr(line, col, ExprType.SEQ);
        expr.exprs = new Expr[] { expr1, expr2, expr3 };
        Patterns.patOrExpr(expr);
        return expr;
    }

    public static Expr makeSequence(int line, int col, Expr ... exprs) {
        Expr expr = new Expr(line, col, ExprType.SEQ);
        expr.exprs = exprs;
        Patterns.patOrExpr(expr);
        return expr;
    }
    
    public static String makeNewName() {
        nameCount++;
        return "&"+nameCount;
    }

    public static Expr makeName(int line, int col, String name) {
        Expr expr = new Expr(line, col, ExprType.NAME);
        expr.name = name;
        expr.patOrExpr = PatOrExpr.EXPR;
        return expr;
    }

    public static Expr makeDottedName(int line, int col, String name, Expr expr1) {
        Expr expr = new Expr(line, col, ExprType.DOTTED_NAME);
        expr.name = name;
        expr.expr1 = expr1;
        expr.patOrExpr = PatOrExpr.PAT;
        return expr;
    }

    public static Expr makeApplication(int line, int col, Expr fun, Expr arg) {
        Expr expr = new Expr(line, col, ExprType.APPLICATION);
        expr.expr1 = fun;
        expr.expr2 = arg;
        Patterns.patOrExpr(expr);
        return expr;
    }

    public static Expr makeComposition(int line, int col, Expr left, Expr right) {
        Expr expr = new Expr(line, col, ExprType.COMPOSITION);
        expr.expr1 = left;
        expr.expr2 = right;
        Patterns.patOrExpr(expr);
        return expr;
    }

    public static Expr makeLambda(int line, int col, Expr pattern, Expr body) {
        Expr expr = new Expr(line, col, ExprType.LAMBDA);
        expr.expr1 = pattern;
        expr.expr2 = body;
        Patterns.patOrExpr(expr);
        return expr;
    }

    public static Expr makeConstruction(int line, int col, Expr[] exprs) {
        Expr expr = new Expr(line, col, ExprType.CONSTRUCTION);
        expr.exprs = exprs;
        Patterns.patOrExpr(expr);
        return expr;
    }

    public static Expr makePredicateConstruction(int line, int col, Expr[] exprs) {
        Expr expr = new Expr(line, col, ExprType.PREDICATE_CONSTRUCTION);
        expr.exprs = exprs;
        Patterns.patOrExpr(expr);
        return expr;
    }

    public static Expr makeGeneralPattern(int line, int col, Expr expr1, Expr[] exprs) {
        Expr expr = new Expr(line, col, ExprType.GENERAL_PATTERN);
        expr.expr1 = expr1;
        expr.exprs = exprs;
        Patterns.patOrExpr(expr);
        return expr;
    }

    public static Expr makeWhere(int line, int col, Expr expr1, Env env) {
        Expr expr = new Expr(line, col, ExprType.WHERE);
        expr.expr1 = expr1;
        expr.env = env;
        Patterns.patOrExpr(expr);
        return expr;
    }

    public static Expr makeCond(int line, int col, Expr cond, Expr ifTrue, Expr ifFalse) {
        Expr expr = new Expr(line, col, ExprType.COND);
        expr.expr1 = cond;
        expr.expr2 = ifTrue;
        expr.expr3 = ifFalse;
        Patterns.patOrExpr(expr);
        return expr;
    }

    public static Expr makeConstant(int line, int col, Expr expr1) {
        Expr expr = new Expr(line, col, ExprType.CONSTANT);
        expr.expr1 = expr1;
        Patterns.patOrExpr(expr);
        return expr;
    }

    public static Expr makePrimed(int line, int col, Expr expr1) {
        Expr expr = new Expr(line, col, ExprType.PRIMED);
        expr.expr1 = expr1;
        Patterns.patOrExpr(expr);
        return expr;
    }

    private Expr(int line, int col, ExprType type) {
        this.type = type;
        this.line = line;
        this.col = col;
    }

    public final ExprType type() {
        return type;
    }

    /**
     * Returns the number of primes (only ['...], p ->' f; g, [|'...|]
     */
    public final int primes() {
        return primes;
    }

    /**
     * Sets the number of primes.
     */
    public final void primes(int primes) {
        this.primes = primes;
    }

    public final char character() {
        return character;
    }

    public final long integer() {
        return integer;
    }

    public final double real() {
        return real;
    }

    public final String string() {
        return string;
    }

    public final String name() {
        return name;
    }

    public final boolean truth() {
        return truth;
    }

    public final Expr expr() {
        return expr1;
    }

    public final Expr lambdaPattern() {
        return expr1;
    }

    public final Expr lambdaBody() {
        return expr2;
    }

    public final Expr fun() {
        return expr1;
    }

    public final Expr arg() {
        return expr2;
    }

    public final Expr left() {
        return expr1;
    }

    public final Expr right() {
        return expr2;
    }

    public final Expr cond() {
        return expr1;
    }

    public final Expr ifTrue() {
        return expr2;
    }

    public final Expr ifFalse() {
        return expr3;
    }

    public final Expr[] exprs() {
        return exprs;
    }

    public final Expr[] patlist() {
        return exprs;
    }

    public final Env env() {
        return env;
    }

    public final void env(Env env) {
        this.env = env;
    }

    public final void patOrExpr(PatOrExpr patOrExpr) {
        this.patOrExpr = patOrExpr;
    }

    public final PatOrExpr patOrExpr() {
        return patOrExpr;
    }
    
    public final boolean ispat() {
        return patOrExpr == PatOrExpr.PAT;
    }

    public final boolean isexpr() {
        return patOrExpr == PatOrExpr.EXPR;
    }

    public void dump(int indent) {
        String typePatOrExpr = type+"["+patOrExpr+","+line+","+col+"]";
        switch (type) {
        case APPLICATION:
            indentln(indent, typePatOrExpr);
            fun().dump(indent+1);
            arg().dump(indent+1);
            break;
        case CHARACTER:
            indentln(indent, typePatOrExpr+": "+character);
            break;
        case COMPOSITION:
            indentln(indent, typePatOrExpr);
            left().dump(indent+1);
            right().dump(indent+1);
            break;
        case COND:
            indentln(indent, typePatOrExpr);
            indentln(indent+1, "condition");
            cond().dump(indent+2);
            indentln(indent+1, "if-true");
            ifTrue().dump(indent+2);
            if (ifFalse() != null) {
                indentln(indent+1, "if-false");
                ifFalse().dump(indent+2);
            }
            break;
        case CONSTANT:
            indentln(indent, typePatOrExpr);
            expr().dump(indent+1);
            break;
        case CONSTRUCTION:
            indentln(indent, typePatOrExpr);
            for (Expr expr : exprs) expr.dump(indent+1);
            break;
        case DOTTED_NAME:
            indentln(indent, typePatOrExpr+": "+name);
            if (expr() != null) expr().dump(indent+1);
            break;
        case GENERAL_PATTERN:
            indentln(indent, typePatOrExpr);
            indentln(indent+1, "expr");
            expr().dump(indent+2);
            indentln(indent+1, "patlist");
            for (Expr expr : patlist()) expr.dump(indent+2);
            break;
        case LAMBDA:
            indentln(indent, typePatOrExpr);
            indentln(indent+1, "pattern");
            lambdaPattern().dump(indent+2);
            indentln(indent+1, "body");
            lambdaBody().dump(indent+2);
            break;
        case NAME:
            indentln(indent, typePatOrExpr+": "+name);
            break;
        case REAL:
            indentln(indent, typePatOrExpr+": "+real);
            break;
        case INTEGER:
            indentln(indent, typePatOrExpr+": "+integer);
            break;
        case PREDICATE_CONSTRUCTION:
            indentln(indent, typePatOrExpr);
            for (Expr expr : exprs()) expr.dump(indent+1);
            break;
        case PRIMED:
            indentln(indent, typePatOrExpr);
            expr().dump(indent+1);
            break;
        case SEQ:
            indentln(indent, typePatOrExpr);
            for (Expr expr : exprs()) expr.dump(indent+1);
            break;
        case STRING:
            indentln(indent, typePatOrExpr+": "+string);
            break;
        case TRUTH:
            indentln(indent, typePatOrExpr+": "+truth);
            break;
        case WHERE:
            indentln(indent, type);
            indentln(indent+1, "expr");
            expr().dump(indent+2);
            indentln(indent+1, "env");
            env.dump(indent+2);
            break;
        }
    }
}
