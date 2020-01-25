package gemi.fl.evaluator.builtins;

import static gemi.fl.evaluator.AbnormalType.ARGUMENT_COUNT_ERROR;
import static gemi.fl.evaluator.AbnormalType.ARGUMENT_TYPE_ERROR;
import static gemi.fl.evaluator.AbnormalType.DIVISION_BY_ZERO;
import static gemi.fl.evaluator.Value.*;

import gemi.fl.evaluator.PrimitiveFunction;
import gemi.fl.evaluator.Value;
import gemi.fl.evaluator.ValueType;

/**
 * Primitive arithmetic functions.
 */
public final class ArithmeticFunctions {

    private static enum ArgType {
        REAL,
        INTEGER,
        APPLICABLE,
        EMPTY,
        WRONG_TYPE
    }

    public static PrimitiveFunction add = (arg, evaluator, environment) -> {
        if (arg.isseq())
            return _add(argType(arg), arg.values());
        else
            return makeAbnormal(ARGUMENT_TYPE_ERROR, "add", "arg", arg);
    };
    
    private static Value _add(ArgType argType, Value[] values) {
        switch (argType) {
        case INTEGER: {
            int sum = 0;
            for (int i = 0; i < values.length; i++) {
                sum += values[i].integer();
            }
            return makeInteger(sum);
        }
        case REAL: {
            double sum = 0;
            for (int i = 0; i < values.length; i++) {
                sum += values[i].real();
            }
            return makeReal(sum);
        }
        case EMPTY: {
            return makeInteger(0);
        }
        default:
            return makeAbnormal(ARGUMENT_TYPE_ERROR, "add", "arg", makeSequence(values));
        }
    }

    public static PrimitiveFunction sub = (arg, evaluator, environment) -> {
        if (arg.isseq())
            return _sub(argType(arg), arg.values());
        else
            return makeAbnormal(ARGUMENT_TYPE_ERROR, "sub", "arg", arg);
    };

    private static Value _sub(ArgType argType, Value[] values) {
        switch (argType) {
        case INTEGER: {
            if (values.length != 2) return makeAbnormal(ARGUMENT_COUNT_ERROR, "sub", "arg1", makeSequence(values));
            long diff = values[0].integer();
            for (int i = 1; i < values.length; i++) {
                diff -= values[i].integer();
            }
            return makeInteger(diff);
        }
        case REAL: {
            if (values.length != 2) return makeAbnormal(ARGUMENT_COUNT_ERROR, "sub", "arg1", makeSequence(values));
            double diff = values[0].real();
            for (int i = 1; i < values.length; i++) {
                diff -= values[i].real();
            }
            return makeReal(diff);
        }
        case EMPTY:
            return makeAbnormal(ARGUMENT_COUNT_ERROR, "sub", "arg1", makeSequence(values));
        default:
            return makeAbnormal(ARGUMENT_TYPE_ERROR, "sub", "arg1", makeSequence(values));
        }
    };
    
    public static PrimitiveFunction mul = (arg, evaluator, environment) -> {
        if (arg.isseq())
            return _mul(argType(arg), arg.values());
        else
            return makeAbnormal(ARGUMENT_TYPE_ERROR, "mul", "arg1", arg);
    };
    
    private static Value _mul(ArgType argType, Value[] values) {
        switch (argType) {
        case INTEGER: {
            long prod = 1;
            for (int i = 0; i < values.length; i++) {
                prod *= values[i].integer();
            }
            return makeInteger(prod);
        }
        case REAL: {
            double prod = 1.0;
            for (int i = 0; i < values.length; i++) {
                prod *= values[i].real();
            }
            return makeReal(prod);
        }
        case EMPTY:
            return makeInteger(1);
        default:
            return makeAbnormal(ARGUMENT_TYPE_ERROR, "mul", "arg1", makeSequence(values));
        }
    }

    public static PrimitiveFunction div = (arg, evaluator, environment) -> {
        if (arg.isseq())
            return _div(argType(arg), arg.values());
        else
            return makeAbnormal(ARGUMENT_TYPE_ERROR, "div", "arg1", arg);
    };

    private static Value _div(ArgType argType, Value[] values) {
        switch (argType) {
        case INTEGER:
        case REAL: {
            if (values.length != 2) return makeAbnormal(ARGUMENT_COUNT_ERROR, "div", "arg1", makeSequence(values));
            double quot = values[0].real();
            for (int i = 1; i < values.length; i++) {
                if (values[i].real() == 0) return makeAbnormal(DIVISION_BY_ZERO, "div", "arg1", makeSequence(values));
                quot /= values[i].real();
            }
            return makeReal(quot);
        }
        case EMPTY:
            return makeAbnormal(ARGUMENT_COUNT_ERROR, "div", "arg1", makeSequence(values));
        default:
            return makeAbnormal(ARGUMENT_TYPE_ERROR, "div", "arg1", makeSequence(values));
        }
    };

    public static PrimitiveFunction neg = (arg, evaluator, environment) -> {
        if (arg.istype(ValueType.INTEGER))
            return makeInteger(-arg.integer());
        else if (arg.istype(ValueType.REAL))
            return makeReal(-arg.real());
        else
            return makeAbnormal(ARGUMENT_TYPE_ERROR, "neg", "arg1", arg);
    };

    public static PrimitiveFunction floor = (arg, evaluator, environment) -> {
        if (arg.istype(ValueType.INTEGER))
            return arg;
        else if (arg.istype (ValueType.REAL))
            return makeInteger((long)Math.floor(arg.real()));
        else
            return makeAbnormal(ARGUMENT_TYPE_ERROR, "floor", "arg1", arg);
    };

    public static PrimitiveFunction ceiling = (arg, evaluator, environment) -> {
        if (arg.istype(ValueType.INTEGER))
            return arg;
        else if (arg.istype(ValueType.REAL))
            return makeInteger((long)Math.ceil(arg.real()));
        else
            return makeAbnormal(ARGUMENT_TYPE_ERROR, "ceiling", "arg1", arg);
    };

    public static PrimitiveFunction abs = (arg, evaluator, environment) -> {
        if (arg.istype(ValueType.INTEGER))
            return arg.integer() > 0?arg:makeInteger(-arg.integer());
        else if (arg.istype(ValueType.REAL))
            return arg.real() > 0?arg:makeReal(-arg.real());
        else
            return makeAbnormal(ARGUMENT_TYPE_ERROR, "abs", "arg1", arg);
    };

    private static ArgType argType(Value arg) {
        if (!arg.isseq())
            return ArgType.WRONG_TYPE;
        Value[] seq = arg.values();
        if (seq.length == 0)
            return ArgType.EMPTY;
        ArgType argType = null;
        for (int i = 0; i < seq.length; i++) {
           if (seq[i].istype(ValueType.INTEGER)) {
               if (argType == null)
                   argType = ArgType.INTEGER;
               else if (argType != ArgType.REAL && argType != ArgType.INTEGER)
                   return ArgType.WRONG_TYPE;
           }
           else if (seq[i].istype(ValueType.REAL)) {
               if (argType == null)
                   argType = ArgType.REAL;
               else if (argType != ArgType.INTEGER && argType != ArgType.REAL)
                   return ArgType.WRONG_TYPE;
               argType = ArgType.REAL;

           }
           else if (seq[i].isapplicable()) {
               if (argType == null)
                   argType = ArgType.APPLICABLE;
               else if (argType != ArgType.APPLICABLE)
                   return ArgType.WRONG_TYPE;
           }
           else {
               return ArgType.WRONG_TYPE;
           }
        }
        return argType == null?ArgType.WRONG_TYPE:argType;
    }
}
