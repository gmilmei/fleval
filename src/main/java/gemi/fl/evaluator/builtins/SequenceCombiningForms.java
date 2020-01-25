package gemi.fl.evaluator.builtins;

import static gemi.fl.evaluator.AbnormalType.ARGUMENT_TYPE_ERROR;
import static gemi.fl.evaluator.Value.*;

import java.util.Arrays;

import gemi.fl.evaluator.*;

/**
 * Primitive sequence combining forms.
 */
public class SequenceCombiningForms {

    public static CombiningForm ileft = (form, arg, evaluator, environment) -> {
        if (form.values().length == 0) {
            return makeCombiningForm(form.string(), form.combiningForm(), new Value[] { arg });
        }
        Value fun = form.value(0);
        if (!arg.isseq()) return makeAbnormal(ARGUMENT_TYPE_ERROR, "/l", "arg1", arg);
        Value[] values = arg.values();
        if (values.length == 0) return makeAbnormal(ARGUMENT_TYPE_ERROR, "/l", "arg1", arg);
        if (values.length == 1) return values[0];
        Value res = values[0];
        for (int i = 1; i < values.length; i++) {
            res = evaluator.apply(fun, makePair(res, values[i]), environment);
            if (res.isabnormal()) return res;
        }
        return res;
    };

    public static CombiningForm iright = (form, arg, evaluator, environment) -> {
        if (form.values().length == 0) {
            return makeCombiningForm(form.string(), form.combiningForm(), new Value[] { arg });
        }
        Value fun = form.value(0);
        if (!arg.isseq()) return makeAbnormal(ARGUMENT_TYPE_ERROR, "/r", "arg1", arg);
        Value[] values = arg.values();
        if (values.length == 0) return makeAbnormal(ARGUMENT_TYPE_ERROR, "/r", "arg1", arg);
        if (values.length == 1) return values[0];
        Value res = values[values.length-1];
        for (int i = values.length-2; i >= 0; i--) {
            res = evaluator.apply(fun, makePair(values[i], res), environment);
            if (res.isabnormal()) return res;
        }
        return res;
    };

    public static CombiningForm tree = (form, arg, evaluator, environment) -> {        
        if (form.values().length == 0) {
            return makeCombiningForm(form.string(), form.combiningForm(), new Value[] { arg });
        }
        Value fun = form.value(0);
        if (!arg.isseq()) return makeAbnormal(ARGUMENT_TYPE_ERROR, "tree", "arg1", arg);
        Value[] values = arg.values();
        if (values.length == 0) return makeAbnormal(ARGUMENT_TYPE_ERROR, "tree", "arg1", arg);
        if (values.length == 1) return values[0];
        return _tree(fun, values, evaluator, environment);
    };
    
    private static Value _tree(Value fun, Value[] values, NaiveEvaluator evaluator, Environment environment) {
        if (values.length == 1) {
            return values[0];
        }
        if (values.length == 2) {
            return evaluator.apply(fun, makeSequence(values), environment);
        }
        int k = (int)Math.ceil(values.length/2.0);
        Value[] x1 = Arrays.copyOfRange(values, 0, k);
        Value[] x2 = Arrays.copyOfRange(values, k, values.length);
        Value res1 = _tree(fun, x1, evaluator, environment);
        Value res2 = _tree(fun, x2, evaluator, environment);
        return evaluator.apply(fun, makePair(res1, res2), environment);
    }

    public static CombiningForm alpha = (form, arg, evaluator, environment) -> {        
        if (form.values().length == 0) {
            return makeCombiningForm(form.string(), form.combiningForm(), new Value[] { arg});
        }
        Value fun = form.value(0);
        if (!arg.isseq()) return makeAbnormal(ARGUMENT_TYPE_ERROR, "alpha", "arg1", arg);
        Value[] values = arg.values();
        if (values.length == 0) return arg;
        Value[] res = new Value[values.length];
        for (int i = 0; i < res.length; i++) {
            res[i] = evaluator.apply(fun, values[i], environment);
            if (res[i].isabnormal()) return res[i];
        }
        return makeSequence(res);
    };

    public static CombiningForm merge = (form, arg, evaluator, environment) -> {
        if (form.values().length == 0) {
            return makeCombiningForm(form.string(), form.combiningForm(), new Value[] { arg });
        }
        Value fun = form.value(0);
        if (!arg.isseq() || arg.values().length != 2)
            return makeAbnormal(ARGUMENT_TYPE_ERROR, "merge", "arg1", arg);        
        if (!arg.value(0).isseq())
            return makeAbnormal(ARGUMENT_TYPE_ERROR, "merge", "arg1", arg);        
        if (!arg.value(1).isseq())
            return makeAbnormal(ARGUMENT_TYPE_ERROR, "merge", "arg1", arg);

        Value[] x = arg.value(0).values();
        Value[] y = arg.value(1).values();
        Value[] res = new Value[x.length+y.length];
        int ix = 0;
        int iy = 0;
        int ires = 0;
        while (ix < x.length && iy < y.length) {
            Value pair = makePair(x[ix], y[iy]);
            Value r = evaluator.apply(fun, pair, environment);
            if (r.istype(ValueType.TRUTH)) {
                if (r.istrue())
                    res[ires] = x[ix++];                   
                else
                    res[ires] = y[iy++];
                ires++;
            }
            else {
                return r;
            }
        }
        while (ix < x.length) res[ires++] = x[ix++];
        while (iy < y.length) res[ires++] = y[iy++];
        
        return makeSequence(res);
    };
}
