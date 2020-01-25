package gemi.fl.evaluator.builtins;

import static gemi.fl.evaluator.Value.*;

import gemi.fl.evaluator.PrimitiveFunction;
import gemi.fl.evaluator.Value;

/**
 * Primtive Boolean functions.
 */
public class BooleanFunctions {

    public static PrimitiveFunction and = (arg, evaluator, environment) -> {
        if (arg.isseq()) {
            Value[] seq = arg.values();
            for (int i = 0; i < seq.length; i++) {
                if (!seq[i].istrue()) {
                    return makeTruth(false);
                }
            }
            return makeTruth(true);
        }
        else {
            return makeTruth(false);
        }
    };

    public static PrimitiveFunction andRaised = (arg, evaluator, environment) -> {
        if (!arg.isseq()) return makeTruth(false);
        Value[] values = arg.values();
        if (values.length <= 1) return makeTruth(true);
        if (applicables(values))
            return CombiningForms.makeRaised(makePrimitiveFunction("and", and), makeSequence(arg.values()), evaluator, environment);
        else
            return and.apply(arg, evaluator, environment);
    };
    
    public static PrimitiveFunction or = (arg, evaluator, environment) -> {
        if (arg.isseq()) {
            Value[] seq = arg.values();
            if (seq.length == 0) 
                return makeTruth(false);
            for (int i = 0; i < seq.length; i++) {
                if (seq[i].istrue()) {
                    return makeTruth(true);
                }
            }
            return makeTruth(false);
        }
        else {
            return makeTruth(false);
        }
    };

    public static PrimitiveFunction orRaised = (arg, evaluator, environment) -> {
        if (!arg.isseq()) return makeTruth(false);
        Value[] values = arg.values();
        if (values.length <= 1) return makeTruth(true);
        if (applicables(values))
            return CombiningForms.makeRaised(makePrimitiveFunction("or", or), makeSequence(arg.values()), evaluator, environment);
        else
            return or.apply(arg, evaluator, environment);
    };

    public static PrimitiveFunction not = (arg, evaluator, environment) -> {
        return makeTruth(!arg.istrue());
    };

    public static Value notValue = makePrimitiveFunction("not", not); 

    public static PrimitiveFunction notRaised = (arg, evaluator, environment) -> {
        if (arg.isapplicable())
            return makeCombiningForm("compose", CombiningForms.compose, new Value[] { makePair(notValue, arg) });
        else
            return not.apply(arg, evaluator, environment);
    };
    
    private static boolean applicables(Value[] values) {
        for (Value value : values) {
            if (!value.isapplicable()) return false;
        }
        return true;
    }
}
