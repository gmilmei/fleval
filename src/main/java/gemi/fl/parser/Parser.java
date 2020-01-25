package gemi.fl.parser;

import static gemi.fl.parser.Expr.*;
import static gemi.fl.parser.Patterns.patOrExpr;
import static gemi.fl.scanner.TokenType.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

import gemi.fl.evaluator.Libraries;
import gemi.fl.evaluator.Pattern;
import gemi.fl.parser.Patterns.PatOrExpr;
import gemi.fl.scanner.ErrorHandler;
import gemi.fl.scanner.Names;
import gemi.fl.scanner.Scanner;
import gemi.fl.scanner.Token;
import gemi.fl.scanner.TokenType;

public final class Parser {

    private Scanner scanner;
    private ErrorHandler errorHandler;
    private Token curToken = null;
    private Token lastToken = null;
    private Set<TokenType> startOfExpression = new HashSet<>();

    public Parser(Scanner scanner, ErrorHandler errorHandler) {
        this.scanner = scanner;
        this.errorHandler = errorHandler;
        startOfExpression.add(CHARACTER);
        startOfExpression.add(INTEGER);
        startOfExpression.add(NAME);
        startOfExpression.add(REAL);
        startOfExpression.add(STRING);
        startOfExpression.add(PAREN_LEFT);
        startOfExpression.add(TILDE);
        startOfExpression.add(SEQUENCE_BRACKET_LEFT);
        startOfExpression.add(CONSTRUCTION_BRACKET_LEFT);
        startOfExpression.add(PREDICATE_CONSTRUCTION_LEFT);
        startOfExpression.add(FALSE);
        startOfExpression.add(TRUE);
    }

    public final Expr parse() {
        next();
        Expr expr = expr();
        if (!at(EOF)) error(curToken.line, curToken.col, "spurious text after end of expression");
        return expr;
    }

    public final Env parseEnv() {
        next();
        Env env = env();
        if (!at(EOF)) error(curToken.line, curToken.col, "spurious text after end of expression");
        return env;
    }

    public final Expr expr() {
        return where();
    }

    public final Expr lambda() {
        if (at(LAMBDA)) {
            // lambda (expr) expr
            int line = curToken.line;
            int col = curToken.col;
            Expr pattern = null;
            Expr body = null;
            next();
            if (at(PAREN_LEFT)) {
                next();
                pattern = checkPattern(assertPat(expr()));
                if (at(PAREN_RIGHT)) {
                    next();
                    body = assertExpr(lambda());
                }
                else {
                    error(curToken.line, curToken.col, "expected ), got "+curToken);
                }
            }
            else {
                error(curToken.line, curToken.col, "expected >, got "+curToken);
            }
            return makeLambda(line, col, pattern, body);
        }
        else {
            return where();
        }
    }

    public final Expr where() {
        int line = curToken.line;
        int col = curToken.col;
        Expr expr = rcompose();
        if (at(WHERE)) {
            next();
            Env env = env();
            expr = makeWhere(line, col, expr, env);
        }
        return expr;
    }

    public final Expr rcompose() {
        Expr expr = cond();
        while (atName("!")) {
            Expr op = Expr.makeName(curToken.line, curToken.col, curToken.name);
            next();
            int line = curToken.line;
            int col = curToken.col;
            expr = Expr.makeApplication(line, col, op, Expr.makeSequence(line, col, expr, rcompose()));
        }
        return expr;
    }

    public final Expr cond() {
        int line = curToken.line;
        int col = curToken.col;
        Expr expr = checkPattern(infix());
        if (at(ARROW_RIGHT)) {
            next();
            int primes = 0;
            while (at(PRIME)) {
                primes++;
                next();
            }
            Expr expr1 = infix();
            Expr expr2 = null;
            if (at(SEMICOLON)) {
                next();
                expr2 = cond();
            }
            if (expr.ispat() && primes > 0) {
                error(line, col, "pattern and primes not combinable");
                return makeCond(line, col, expr, expr1, expr2);
            }
            else {
                expr = makeCond(line, col, expr, expr1, expr2);
                expr.primes(primes);
                return expr;
            }
        }
        return expr;
    }

