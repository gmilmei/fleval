package gemi.fl.evaluator.builtins;

import static gemi.fl.evaluator.AbnormalType.SIGNAL;
import static gemi.fl.evaluator.Value.*;
import static gemi.fl.evaluator.Value.makeCombiningForm;

import gemi.fl.evaluator.CombiningForm;
import gemi.fl.evaluator.PrimitiveFunction;
import gemi.fl.evaluator.Value;

/**
 * Miscellaneous primitive functions.
 */
public class MiscFunctions {

    public static PrimitiveFunction id = (arg, evaluator, environment) -> {
        return arg;
    };

    public static Value idValue = makePrimitiveFunction("id", id);

    public static CombiningForm delta = (form, arg, evaluator, environment) -> {
        if (form.values().length == 0) {
            return makeCombiningForm(form.string(), form.combiningForm(), new Value[] { arg});
        }
        else {
            Value res = evaluator.apply(form.value(0), arg, environment);
            if (res.isabnormal())
                return res;
            else if (res.istrue())
                return arg;
            else
                return makeAbnormal(SIGNAL, "delta", "arg", arg);
        }
    };

    public static PrimitiveFunction signal = (arg, evaluator, environment) -> {
        return Value.makeSignal(arg);
    };
}
