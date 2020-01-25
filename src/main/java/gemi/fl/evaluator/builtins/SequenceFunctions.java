package gemi.fl.evaluator.builtins;

import static gemi.fl.evaluator.AbnormalType.ARGUMENT_COUNT_ERROR;
import static gemi.fl.evaluator.AbnormalType.ARGUMENT_TYPE_ERROR;
import static gemi.fl.evaluator.AbnormalType.INDEX_ERROR;
import static gemi.fl.evaluator.Value.*;

import java.util.Arrays;

import gemi.fl.evaluator.PrimitiveFunction;
import gemi.fl.evaluator.Value;
import gemi.fl.evaluator.ValueType;

/**
 * Primtive sequence functions.
 */
public final class SequenceFunctions {

    public static PrimitiveFunction al = (arg, evaluator, environment) -> {
        if (!arg.isseq())
            return makeAbnormal(ARGUMENT_TYPE_ERROR, "al", "arg1", arg);
        if (arg.values().length != 2)
            return makeAbnormal(ARGUMENT_COUNT_ERROR, "al", "arg1", arg);
        if (!arg.value(1).isseq())
            return makeAbnormal(ARGUMENT_TYPE_ERROR, "al", "arg1", arg);
        Value x = arg.value(0);
        Value[] y = arg.value(1).values();
        Value[] res = new Value[1+y.length];
        res[0] = x;
        for (int i = 0; i < y.length; i++) res[i+1] = y[i];
        return makeSequence(res);
    };

    public static PrimitiveFunction ar = (arg, evaluator, environment) -> {
        if (!arg.isseq())
            return makeAbnormal(ARGUMENT_TYPE_ERROR, "ar", "arg1", arg);
        if (arg.values().length != 2)
            return makeAbnormal(ARGUMENT_COUNT_ERROR, "ar", "arg1", arg);
        if (!arg.value(0).isseq())
            return makeAbnormal(ARGUMENT_TYPE_ERROR, "ar", "arg1", arg);
        Value x = arg.value(1);
        Value[] y = arg.value(0).values();
        Value[] res = new Value[1+y.length];
        int i = 0;
        for (i = 0; i < y.length; i++) res[i] = y[i];
        res[i] = x;
        return makeSequence(res);
    };

    public static PrimitiveFunction cat = (arg, evaluator, environment) -> {
        if (!arg.isseq())
            return makeAbnormal(ARGUMENT_TYPE_ERROR, "cat", "arg1", arg);
        Value[] seq = arg.values();
        int n = 0;
        for (int i = 0; i < seq.length; i++) {
            if (!seq[i].isseq())
                return makeAbnormal(ARGUMENT_TYPE_ERROR, "cat", "arg1", arg);
            n += seq[i].values().length;
        }
        Value[] res = new Value[n];
        int k = 0;
        for (int i = 0; i < seq.length; i++) {
            for (int j = 0; j < seq[i].values().length; j++) {
                res[k++] = seq[i].value(j);
            }
        }
        return makeSequence(res);
    };

    public static PrimitiveFunction distl = (arg, evaluator, environment) -> {
        if (!arg.isseq())
            return makeAbnormal(ARGUMENT_TYPE_ERROR, "distl", "arg1", arg);
        if (arg.values().length != 2)
            return makeAbnormal(ARGUMENT_COUNT_ERROR, "distl", "arg1", arg);
        if (!arg.value(1).isseq())
            return makeAbnormal(ARGUMENT_TYPE_ERROR, "distl", "arg1", arg);
        Value x = arg.value(0);
        Value[] y = arg.value(1).values();
        Value[] res = new Value[y.length];
        for (int i = 0; i < y.length; i++) res[i] = makePair(x, y[i]);
        return makeSequence(res);
    };
    
    public static PrimitiveFunction distr = (arg, evaluator, environment) -> {
        if (!arg.isseq())
            return makeAbnormal(ARGUMENT_TYPE_ERROR, "distr", "arg1", arg);
        if (arg.values().length != 2)
            return makeAbnormal(ARGUMENT_COUNT_ERROR, "distr", "arg1", arg);
        if (!arg.value(0).isseq())
            return makeAbnormal(ARGUMENT_TYPE_ERROR, "distr", "arg1", arg);
        Value[] y = arg.value(0).values();
        Value x = arg.value(1);
        Value[] res = new Value[y.length];
        for (int i = 0; i < y.length; i++) res[i] = makePair(y[i], x);
        return makeSequence(res);
    };

    public static PrimitiveFunction intsto = (arg, evaluator, environment) -> {
        if (!arg.istype(ValueType.INTEGER))
            return makeAbnormal(ARGUMENT_TYPE_ERROR, "insto", "arg1", arg);
        long n = arg.integer();
        if (n < 1) return makeAbnormal(ARGUMENT_TYPE_ERROR, "insto", "arg1", arg);
        Value[] values = new Value[(int)n];
        for (int i = 0; i < n; i++) values[i] = makeInteger(i+1);
        return makeSequence(values);
    };
    