    public final Expr infix() {
        Expr expr = null;
        List<Expr> exprs = new LinkedList<>();
        exprs.add(or());
        while (startOfExpression.contains(curToken.type))
            exprs.add(or());

        if (exprs.size() == 1) {
             expr = exprs.remove(0);
        }
        else if (exprs.size()%2 == 1) {
            Expr left = exprs.remove(0);
            Expr fun = exprs.remove(0);
            Expr right = exprs.remove(0);
            left = Expr.makeApplication(left.line, left.col, fun, Expr.makeSequence(left.line, left.col, left, right));
            while (!exprs.isEmpty()) {
                fun = exprs.remove(0);
                right = exprs.remove(0);
                left = Expr.makeApplication(left.line, left.col, fun, Expr.makeSequence(left.line, left.col, left, right));
            }
            expr = left;
        }
        else {
            error(curToken.line, curToken.col, "invalid infix expression");
            expr = exprs.remove(0);
        }
        patOrExpr(expr);
        return expr;
    }

    public final Expr or() {
        Expr expr = and();
        while (atName(Names.OR)) {
            Expr op = Expr.makeName(curToken.line, curToken.col, curToken.name);
            next();
            int line = curToken.line;
            int col = curToken.col;
            expr = Expr.makeApplication(line, col, op, Expr.makeSequence(line, col, expr, and()));
        }
        return expr;
    }

    public final Expr and() {
        Expr expr = patternappend();
        while (atName(Names.AND)) {
            Expr op = Expr.makeName(curToken.line, curToken.col, curToken.name);
            next();
            int line = curToken.line;
            int col = curToken.col;
            expr = Expr.makeApplication(line, col, op, Expr.makeSequence(line, col, expr, patternappend()));
        }
        return expr;
    }

    public final Expr patternappend() {
        Expr expr = predicateappend();
        while (atName(Names.PATTERN_APPEND_LEFT) || atName(Names.PATTERN_APPEND_RIGHT)) {
            Expr op = Expr.makeName(curToken.line, curToken.col, curToken.name);
            next();
            int line = curToken.line;
            int col = curToken.col;
            expr = Expr.makeApplication(line, col, op, Expr.makeSequence(line, col, expr, patternappend()));
        }
        return expr;
    }

    public final Expr predicateappend() {
        Expr expr = equals();
        while (at(PREDICATE_APPEND_LEFT) || at(PREDICATE_APPEND_RIGHT)) {
            Expr op = Expr.makeName(curToken.line, curToken.col, curToken.name);
            next();
            int line = curToken.line;
            int col = curToken.col;
            expr = Expr.makeApplication(line, col, op, Expr.makeSequence(line, col, expr, predicateappend()));
        }
        return expr;
    }

    public final Expr equals() {
        Expr expr = add();
        while (atName(Names.EQUAL)) {
            Expr op = Expr.makeName(curToken.line, curToken.col, curToken.name);
            next();
            int line = curToken.line;
            int col = curToken.col;
            expr = Expr.makeApplication(line, col, op, Expr.makeSequence(line, col, expr, add()));
        }
        return expr;
    }

    public final Expr add() {
        Expr expr = multiply();
        while (atName(Names.ADD) || atName(Names.SUBTRACT)) {
            Expr op = Expr.makeName(curToken.line, curToken.col, curToken.name);
            next();
            int line = curToken.line;
            int col = curToken.col;
            expr = Expr.makeApplication(line, col, op, Expr.makeSequence(line, col, expr, multiply()));
        }
        return expr;
    }

    public final Expr multiply() {
        Expr expr = compose();
        while (atName(Names.MULTIPLY) || atName(Names.DIVIDE)) {
            Expr op = Expr.makeName(curToken.line, curToken.col, curToken.name);
            next();
            int line = curToken.line;
            int col = curToken.col;
            expr = Expr.makeApplication(line, col, op, Expr.makeSequence(line, col, expr, compose()));
        }
        return expr;
    }

    public final Expr compose() {
        Expr expr = application();
        while (atName(Names.COMPOSE)) {
            next();
            int primes = 0;
            while (at(PRIME)) {
                primes++;
                next();
            }
            expr = Expr.makeComposition(expr.line, expr.col, expr, application());
            expr.primes(primes);
        }
        return expr;
    }

