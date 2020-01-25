package gemi.fl.evaluator;

import static gemi.fl.evaluator.PatternCombiners.*;
import static gemi.fl.evaluator.builtins.MiscFunctions.idValue;
import static gemi.fl.evaluator.builtins.Predicates.ttValue;
import static gemi.fl.parser.Utilities.indentln;

import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import gemi.fl.parser.Expr;
import gemi.fl.parser.Parser;
import gemi.fl.scanner.ErrorHandler;
import gemi.fl.scanner.Names;
import gemi.fl.scanner.Scanner;

public final class Pattern {

    public static Map<String,PatternCombiner> patternEnv = new HashMap<>();
    
    private Map<String,Value> functions = new HashMap<>();
    private Value predicate = null;
    
    public static Pattern makePattern(Expr expr, NaiveEvaluator evaluator, Environment environment) {
        switch (expr.type()) {
        case DOTTED_NAME: {
            Pattern pattern = new Pattern();
            pattern.functions.put(expr.name(), idValue);
            if (expr.expr() != null)
                pattern.predicate = evaluator.evaluate(expr.expr(), environment);
            else
                pattern.predicate = ttValue;
            return pattern;
        }
        case PREDICATE_CONSTRUCTION: {
            Expr[] exprs = expr.exprs(); 
            Pattern[] patterns = new Pattern[exprs.length];
            for (int i = 0; i < patterns.length; i++) {
                patterns[i] = makePattern(exprs[i], evaluator, environment);
                if (patterns[i] == null) return null;
            }
            return patcons.apply(patterns, evaluator, environment);
        }
        case APPLICATION: {
            if (expr.isexpr()) {
                Pattern pattern = new Pattern();
                Value fun = evaluator.evaluate(expr, environment);
                if (fun.isabnormal()) return null;
                pattern.predicate = fun;
                return pattern;
            }
            else {
                Expr fun = expr.fun();
                Expr arg = expr.arg();
                Expr[] args = arg.exprs();
                if (args == null || args.length != 2) return null;
                Pattern p1 = makePattern(arg.exprs()[0], evaluator, environment);
                Pattern p2 = makePattern(arg.exprs()[1], evaluator, environment);
                PatternCombiner patternCombiner = patternEnv.get(fun.name());
                if (patternCombiner != null)
                    return patternCombiner.apply(new Pattern[] { p1, p2 }, evaluator, environment);
                else
                    return null;
            }
        }
        case GENERAL_PATTERN: {
            Expr fun = expr.expr();
            Expr[] exprs = expr.exprs();
            Pattern[] patterns = new Pattern[exprs.length];
            for (int i = 0; i < patterns.length; i++) {
                patterns[i] = makePattern(exprs[i], evaluator, environment);
                if (patterns[i] == null) return null;
            }
            PatternCombiner patternCombiner = patternEnv.get(fun.name());
            if (patternCombiner != null)
                return patternCombiner.apply(patterns, evaluator, environment);
            else
                return null;
        }
        default:
            if (expr.isexpr()) {
                Pattern pattern = new Pattern();
                Value fun = evaluator.evaluate(expr, environment);
                if (fun.isabnormal()) return null;
                pattern.predicate = fun;
                return pattern;
            }
            return null;
        }
    }
    
    public static Map<String,Integer> names(Expr expr) {
        Map<String,Integer> names = new HashMap<>();
        if (expr != null) names(expr, names);
        return names;
    }
    
    private static void names(Expr expr, Map<String,Integer> names) {
        if (expr == null) return;
        switch (expr.type()) {
        case DOTTED_NAME: {
            Integer count = names.get(expr.name());
            if (count == null) count = 0;
            names.put(expr.name(), count+1);
            break;
        }
        case PREDICATE_CONSTRUCTION: {
            for (Expr e : expr.exprs()) names(e, names);
            break;
        }
        case APPLICATION: {
            names(expr.fun(), names);
            names(expr.arg(), names);
            Expr[] args = expr.arg().exprs();
            if (args == null) break;
            for (Expr e : args) names(e, names);
            break;
        }
        case GENERAL_PATTERN: {
            Expr fun = expr.expr();
            Expr[] exprs = expr.exprs();
            names(fun, names);
            for (Expr e : exprs) names(e, names);
            break;
        }
        case COMPOSITION: {
            names(expr.left(), names);
            names(expr.right(), names);
            break;
        }
        case COND: {
            names(expr.cond(), names);
            names(expr.ifTrue(), names);
            names(expr.ifFalse(), names);
            break;
        }
        case CONSTRUCTION: {
            for (Expr e : expr.exprs()) names(e, names);
            break;
        }
        case PRIMED: {
            names(expr.expr(), names);
            break;
        }
        default:
            break;
        }
    }
    
    public final Map<String,Value> functions() {
        return functions;
    }

    public final Set<Entry<String,Value>> functionSet() {
        return functions.entrySet();
    }

    public final Value predicate() {
        return predicate;
    }

    public final void predicate(Value predicate) {
        this.predicate = predicate;
    }

    public final void bind(String name, Value value) {
        functions.put(name, value);
    }

    public final boolean bindAll(Map<String,Value> bindings) {
        for (Entry<String,Value> entry : bindings.entrySet()) {
            if (functions.containsKey(entry.getKey()))
                return false;
            functions.put(entry.getKey(), entry.getValue());
        }
        return false;
    }

    public final void dump() {
        System.out.println("Functions:");
        for (Entry<String,Value> entry : functions.entrySet()) {
            indentln(1, entry.getKey()+": "+entry.getValue());
        }
        System.out.println("Predicate:");
        indentln(1, predicate);
    }
    
    static {
        patternEnv.put("pand", pand);
        patternEnv.put("por", por);
        patternEnv.put("pcomb", pcomp);
        patternEnv.put("patcons", patcons);
        patternEnv.put(Names.PATTERN_APPEND_LEFT, patAppendLeft);
        patternEnv.put(Names.PATTERN_APPEND_RIGHT, patAppendRight);
    }
    
    public static void main(String[] args) throws Exception {
        String filename = "work/test.fl";
        ErrorHandler errorHandler = new ErrorHandler(filename, System.err);
        Scanner scanner = new Scanner(new FileReader(filename), errorHandler);
        Parser parser = new Parser(scanner, errorHandler);
        Expr expr = parser.parse();
        expr = expr.cond();
        expr.dump(0);
        NaiveEvaluator evaluator = new NaiveEvaluator(PrimitiveFunctions.environment);
        Pattern pattern = Pattern.makePattern(expr, evaluator, PrimitiveFunctions.environment);
        pattern.dump();
    }
}