    public static PrimitiveFunction len = (arg, evaluator, environment) -> {
        if (arg.isseq())
            return makeInteger(arg.values().length);
        else
            return makeAbnormal(ARGUMENT_TYPE_ERROR, "len", "arg1", arg);
    };

    public static PrimitiveFunction reverse = (arg, evaluator, environment) -> {
        if (!arg.isseq())
            return makeAbnormal(ARGUMENT_TYPE_ERROR, "reverse", "arg1", arg);
        Value[] seq = arg.values();
        if (seq.length <= 1) return arg;
        Value[] values = new Value[seq.length];
        int n = arg.values().length;
        for (int i = 0; i < n; i++) values[n-i-1] = seq[i];
        return makeSequence(values);
    };

    public static PrimitiveFunction sel = (arg, evaluator, environment) -> {
        if (!arg.isseq())
            return makeAbnormal(ARGUMENT_TYPE_ERROR, "sel", "arg1", arg);
        if (arg.values().length != 2)
            return makeAbnormal(ARGUMENT_COUNT_ERROR, "sel", "arg1", arg);
        if (!arg.value(0).istype(ValueType.INTEGER))
            return makeAbnormal(ARGUMENT_TYPE_ERROR, "sel", "arg1", arg);
        if (!arg.value(1).isseq())
            return makeAbnormal(ARGUMENT_TYPE_ERROR, "seq", "arg1", arg);
        long i = arg.value(0).integer();  
        Value[] x = arg.value(1).values();
        if (i > 0 && i <= x.length)
            return x[(int)i-1];
        else if (i < 0 && i >= -x.length)
            return x[x.length+(int)i];
        else
            return makeAbnormal(INDEX_ERROR, "sel", "arg1", arg);
    };

    public static PrimitiveFunction trans = (arg, evaluator, environment) -> {
        if (!arg.isseq())
            return makeAbnormal(ARGUMENT_TYPE_ERROR, "trans", "arg1", arg);
        Value[] x = arg.values();
        if (x.length == 0) return arg;
        int len = -1;
        for (int i = 0; i < x.length; i++) {
            Value[] v = x[i].values();
            if (v == null)
                return makeAbnormal(ARGUMENT_TYPE_ERROR, "trans", "arg1", arg);
            else if (len < 0) 
                len = v.length;
            else if (v.length != len)
                return makeAbnormal(ARGUMENT_TYPE_ERROR, "trans", "arg1", arg);            
        }
        Value[] y = new Value[len];
        for (int j = 0; j < len; j++) {
            Value[] v = new Value[x.length];
            for (int i = 0; i < x.length; i++) {
                v[i] = x[i].value(j);
            }
            y[j] = makeSequence(v);
        }
        return makeSequence(y);
    };

    public static PrimitiveFunction tl = (arg, evaluator, environment) -> {
        if (!arg.isseq())
            return makeAbnormal(ARGUMENT_TYPE_ERROR, "tl", "arg1", arg);
        Value[] values = arg.values();
        if (values.length < 1)
            return arg;
        else
            return makeSequence(Arrays.copyOfRange(values, 1, values.length));
    };
    
    public static Value tlValue = makePrimitiveFunction("tl", tl);

    public static PrimitiveFunction tlr = (arg, evaluator, environment) -> {
        if (!arg.isseq())
            return makeAbnormal(ARGUMENT_TYPE_ERROR, "tlr", "arg1", arg);
        Value[] values = arg.values();
        if (values.length < 1)
            return arg;
        else
            return makeSequence(Arrays.copyOfRange(values, 0, values.length-1));
    };

    public static Value tlrValue = makePrimitiveFunction("tlr", tlr);

    public static PrimitiveFunction makeSFunction(final int n) {
        return (arg, evaluator, environment) -> {
            if (!arg.isseq())
                return makeAbnormal(ARGUMENT_TYPE_ERROR, "s"+n, "arg1", arg);
            if (n <= arg.values().length)
                return arg.value(n-1);
            else
                return makeAbnormal(INDEX_ERROR, "s"+n, "arg1", arg);
        };  
    }

    public static PrimitiveFunction makeRFunction(final int n) {
        return (arg, evaluator, environment) -> {
            if (!arg.isseq())
                return makeAbnormal(ARGUMENT_TYPE_ERROR, "r"+n, "arg1", arg);
            int len = arg.values().length;
            if (n <= len)
                return arg.value(len-n);
            else
                return makeAbnormal(INDEX_ERROR, "r"+n, "arg1", arg);
        };  
    }
    
    public static Value s(int i) {
        return makePrimitiveFunction("s"+i, makeSFunction(i));
    }

    public static Value r(int i) {
        return makePrimitiveFunction("r"+i, makeRFunction(i));
    }
}