    public final Expr application() {
        Expr expr = constant();
        while (at(APPLICATION_SIGN)) {
            next();
            int primes = 0;
            while (at(PRIME)) {
                primes++;
                next();
            }
            expr = Expr.makeApplication(expr.line, expr.col, expr, constant());
            expr.primes(primes);
        }
        return expr;
    }

    public final Expr constant() {
        int line = curToken.line;
        int col = curToken.col;
        if (at(TILDE)) {
            next();
            int primes = 0;
            while (at(PRIME)) {
                primes++;
                next();
            }
            Expr expr = makeConstant(line, col, assertExpr(constant()));
            for (int i = 0; i < primes; i++) {
                expr = makePrimed(line, col, expr);
            }
            return expr;
        }
        else {
            return prime();
        }
    }

    public final Expr prime() {
        Expr expr = simple();
        while (at(PRIME)) {
            expr = makePrimed(curToken.line, curToken.col, assertExpr(expr));
            next();
        }
        return expr;
    }

    public final Expr simple() {
        Expr expr = null;
        int line = curToken.line;
        int col = curToken.col;
        if (at(LAMBDA)) {
            return lambda();
        }
        else if (at(CHARACTER)) {
            expr = makeCharacter(line, col, curToken.character);
            next();
        }
        else if (at(NAME)) {
            String name = curToken.name;
            next();
            if (at(PERIOD) || at(PERIOD_EXPR)) {
                Expr expr1 = null;
                if (at(PERIOD_EXPR)) {
                    next();
                    expr1 = constant();
                }
                else {
                    next();
                }
                expr = makeDottedName(line, col, name, expr1);
            }
            else {
                expr = makeName(line, col, name);
            }
        }
        else if (at(REAL)) {
            expr = makeReal(line, col, curToken.real);
            next();
        }
        else if (at(INTEGER)) {
            expr = makeInteger(line, col, curToken.integer);
            next();
        }
        else if (at(TRUE)) {
            expr = makeTruth(line, col, true);
            next();
        }
        else if (at(FALSE)) {
            expr = makeTruth(line, col, false);
            next();
        }
        else if (at(STRING)) {
            expr = makeString(line, col, curToken.string);
            next();
        }
        else if (at(SEQUENCE_BRACKET_LEFT)) {
            next();
            List<Expr> exprs = new LinkedList<>();
            if (at(SEQUENCE_BRACKET_RIGHT)) {
                next();
            }
            else {
                while (true) {
                    exprs.add(assertExpr(expr()));
                    if (at(COMMA))
                        next();
                    else
                        break;
                }
                if (at(SEQUENCE_BRACKET_RIGHT))
                    next();
                else
                    error(curToken.line, curToken.col, "expected >, got "+curToken);
            }
            expr = makeSequence(line, col, exprs.toArray(new Expr[exprs.size()]));
        }
        else if (at(PAREN_LEFT)) {
            next();
            expr = expr();
            if (at(PAREN_RIGHT))
                next();
            else
                error(curToken.line, curToken.col, "expected ), got "+curToken);
        }
        else if (at(PREDICATE_CONSTRUCTION_LEFT)) {
            // [| expr, ... |]
            List<Expr> exprs = new LinkedList<>();
            next();
            int primes = 0;
            while (at(PRIME)) {
                primes++;
                next();
            }
            if (!at(PREDICATE_CONSTRUCTION_RIGHT)) while (true) {
                exprs.add(expr());
                if (at(COMMA))
                    next();
                else
                    break;
            }
            if (at(PREDICATE_CONSTRUCTION_RIGHT))
                next();
            else
                error(curToken.line, curToken.col, "expected |], got "+curToken);
            expr = makePredicateConstruction(line, col, exprs.toArray(new Expr[exprs.size()]));
            expr.primes(primes);
        }
        else if (at(CONSTRUCTION_BRACKET_LEFT)) {
            // [ expr, ... ]
            List<Expr> exprs = new LinkedList<>();
            next();
            int primes = 0;
            while (at(PRIME)) {
                primes++;
                next();
            }
            if (!at(CONSTRUCTION_BRACKET_RIGHT)) while (true) {
                exprs.add(assertExpr(expr()));
                if (at(COMMA))
                    next();
                else
                    break;
            }
            if (at(CONSTRUCTION_BRACKET_RIGHT))
                next();
            else
                error(curToken.line, curToken.col, "expected ], got "+curToken);
            expr = makeConstruction(line, col, exprs.toArray(new Expr[exprs.size()]));
            expr.primes(primes);
        }
        else if (at(PAT)) {
            // pat(expr; patlist)
            Expr expr1 = null;
            List<Expr> exprs = new LinkedList<>();
            next();
            if (at(PAREN_LEFT)) {
                next();
                expr1 = expr();
                assertExpr(expr1);
                if (at(SEMICOLON)) {
                    next();
                    while (true) {
                        exprs.add(expr());
                        if (at(COMMA))
                            next();
                        else
                            break;

                    }
                    if (at(PAREN_RIGHT))
                        next();
                    else
                        error(curToken.line, curToken.col, "expected ), got "+curToken);
                }
                else {
                    error(curToken.line, curToken.col, "expected ;, got "+curToken);
                }
            }
            else {
                errorHandler.error(curToken.line, curToken.col, "expected (, got "+curToken);
            }
            expr = makeGeneralPattern(line, col, expr1, exprs.toArray(new Expr[exprs.size()]));
        }
        else if (at(META)) {
            next();
            if (at(PAREN_LEFT)) {
                next();
                Expr expr1 = expr();
                if (expr1 == null) expr1 = makeName(curToken.line, curToken.col, "ERROR");
                assertExpr(expr1);
                if (at(PAREN_RIGHT))
                    next();
                else
                    error(curToken.line, curToken.col, "expected ), got "+curToken);
                expr = makeApplication(line, col, makeName(line, col, "meta"), expr1);
            }
            else {
                error(curToken.line, curToken.col, "expected (, got "+curToken);
            }
        }
        else {
            errorHandler.error(curToken.line, curToken.col, "syntax error");
            expr = makeName(curToken.line, curToken.col, "ERROR");        
        }
        return expr;
    }

