package gemi.fl.evaluator;

import static gemi.fl.evaluator.Value.*;

import java.util.List;
import java.util.Map.Entry;
import java.util.Random;

import gemi.fl.evaluator.builtins.CombiningForms;
import gemi.fl.evaluator.builtins.PredicateCombiningForms;
import gemi.fl.evaluator.builtins.UsertypeFunctions;
import gemi.fl.parser.Defn;
import gemi.fl.parser.Env;
import gemi.fl.parser.EnvType;
import gemi.fl.parser.Expr;

public final class NaiveEvaluator {

    private Environment env;

    public NaiveEvaluator(Environment env) {
        this.env = env;
    }

    public final Value evaluate(Expr expr) {
        return evaluate(expr, env);
    }

    /**
     * Evaluates <code>expr</code> in <code>environment</code>.
     */
    public final Value evaluate(Expr expr, Environment environment) {
        if (expr == null) return null;
        switch (expr.type()) {
        case CHARACTER: {
            return makeCharacter(expr.character());
        }
        case INTEGER: {
            return makeInteger(expr.integer());
        }
        case REAL: {
            return makeReal(expr.real());
        }
        case TRUTH: {
            return makeTruth(expr.truth());
        }
        case STRING: {
            return makeString(expr.string());
        }
        case CONSTANT: {
            Value value = evaluate(expr.expr(), environment);
            return CombiningForms.makeConstant(value, this, environment);
        }
        case APPLICATION: {
            Value function = evaluate(expr.fun(), environment);
            if (function.isabnormal()) return function;
            Value arg = evaluate(expr.arg(), environment);
            if (arg.isabnormal()) return arg;
            if (expr.primes() == 0) {
                return apply(function, arg, environment);
            }
            else {
                int primes = expr.primes();
                Value f = CombiningForms.applyValue;
                while (primes > 0) {
                    f = CombiningForms.makeLifted(f, this, environment);
                    primes--;
                }
                return apply(f, makePair(function,arg), environment);
            }
        }
        case COMPOSITION: {
            Value left = evaluate(expr.left(), environment);
            if (left.isabnormal()) return left;
            Value right = evaluate(expr.right(), environment);
            if (right.isabnormal()) return right;
            if (expr.primes() == 0) {
                return CombiningForms.makeCompose(makePair(left, right), this, environment);
            }
            else {
                int primes = expr.primes();
                Value f = CombiningForms.composeValue;
                while (primes > 0) {
                    f = CombiningForms.makeLifted(f, this, environment);
                    primes--;
                }
                return apply(f, makePair(left, right), environment);
            }
        }
        case COND: {
            if (expr.cond().ispat()) {
                Pattern pat = Pattern.makePattern(expr.cond(), this, environment);
                if (pat == null) return makeAbnormal(AbnormalType.PATTERN_ERROR, "cond", "pat", makeSequence(new Value[] {}));
                Environment e = new Environment(EnvType.DEFNLIST, environment);
                e.bind(pat.functions());
                Value ifTrue = evaluate(expr.ifTrue(), e);
                if (ifTrue.isabnormal()) return ifTrue;
                Value ifFalse = null;
                if (expr.ifFalse() != null) {
                    ifFalse = evaluate(expr.ifFalse(), e);
                    if (ifFalse.isabnormal()) return ifFalse;
                }
                return makeFunction("&", pat.predicate(), ifTrue, ifFalse);
            }
            else {
                // TODO: handle primes
                Value cond = evaluate(expr.cond(), environment);
                if (cond.isabnormal()) return cond;
                Value ifTrue = evaluate(expr.ifTrue(), environment);
                if (ifTrue.isabnormal()) return ifTrue;
                if (expr.ifFalse() != null) {
                    Value ifFalse = evaluate(expr.ifFalse(), environment);
                    if (ifFalse.isabnormal()) return ifFalse;
                    return CombiningForms.makeCond(makeTriple(cond, ifTrue, ifFalse), this, environment);
                }
                return CombiningForms.makeCond(makePair(cond, ifTrue), this, environment);
            }
        }
        case CONSTRUCTION: {
            Value[] values = new Value[expr.exprs().length];
            for (int i = 0; i < values.length; i++) {
                values[i] = evaluate(expr.exprs()[i], environment);
                if (values[i].isabnormal()) return values[i];
            }
            Value seq = makeSequence(values);
            if (expr.primes() == 0) {
                return CombiningForms.makeCons(seq, this, environment);
            }
            else {
                int primes = expr.primes();
                Value f = CombiningForms.consValue;
                while (primes > 0) {
                    f = CombiningForms.makeLifted(f, this, environment);
                    primes--;
                }
                return apply(f, seq, environment);
            }
        }
        case PREDICATE_CONSTRUCTION: {
            Value[] values = new Value[expr.exprs().length];
            for (int i = 0; i < values.length; i++) {
                values[i] = evaluate(expr.exprs()[i], environment);
                if (values[i].isabnormal()) return values[i];
            }
            Value seq = makeSequence(values);
            if (expr.primes() == 0) {
                return PredicateCombiningForms.makePcons(makeSequence(values), this, environment);
            }
            else {
                int primes = expr.primes();
                Value f = PredicateCombiningForms.pconsValue;
                while (primes > 0) {
                    f = CombiningForms.makeLifted(f, this, environment);
                    primes--;
                }
                return apply(f, seq, environment);
            }
        }
        case LAMBDA: {
            if (expr.lambdaPattern().ispat())
                return makeLambda(expr.lambdaPattern(), expr.lambdaBody(), environment);
            else
                return makeAbnormal(AbnormalType.PATTERN_ERROR, "eval", "lambda", makeString("pattern"));
        }
        case NAME: {
            String name = expr.name();
            Value value = environment.lookup(name);
            if (value == null)
                return makeAbnormal(AbnormalType.UNDEFINED_NAME, "eval", "no such function", makeString(name));
            else
                return value;
        }
        case PRIMED: {
            Value value = evaluate(expr.expr(), environment);
            if (value.isabnormal()) return value;
            return CombiningForms.makeLifted(value, this, environment);
        }
        case SEQ: {
            Value[] seq = new Value[expr.exprs().length];
            int n = 0;
            for (Expr e : expr.exprs()) {
                Value res = evaluate(e, environment);
                if (res.isabnormal()) return res;
                seq[n++] = res;
            }
            return makeSequence(seq);
        }
        case WHERE: {
            return evaluate(expr.expr(), evaluateEnv(expr.env(), environment));
        }
        case GENERAL_PATTERN: {
            return makeAbnormal(AbnormalType.OTHER, "eval", "pattern", makeString(expr.toString()));
        }
        case DOTTED_NAME: {
            return makeAbnormal(AbnormalType.OTHER, "eval", "pattern", makeString(expr.toString()));
        }
        default:
            return makeAbnormal(AbnormalType.OTHER, "eval", "other", makeString(expr.toString()));
        }
    }

