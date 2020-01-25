package gemi.fl.evaluator.builtins;

import static gemi.fl.evaluator.Value.makePrimitiveFunction;
import static gemi.fl.evaluator.Value.makeTruth;

import gemi.fl.evaluator.PrimitiveFunction;
import gemi.fl.evaluator.Value;
import gemi.fl.evaluator.ValueType;

/**
 * Primitive predicates.
 */
public final class Predicates {

    public static PrimitiveFunction isint = (arg, evaluator, environment) -> {
        return makeTruth(arg.istype(ValueType.INTEGER));
    };

    public static PrimitiveFunction isreal = (arg, evaluator, environment) -> {
        return makeTruth(arg.istype(ValueType.REAL));
    };

    public static PrimitiveFunction isnum = (arg, evaluator, environment) -> {
        return makeTruth(arg.istype(ValueType.REAL) || arg.istype(ValueType.INTEGER));
    };

    public static PrimitiveFunction ispos = (arg, evaluator, environment) -> {
        if (arg.istype(ValueType.INTEGER))
            return makeTruth(arg.integer() > 0);
        else if (arg.istype(ValueType.REAL))
            return makeTruth(arg.real() > 0);
        else
            return makeTruth(false);
    };

    public static PrimitiveFunction isneg = (arg, evaluator, environment) -> {
        if (arg.istype(ValueType.INTEGER))
            return makeTruth(arg.integer() < 0);
        else if (arg.istype(ValueType.REAL))
            return makeTruth(arg.real() < 0);
        else
            return makeTruth(false);
    };

    public static PrimitiveFunction iszero = (arg, evaluator, environment) -> {
        if (arg.istype(ValueType.INTEGER))
            return makeTruth(arg.integer() == 0);
        else if (arg.istype(ValueType.REAL))
            return makeTruth(arg.real() == 0);
        else
            return makeTruth(false);
    };

    public static PrimitiveFunction isatom = (arg, evaluator, environment) -> {
        return makeTruth(arg.istype(ValueType.INTEGER)
                || arg.istype(ValueType.REAL)
                || arg.istype(ValueType.CHARACTER)
                || arg.istype(ValueType.TRUTH));
    };

    public static PrimitiveFunction isbool = (arg, evaluator, environment) -> {
        return makeTruth(arg.istype(ValueType.TRUTH));
    };

    public static PrimitiveFunction ischar = (arg, evaluator, environment) -> {
        return makeTruth(arg.istype(ValueType.CHARACTER));
    };

    public static PrimitiveFunction isutype = (arg, evaluator, environment) -> {
        return makeTruth(arg.istype(ValueType.USER));
    };

    public static PrimitiveFunction isfunc = (arg, evaluator, environment) -> {
        return makeTruth(arg.isapplicable());
    };

    private static boolean _isobj(Value value) {
        if (value.istype(ValueType.INTEGER)
                || value.istype(ValueType.REAL)
                || value.istype(ValueType.CHARACTER)
                || value.istype(ValueType.TRUTH))
            return true;
        if (value.isseq()) {
            for (Value val : value.values()) {
                if (!_isobj(val)) return false;
            }
            return true;
        }
        return false;
    }
    
    public static PrimitiveFunction isobj = (arg, evaluator, environment) -> {
        return makeTruth(_isobj(arg));
    };

    public static PrimitiveFunction isnull = (arg, evaluator, environment) -> {
        if (arg.isseq())
            return makeTruth(arg.values().length == 0);
        else
            return makeTruth(false);
    };

    public static PrimitiveFunction ispair = (arg, evaluator, environment) -> {
        if (arg.isseq())
            return makeTruth(arg.values().length == 2);
        else
            return makeTruth(false);
    };

    public static PrimitiveFunction isseq = (arg, evaluator, environment) -> {
        return makeTruth(arg.isseq());
    };

    public static PrimitiveFunction isstring = (arg, evaluator, environment) -> {
        return makeTruth(arg.isstring());
    };

    public static PrimitiveFunction ff = (arg, evaluator, environment) -> {
        return makeTruth(false);
    };

    public static PrimitiveFunction isval = (arg, evaluator, environment) -> {
        return makeTruth(true);
    };

    public static PrimitiveFunction tt = (arg, evaluator, environment) -> {
        return makeTruth(true);
    };

    public static Value ttValue = makePrimitiveFunction("tt", tt);
}