    //
    // Environments.
    //

    private final Env env() {
        Env env = simpleenv();

        // binary env composition
        while (at(USES, WHERE, UNION)) {
            EnvType envType = null;
            switch (curToken.type) {
            case USES: envType = EnvType.USES; break;
            case WHERE: envType = EnvType.WHERE; break;
            case UNION: envType = EnvType.UNION; break;
            default: break;
            }
            next();
            int line = curToken.line;
            int col = curToken.col;
            Env other = env();
            Env res = new Env(envType, env, other);
            switch (envType) {           
            case UNION:
                Set<String> inters = new HashSet<>(env.D());
                inters.retainAll(other.D());
                for (String name : inters) errorHandler.error(line, col, "duplicate definition of "+name);
                break;
            default:
                break;
            }
            env = res;
        }

        return env;
    }
    
    private final Env simpleenv() {
        int l = curToken.line;
        int c = curToken.col;
        if (at(BRACE_LEFT)) {                       
            next();
            Env env;
            if (at(DEF, NRDEF, EXDEF, TYPE, ASN, SIG)) {
                env = new Env(EnvType.DEFNLIST);
                defnlist(env);
            }
            else {
                env = env();
            }
            if (at(BRACE_RIGHT))
                next();
            else
                errorHandler.error(curToken.line, curToken.col, "expected }, got "+curToken);
            return env;
        }

        if (at(HIDE, EXPORT, REC)) {
            Env env = null;
            switch (curToken.type) {
            case HIDE: env = new Env(EnvType.HIDE); break;
            case EXPORT: env = new Env(EnvType.EXPORT); break;
            case REC: env = new Env(EnvType.REC); break;
            default: break;
            }
            next();
            Set<String> namelist = new HashSet<>();
            int line = curToken.line;
            int col = curToken.col;
            if (at(PAREN_LEFT)) {
                next();
                while (at(NAME)) {
                    namelist.add(curToken.name);
                    next();
                    if (at(COMMA))
                        next();
                    else
                        break;
                }
                if (namelist.isEmpty())
                    errorHandler.error(line, col, "namelist is empty");
                if (at(PAREN_RIGHT))
                    next();
                else
                    errorHandler.error(curToken.line, curToken.col, "expected ), got "+curToken);
                // update namelist
                Env env1 = env();
                env.env1(env1);
                
                for (String n : namelist) {
                    if (env1.D().contains(n))
                        env.namelist().add(n);
                    else
                        errorHandler.error(line, col, "undefined name "+n);
                }
            }
            else {
                errorHandler.error(curToken.line, curToken.col, "expected (, got "+curToken);
            }
            return env;
        }

        if (at(LIB)) {
            Env env = new Env(EnvType.LIB);
            next();
            if (at(PAREN_LEFT)) {
                next();
                if (at(STRING)) {
                    try {
                        Libraries.load(curToken.string);
                    } catch (FileNotFoundException e) {
                        error(curToken.line, curToken.col, "did not find file "+curToken.string);
                    } catch (IOException e) {
                        error(curToken.line, curToken.col, "cannot load file "+curToken.string);
                    }
                    env.lib(curToken.string);
                    next();
                    if (at(PAREN_RIGHT)) {
                        next();
                    }
                    else {
                        errorHandler.error(curToken.line, curToken.col, "expected ), got "+curToken);
                    }
                }
                else {
                    errorHandler.error(curToken.line, curToken.col, "expected string, got "+curToken);
                }
            }
            else {
                errorHandler.error(curToken.line, curToken.col, "expected (, got "+curToken);
            }
            return env;
        }

        if (at(PF)) {            
            Env env = new Env(EnvType.PF);
            next();
            return env;
        }

        // error
        errorHandler.error(l, c, "unexpected token "+curToken);
        return new Env(EnvType.PF);
    }
    
