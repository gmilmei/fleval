package gemi.fl.parser;

public final class Patterns {

    public static enum PatOrExpr {
        PAT,
        EXPR,
        INVALID
    }

    public static PatOrExpr patOrExpr(Expr expr) {
        if (expr == null)
            return PatOrExpr.INVALID;
        else if (expr.patOrExpr() != null)
            return expr.patOrExpr();

        PatOrExpr patOrExpr = PatOrExpr.INVALID;
        switch (expr.type()) {
        case APPLICATION: {
            PatOrExpr midType = patOrExpr(expr.fun());
            if (midType != PatOrExpr.EXPR) {
                patOrExpr = PatOrExpr.INVALID;
                break;
            }

            Expr arg = expr.arg();
            if (arg.type() != ExprType.SEQ || arg.exprs().length != 2) {
                patOrExpr = PatOrExpr.EXPR;
                // TODO: check elements for invalidity
                break;
            }

            PatOrExpr leftType = patOrExpr(arg.exprs()[0]);
            if (leftType == PatOrExpr.INVALID) {
                patOrExpr = PatOrExpr.INVALID;
                break;
            }
            PatOrExpr rightType = patOrExpr(arg.exprs()[1]);
            if (rightType == PatOrExpr.INVALID) {
                patOrExpr = PatOrExpr.INVALID;
                break;
            }

            if (leftType == PatOrExpr.PAT || rightType == PatOrExpr.PAT) {
                patOrExpr = PatOrExpr.PAT;
            }
            else {
                patOrExpr = PatOrExpr.EXPR;
            }
            break;
        }
        case DOTTED_NAME:
            patOrExpr =  PatOrExpr.PAT;
            break;
        case GENERAL_PATTERN: {
            if (patOrExpr(expr.expr()) != PatOrExpr.EXPR) {
                patOrExpr = PatOrExpr.INVALID;
                break;
            }
            boolean containsPat = false;
            boolean invalid = false;
            for (Expr e : expr.patlist()) {
                PatOrExpr t = patOrExpr(e);
                if (t == PatOrExpr.INVALID)
                    invalid = true;
                else if (t == PatOrExpr.PAT)
                    containsPat = true;
            }
            if (invalid)
                patOrExpr = PatOrExpr.INVALID;
            else if (containsPat)
                patOrExpr = PatOrExpr.PAT;
            else
                patOrExpr = PatOrExpr.INVALID;
            break;
        }
        case PREDICATE_CONSTRUCTION: {
            boolean containsPat = false;
            boolean invalid = false;
            for (Expr e : expr.exprs()) {
                PatOrExpr t = patOrExpr(e);
                if (t == PatOrExpr.INVALID)
                    invalid = true;
                else if (t == PatOrExpr.PAT)
                    containsPat = true;
            }
            if (invalid)
                patOrExpr = PatOrExpr.INVALID;
            else if (containsPat)
                patOrExpr = PatOrExpr.PAT;
            else
                patOrExpr = PatOrExpr.EXPR;
            break;
        }
        case LAMBDA:
        case CHARACTER:
        case COMPOSITION:
        case COND:
        case CONSTANT:
        case CONSTRUCTION:
        case INTEGER:
        case NAME:
        case PRIMED:
        case REAL:
        case SEQ:
        case STRING:
        case TRUTH:
        case WHERE:
            patOrExpr = PatOrExpr.EXPR;
            break;
        }
        expr.patOrExpr(patOrExpr);
        return patOrExpr;
    }
}