    /**
     * Applies <code>applicable</code> to <code>arg</code> in <code>environment.
     */
    public final Value apply(Value applicable, Value arg, Environment environment) {
        switch (applicable.type()) {
        case FUNCTION: {
            return applyFunction(applicable, arg, environment);
        }
        case LAMBDA: {
            return applyLambda(applicable, arg, environment);
        }
        case COMB: {
            return applicable.combiningForm().apply(applicable, arg, this, environment);
        }
        default:
            return makeAbnormal(AbnormalType.NON_APPLICABLE_ERROR, "eval", "apply", makePair(applicable,arg));
        }
    }
    
    public final Value applyFunction(Value applicable, Value arg, Environment environment) {        
        if (applicable.primitiveFunction() != null)
            return applicable.primitiveFunction().apply(arg, this, environment);

        Value[] values = applicable.values();
        if (values != null) {
            Value pattern = values[0];
            Value cond = apply(pattern, arg, environment);
            if (cond.istrue())
                return apply(values[1], arg, environment);
            else if (values[2] != null)
                return apply(values[2], arg, environment);
            else
                return makeAbnormal(AbnormalType.PATTERN_ERROR, "apply", "pat", applicable);
        }
        else if (applicable.pattern() == null) {
            Value fun = evaluate(applicable.iftrueexpr(), applicable.environment());
            return apply(fun, arg, environment);
        }
        else {
            // function has <- pattern
            Expr pattern = applicable.pattern();
            String name = applicable.name();
            environment = applicable.environment();
            Pattern pat = Pattern.makePattern(pattern, this, environment);        
            if (pat == null) return makeAbnormal(AbnormalType.PATTERN_ERROR, name, "arg1", arg);
            Value cond = apply(pat.predicate(), arg, environment);
            if (cond.istrue()) {
                environment = new Environment(EnvType.DEFNLIST, environment);
                environment.bind(pat.functions());
                if (applicable.iftruevalue() != null)
                    return apply(applicable.iftruevalue(), arg, environment);
                else
                    return apply(evaluate(applicable.iftrueexpr(), environment), arg, environment);            
            }
            else {
                return makeAbnormal(AbnormalType.PATTERN_ERROR, "apply", "pat", applicable);
            }
        }
    }
    