    private final List<Defn> defnlist(Env env) {
        List<Defn> defnlist = new LinkedList<>();
        env.defnlist(defnlist);
        while (at(DEF, NRDEF, EXDEF, TYPE, ASN, SIG)) {
            Collection<String> D = env.D();
            if (at(DEF, NRDEF)) {
                Defn defn = new Defn(at(DEF)?DefnType.DEF:DefnType.NRDEF);
                next();
                if (at(NAME)) {
                    int line = curToken.line;
                    int col = curToken.col;
                    String name = curToken.name;
                    next();
                    defn.name(name);
                    if (at(PAREN_LEFT) || at(ARROW_LEFT)) {
                        defn.argexp(argexp());
                    }
                    if (at(DEFINITION_SYMBOL)) {
                        next();
                        defn.expr(assertExpr(expr()));
                    }
                    else {
                        errorHandler.error(curToken.line, curToken.col, "expected ==, got "+curToken);
                    }
                    if (D.contains(name)) {
                        errorHandler.error(line, col, "duplicate definition of "+name);
                    }
                    D.add(name);
                    defnlist.add(defn);
                }
                else {
                    errorHandler.error(curToken.line, curToken.col, "expected name, got "+curToken);
                }
            }
            else if (at(EXDEF)) {
                next();
                int line = curToken.line;
                int col = curToken.col;
                Expr pattern = checkPattern(assertPat(simple()));
                ArgExp argexp = null;
                if (at(PAREN_LEFT) || at(ARROW_LEFT)) argexp = argexp();
                Expr expr = null;
                if (at(DEFINITION_SYMBOL)) {
                    next();
                    expr = assertExpr(expr());
                }
                else {
                    errorHandler.error(curToken.line, curToken.col, "expected ==, got "+curToken);
                }
                
                // build expanded "def" definitions
                for (String name : Pattern.names(pattern).keySet()) {
                    Expr signal = Expr.makeComposition(line, col,
                            Expr.makeName(line, col, "signal"),
                            Expr.makeConstruction(line, col, new Expr[] {
                                    Expr.makeConstant(line, col, Expr.makeString(line, col, name)),
                                    Expr.makeConstant(line, col, Expr.makeString(line, col, "range")),
                                    Expr.makeName(line, col, "id")
                            }));
                    Expr cond = Expr.makeCond(line, col, pattern, Expr.makeName(line, col, name), signal);
                    Expr body = Expr.makeComposition(line, col, cond, expr);
                    Defn defn = new Defn(DefnType.DEF);
                    defn.name(name);
                    defn.argexp(argexp);
                    defn.expr(body);
                    if (D.contains(name))
                        errorHandler.error(line, col, "duplicate definition of "+name);
                    D.add(name);
                    defnlist.add(defn);
                }
            }
            else if (at(TYPE)) {
                Defn defn = new Defn(DefnType.TYPE);
                next();
                if (at(NAME)) {
                    int line = curToken.line;
                    int col = curToken.col;
                    String name = curToken.name;
                    defn.name(name);
                    next();
                    if (at(DEFINITION_SYMBOL)) {
                        next();
                        defn.expr(checkPattern(expr()));
                        // handle names defined by the type
                        if (D.contains(name)) errorHandler.error(line, col, "duplicate definition of "+name);
                        D.add(name);
                        if (D.contains("mk"+name)) errorHandler.error(line, col, "duplicate definition of mk"+name);
                        D.add("mk"+name);
                        if (D.contains("is"+name)) errorHandler.error(line, col, "duplicate definition of is"+name);
                        D.add("is"+name);
                        if (D.contains("un"+name)) errorHandler.error(line, col, "duplicate definition of un"+name);
                        D.add("un"+name);
                        for (String n : Pattern.names(defn.expr()).keySet()) {
                            if (D.contains(n)) errorHandler.error(line, col, "duplicate definition of "+n);
                            D.add(n);
                        }
                    }
                    else {
                        errorHandler.error(curToken.line, curToken.col, "syntax error");
                    }
                }
                else {
                    errorHandler.error(curToken.line, curToken.col, "expected name, got "+curToken);
                }
                defnlist.add(defn);
            }
            else if (at(ASN)) {
                Defn defn = new Defn(DefnType.ASN);
                next();
                defn.expr1(expr());
                if (at(DEFINITION_SYMBOL)) {
                    next();
                    defn.expr2(expr());
                }
                else {
                    errorHandler.error(curToken.line, curToken.col, "expected ==, got "+curToken);
                }
                defnlist.add(defn);
            }
            else if (at(SIG)) {
                Defn defn = new Defn(DefnType.SIG);
                next();
                defn.expr1(expr());
                if (!at(SIG_SIGN))
                    errorHandler.error(curToken.line, curToken.col, "expected ::, got "+curToken);
                next();
                defn.expr2(expr());
                defnlist.add(defn);
            }
            else {
                errorHandler.error(curToken.line, curToken.col, "syntax error");
            }
        }
        return defnlist;
    }

