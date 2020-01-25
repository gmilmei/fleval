package gemi.fl.evaluator.builtins;

import static gemi.fl.evaluator.Value.*;

import gemi.fl.evaluator.*;

public class ComparisonFunctions {

    public static PrimitiveFunction eq = (arg, evaluator, environment) -> {
        if (!arg.isseq()) return makeTruth(false);
        Value[] values = arg.values();
        if (values.length <= 1) return makeTruth(true);
        Value val = values[0];
        for (int i = 1; i < values.length; i++) {
            Compare compare = val.compare(values[i]);
            if (compare == Compare.EQUAL) {
                val = values[i];
                continue;
            }
            
            if (compare == Compare.UNCOMPARABLE)
                return makeAbnormal(AbnormalType.ARGUMENT_TYPE_ERROR, "eq", "uncomparable", arg);
            else
                return makeTruth(false);
        }
        return makeTruth(true);
    };

    public static PrimitiveFunction neq = (arg, evaluator, environment) -> {
        Value res = eq.apply(arg, evaluator, environment);
        if (res.istype(ValueType.TRUTH))
            return makeTruth(!res.truth());
        else
            return res;
    };

    public static PrimitiveFunction less = (arg, evaluator, environment) -> {
        if (!arg.isseq()) return makeTruth(false);
        Value[] seq = arg.values();
        if (seq.length <= 1) return makeTruth(true);
        Value val = seq[0];
        for (int i = 1; i < seq.length; i++) {
            Compare compare = val.compare(seq[i]);
            if (compare == Compare.LESS) {
                val = seq[i];
                continue;
            }
            if (compare == Compare.UNCOMPARABLE)
                return makeAbnormal(AbnormalType.ARGUMENT_TYPE_ERROR, "less", "uncomparable", arg);
            else
                return makeTruth(false);
        }
        return makeTruth(true);
    };

    public static PrimitiveFunction greater = (arg, evaluator, environment) -> {
        if (!arg.isseq()) return makeTruth(false);
        Value[] seq = arg.values();
        if (seq.length <= 1) return makeTruth(true);
        Value val = seq[0];
        for (int i = 1; i < seq.length; i++) {
            Compare compare = val.compare(seq[i]);
            if (compare == Compare.GREATER) {
                val = seq[i];
                continue;
            }
            if (compare == Compare.UNCOMPARABLE)
                return makeAbnormal(AbnormalType.ARGUMENT_TYPE_ERROR, "greater", "uncomparable", arg);
            else
                return makeTruth(false);
        }
        return makeTruth(true);
    };

    public static PrimitiveFunction lesseq = (arg, evaluator, environment) -> {
        if (!arg.isseq()) return makeTruth(false);
        Value[] seq = arg.values();
        if (seq.length <= 1) return makeTruth(true);
        Value val = seq[0];
        for (int i = 1; i < seq.length; i++) {
            Compare compare = val.compare(seq[i]);
            if (compare == Compare.LESS || compare == Compare.EQUAL) {
                val = seq[i];
                continue;
            }
            if (compare == Compare.UNCOMPARABLE)
                return makeAbnormal(AbnormalType.ARGUMENT_TYPE_ERROR, "lesseq", "uncomparable", arg);
            else
                return makeTruth(false);
        }
        return makeTruth(true);
    };

    public static PrimitiveFunction greatereq = (arg, evaluator, environment) -> {
        if (!arg.isseq()) return makeTruth(false);
        Value[] seq = arg.values();
        if (seq.length <= 1) return makeTruth(true);
        Value val = seq[0];
        for (int i = 1; i < seq.length; i++) {
            Compare compare = val.compare(seq[i]);
            if (compare == Compare.GREATER || compare == Compare.EQUAL) {
                val = seq[i];
                continue;
            }
            if (compare == Compare.UNCOMPARABLE)
                return makeAbnormal(AbnormalType.ARGUMENT_TYPE_ERROR, "greatereq", "uncomparable", arg);
            else
                return makeTruth(false);
        }
        return makeTruth(true);
    };
}
