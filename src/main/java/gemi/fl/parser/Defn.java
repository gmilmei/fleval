package gemi.fl.parser;

import static gemi.fl.parser.Utilities.*;

public final class Defn {

    private DefnType type;

    private String name = null;
    private Expr expr = null;
    private Expr expr1 = null;
    private Expr expr2 = null;
    private ArgExp argexp = null;

    public Defn(DefnType type) {
        this.type = type;
    }
    
    public final DefnType type() {
        return type;
    }
    
    public final void type(DefnType type) {
        this.type = type;
    }
    
    public final String name() {
        return name;
    }
    
    public final void name(String name) {
        this.name = name;
    }

    public final ArgExp argexp() {
        return argexp;
    }

    public final void argexp(ArgExp argexp) {
        this.argexp = argexp;
    }

    public final Expr expr() {
        return expr;
    }

    public final void expr(Expr expr) {
        this.expr = expr;
    }

    public final Expr expr1() {
        return expr1;
    }

    public final void expr1(Expr expr) {
        this.expr1 = expr;
    }

    public final Expr expr2() {
        return expr2;
    }

    public final void expr2(Expr expr) {
        this.expr2 = expr;
    }

    public final void dump(int indent) {
        switch (type) {
        case ASN:
            indentln(indent, type);
            expr1.dump(indent+1);
            expr2.dump(indent+1);
            break;
        case DEF:
        case NRDEF:
            indentln(indent, type+": "+name);
            if (argexp != null) {
                indentln(indent+1, "argexp");
                argexp.dump(indent+2);
            }
            indentln(indent+1, "expr");
            expr.dump(indent+2);
            break;
        case SIG:
            indentln(indent, type);
            indentln(indent+1, "expr");
            expr1.dump(indent+2);
            indentln(indent+1, "mpred");
            expr2.dump(indent+2);
            break;
        case TYPE:
            indentln(indent, type+": "+name);
            indentln(indent+1, "expr");
            expr.dump(indent+2);
            break;
        }
    }
}