    public final ArgExp argexp() {
        if (at(ARROW_LEFT)) {
            next();
            ArgExp argexp = new ArgExp();
            argexp.arrowExpr = expr();
            return argexp;
        }
        ArgExp argexp = new ArgExp();
        while (at(PAREN_LEFT)) {
            next();
            argexp.patterns.add(checkPattern(assertPat(expr())));
            if (at(PAREN_RIGHT))
                next();
            else
                errorHandler.error(curToken.line, curToken.col, "expected ), got "+curToken);
            if (!at(PAREN_LEFT)) break;
        }
        if (at(ARROW_LEFT)) {
            next();
            argexp.arrowExpr = expr();
        }
        return argexp;
    }

    //
    // Utilities.
    //

    private final void next() {
        if (lastToken != null) {
            curToken = lastToken;
            lastToken = null;
        }
        else {
            curToken = scanner.next();
        }
    }

    private final boolean atName(String name) {
        return at(NAME) && curToken.name.equals(name);
    }

    private final boolean at(TokenType tokenType) {
        return curToken.type == tokenType;
    }

    private final boolean at(TokenType ... tokenTypes) {
        for (TokenType tokenType : tokenTypes) {
            if (curToken.type == tokenType)
                return true;
        }
        return false;
    }

    private final Expr assertExpr(Expr expr) {
        if (expr.isexpr())
            return expr;
        error(curToken.line, curToken.col, "expected expression, got pattern");
        expr.patOrExpr(PatOrExpr.EXPR);
        return expr;
    }

    private final Expr assertPat(Expr expr) {
        if (expr.ispat())
            return expr;
        error(curToken.line, curToken.col, "expected pattern, got expression");
        expr.patOrExpr(PatOrExpr.PAT);
        return expr;
    }
    
    private Expr checkPattern(Expr pattern) {        
        if (pattern != null && pattern.ispat()) {
            for (Entry<String,Integer> entry : Pattern.names(pattern).entrySet()) {
            String name = entry.getKey();
            if (entry.getValue() > 1)
                errorHandler.error(pattern.line, pattern.col, "duplicate pattern name "+name);
            }
        }
        return pattern;
    }

    private final void error(int line, int col, String text) {
        errorHandler.error(line, col, text);
    }
}
