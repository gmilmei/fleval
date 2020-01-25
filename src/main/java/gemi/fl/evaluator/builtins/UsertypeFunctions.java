package gemi.fl.evaluator.builtins;

import static gemi.fl.evaluator.AbnormalType.ARGUMENT_TYPE_ERROR;
import static gemi.fl.evaluator.Value.makeAbnormal;
import static gemi.fl.evaluator.Value.makePrimitiveFunction;
import static gemi.fl.evaluator.Value.makeUser;

import gemi.fl.evaluator.PrimitiveFunction;
import gemi.fl.evaluator.Value;

import static gemi.fl.evaluator.Value.*;

/**
 * Primitive functions for user types.
 */
public final class UsertypeFunctions {
    
    public static Value makeUsertypeConstructor(String typename, String consname, long tag) {
        PrimitiveFunction fun = (arg, evaluator, environment) -> {
            return makeUser(typename, tag, arg);
        };
        return makePrimitiveFunction(consname, fun);
    }

    public static Value makeUsertypeDeconstructor(String typename, String deconsname, long tag) {
        PrimitiveFunction fun = (arg, evaluator, environment) -> {
            if (!arg.isuser(typename, tag))
                return makeAbnormal(ARGUMENT_TYPE_ERROR, deconsname, "arg1", arg);
            return arg.value();
        };
        return makePrimitiveFunction(deconsname, fun);
    }

    public static Value makeUsertypeTest(String typename, String testname, long tag) {
        PrimitiveFunction fun = (arg, evaluator, environment) -> {
            return makeTruth(arg.isuser(typename, tag));
        };
        return makePrimitiveFunction(testname, fun);
    }

    public static Value makeUsertypeSelector(String typename, String selectorname, long tag, Value selector) {
        PrimitiveFunction fun = (arg, evaluator, environment) -> {
            if (!arg.isuser(typename, tag))
                return makeAbnormal(ARGUMENT_TYPE_ERROR, selectorname, "arg1", arg);
            return evaluator.apply(selector, arg.value(), environment);
        };
        return makePrimitiveFunction(selectorname, fun);
    }
}
