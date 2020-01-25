package gemi.fl.evaluator.builtins;

import static gemi.fl.evaluator.AbnormalType.*;
import static gemi.fl.evaluator.Value.makeAbnormal;
import static gemi.fl.evaluator.Value.makeCombiningForm;
import static gemi.fl.evaluator.Value.makeTruth;
import static gemi.fl.evaluator.Value.*;

import java.util.Arrays;

import gemi.fl.evaluator.*;

/**
 * Primitive predicate combining forms. 
 */
public class PredicateCombiningForms {

    public static CombiningForm pcons = (form, arg, evaluator, environment) -> {        
        Value[] values = form.values();
        if (values.length == 0) {
            if (!arg.isseq()) 
                return makeAbnormal(ARGUMENT_TYPE_ERROR, "pcons", "arg1", arg);
            else
                return makeCombiningForm(form.string(), form.combiningForm(), new Value[] { arg });
        }
        if (!arg.isseq())
            return makeAbnormal(ARGUMENT_COUNT_ERROR, "pcons", "arg2", arg);
        Value[] preds = values[0].values();
        Value[] args = arg.values();
        if (args.length != preds.length)
            return FALSE;
        for (int i = 0; i < preds.length; i++) {
            Value res = evaluator.apply(preds[i], args[i], environment);
            if (res.isabnormal()) return res;
            if (!res.istrue()) return FALSE;
        }
        return TRUE;
    };
    
    public static Value pconsValue = makeCombiningForm("pcons", pcons);

    public static Value makePcons(Value value, NaiveEvaluator evaluator, Environment environment) {
            return pcons.apply(pconsValue, value, evaluator, environment);
    }

    public static CombiningForm equal = (form, arg, evaluator, environment) -> {        
        Value[] values = form.values();
        if (values.length == 0) {
            if (!arg.isseq() || arg.values().length != 2)
                return makeAbnormal(ARGUMENT_COUNT_ERROR, "=", "arg1", arg);
            return makeCombiningForm(form.string(), form.combiningForm(), new Value[] { arg });
        }
        Value f = form.value(0).value(0);
        Value g = form.value(0).value(1);
        Value x = evaluator.apply(f, arg, environment);
        if (x.isabnormal()) return x;
        Value y = evaluator.apply(g, arg, environment);
        if (y.isabnormal()) return x;
        return makeTruth(x.equals(y));
    };

    public static CombiningForm seqof = (form, arg, evaluator, environment) -> {        
        Value[] values = form.values();
        if (values.length == 0) {
            return makeCombiningForm(form.string(), form.combiningForm(), new Value[] { arg });
        }
        if (!arg.isseq()) return makeTruth(false);
        Value fun = form.value(0);
        for (Value value : arg.values()) {
            Value res = evaluator.apply(fun, value, environment);
            if (res.isabnormal()) return res;
            if (!res.istrue()) return makeTruth(false);
        }
        return makeTruth(true);
    };

    public static CombiningForm eqto = (form, arg, evaluator, environment) -> {        
        Value[] values = form.values();
        if (values.length == 0) {
            return makeCombiningForm(form.string(), form.combiningForm(), new Value[] { arg });
        }
        Value x = values[0];
        return makeTruth(x.compare(arg) == Compare.EQUAL);
    };

    public static CombiningForm lenis = (form, arg, evaluator, environment) -> {        
        Value[] values = form.values();
        if (values.length == 0) {
            if (!arg.istype(ValueType.INTEGER)) return makeAbnormal(ARGUMENT_TYPE_ERROR, "lenis", "arg1", arg);
            return makeCombiningForm(form.string(), form.combiningForm(), new Value[] { arg });
        }
        Value x = values[0];
        if (!arg.isseq()) return makeTruth(false);
        return makeTruth(arg.values().length == x.integer());
    };

    public static CombiningForm pleft = (form, arg, evaluator, environment) -> {        
        Value[] values = form.values();
        if (values.length == 0) {
            if (!arg.isseq() || arg.values().length != 2) return makeAbnormal(ARGUMENT_TYPE_ERROR, "|->", "arg1", arg);
            return makeCombiningForm(form.string(), form.combiningForm(), new Value[] { arg });
        }
        Value p = values[0].value(0);
        Value q = values[0].value(1);        
        if (!arg.isseq() || arg.values().length == 0) return makeTruth(false);
        Value res = evaluator.apply(p, arg.value(0), environment);
        if (res.istrue()) {
            Value seq = makeSequence(Arrays.copyOfRange(arg.values(), 1, arg.values().length));
            res = evaluator.apply(q, seq, environment);            
            if (res.isabnormal())
                return res;
            else
                return makeTruth(res.istrue());
        }
        return makeTruth(false);
    };
    
    public static Value pleftValue = makeCombiningForm("|->", pleft);

    public static Value makePleft(Value value, NaiveEvaluator evaluator, Environment environment) {
            return pleft.apply(pleftValue, value, evaluator, environment);
    }

    public static CombiningForm pright = (form, arg, evaluator, environment) -> {
        Value[] values = form.values();
        if (values.length == 0) {
            if (!arg.isseq() || arg.values().length != 2) return makeAbnormal(ARGUMENT_TYPE_ERROR, "|->", "arg1", arg);
            return makeCombiningForm(form.string(), form.combiningForm(), new Value[] { arg });
        }
        Value p = values[0].value(0);
        Value q = values[0].value(1);
        if (!arg.isseq() || arg.values().length == 0) return makeTruth(false);
        Value seq = makeSequence(Arrays.copyOfRange(arg.values(), 0, arg.values().length-1));
        Value res = evaluator.apply(p, seq, environment);
        if (res.istrue()) {
            res = evaluator.apply(q, arg.value(arg.values().length-1), environment);            
            if (res.isabnormal())
                return res;
            else
                return makeTruth(res.istrue());
        }
        return makeTruth(false);
    };

    public static Value prightValue = makeCombiningForm("<-|", pright);

    public static Value makePright(Value value, NaiveEvaluator evaluator, Environment environment) {
            return pright.apply(prightValue, value, evaluator, environment);
    }
}