    public final Value applyLambda(Value applicable, Value arg, Environment environment) {
        environment = applicable.environment();
        Pattern pattern = Pattern.makePattern(applicable.pattern(), this, environment);
        if (pattern == null)
            return makeAbnormal(AbnormalType.PATTERN_ERROR, "apply", "pat", applicable);
        if (!apply(pattern.predicate(), arg, environment).istrue())
            return makeAbnormal(AbnormalType.PATTERN_ERROR, "lambda", "arg", arg);
        Environment e = new Environment(EnvType.DEFNLIST, environment);
        for (Entry<String,Value> f : pattern.functionSet()) {
            String n = f.getKey();
            Value fun = f.getValue();
            Value v = apply(fun, arg, environment);
            e.bind(n, v);
        }
        return evaluate(applicable.body(), e);
    }

    /**
     * Evaluates <code>env</code> in <code>environment</code>.
     */
    public final Environment evaluateEnv(Env env, Environment environment) {
        switch (env.type()) {
        case DEFNLIST:
            return evaluateDefnList(env.defnlist(), environment);
        case EXPORT:
            return new Environment(EnvType.EXPORT, env.namelist(), evaluateEnv(env.env1(), environment), environment);
        case HIDE:
            return new Environment(EnvType.HIDE, env.namelist(), evaluateEnv(env.env1(), environment), environment);
        case LIB:
            Library library = Libraries.get(env.lib());
            return library.environment();
        case PF:
            return PrimitiveFunctions.environment;
        case REC: {
            Environment e1 = new Environment(EnvType.REC, env.namelist(), null, environment);
            Environment e2 = evaluateEnv(env.env1(), e1);
            e1.env1(e2);
            return e2;
        }
        case UNION: {
            Environment e1 = evaluateEnv(env.env1(), environment);
            Environment e2 = evaluateEnv(env.env2(), environment);
            return new Environment(EnvType.UNION, e1, e2, environment);
        }
        case USES: {
            Environment e2 = evaluateEnv(env.env2(), environment);
            Environment e1 = evaluateEnv(env.env1(), e2);
            return new Environment(EnvType.USES, e1, e2, environment);
        }
        case WHERE:
            Environment e2 = evaluateEnv(env.env2(), environment);
            Environment e1 = evaluateEnv(env.env1(), e2);
            return new Environment(EnvType.WHERE, e1, e2, environment);
        case ABNORMAL:
            return environment;
        }
        return environment;
    }

    private final Environment evaluateDefnList(List<Defn> defnlist, Environment next) {
        Environment environment = new Environment(EnvType.DEFNLIST, next);
        for (Defn defn : defnlist) {
            String name = defn.name();
            switch (defn.type()) {
            case DEF:
                next = environment;
            case NRDEF: {
                if (defn.argexp() == null) {
                    Value fun = makeFunction(name, null, defn.expr(), null, next);
                    environment.bind(name, fun);
                }
                else {
                    List<Expr> patterns = defn.argexp().patterns;
                    Expr arrowExpr = defn.argexp().arrowExpr;
                    Expr body = null;
                    if (arrowExpr != null)
                        body = Expr.makeCond(defn.expr().line, defn.expr().col, arrowExpr, defn.expr(), null);                    
                    else
                        body = defn.expr();
                    for (int i = patterns.size()-1; i >= 0; i--) {
                        body = Expr.makeLambda(body.line, body.col, patterns.get(i), body);
                    }
                    Value fun = makeFunction(name, null, body, null, next);
                    environment.bind(name, fun);
                }
                break;
            }
            case TYPE: {
                Pattern pattern = Pattern.makePattern(defn.expr(), this, next);
                if (pattern == null) {
                    environment = Environment.makeAbnormal(makeString("pattern error in type"), next);
                    return environment;
                }
                long tag = new Random().nextLong();
                String consname = "mk"+name;
                Value usertypeConstructor = UsertypeFunctions.makeUsertypeConstructor(name, consname, tag);
                environment.bind(consname, makeFunction(consname, pattern.predicate(), usertypeConstructor, null));
                String deconsname = "un"+name;
                Value usertypeDeconstructor  = UsertypeFunctions.makeUsertypeDeconstructor(name, consname, tag);
                environment.bind(deconsname, usertypeDeconstructor);
                String testname = "is"+name;
                Value usertypeTest = UsertypeFunctions.makeUsertypeTest(name, testname, tag);
                environment.bind(testname, usertypeTest);
                for (Entry<String,Value> f : pattern.functionSet()) {
                    String selname = f.getKey();
                    Value sel = UsertypeFunctions.makeUsertypeSelector(name, selname, tag, f.getValue());
                    environment.bind(selname, sel);
                }
                break;
            }
            case ASN:
                // Ignored
                break;
            case SIG:
                // Ignored
                break;
            }
        }
        return environment;
    }
}
