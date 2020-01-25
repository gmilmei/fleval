package gemi.fl.evaluator.builtins;

import static gemi.fl.evaluator.AbnormalType.ARGUMENT_COUNT_ERROR;
import static gemi.fl.evaluator.AbnormalType.ARGUMENT_TYPE_ERROR;
import static gemi.fl.evaluator.AbnormalType.SIGNAL;
import static gemi.fl.evaluator.Value.makeAbnormal;
import static gemi.fl.evaluator.Value.makeCombiningForm;
import static gemi.fl.evaluator.Value.*;
import static gemi.fl.evaluator.Value.makeSequence;

import gemi.fl.evaluator.*;

public class CombiningForms {

    public static CombiningForm compose = (form, arg, evaluator, environment) -> {
        Value[] values = form.values();
        if (values.length == 0) {
            if (!arg.isseq()) makeAbnormal(ARGUMENT_TYPE_ERROR, "compose", "arg1", arg);
            return makeCombiningForm(form.string(), form.combiningForm(), new Value[] { arg });
        }
        Value[] funs = form.value(0).values();
        Value res = arg;        
        for (int i = funs.length-1; i >= 0; i--) {
            res = evaluator.apply(funs[i], res, environment);
            if (res.isabnormal()) return res;
        }
        return res;
    };
    
    public static Value composeValue = makeCombiningForm("compose", compose);

    public static Value makeCompose(Value value, NaiveEvaluator evaluator, Environment environment) {
        return compose.apply(composeValue, value, evaluator, environment);
    }
    
    public static Value makeCompose(Value value1, Value value2, NaiveEvaluator evaluator, Environment environment) {
        return compose.apply(composeValue, makePair(value1, value2), evaluator, environment);
    }

    public static CombiningForm rcompose = (form, arg, evaluator, environment) -> {
        Value[] values = form.values();
        if (values.length == 0) {
            if (!arg.isseq()) makeAbnormal(ARGUMENT_TYPE_ERROR, "rcompose", "arg1", arg);
            return makeCombiningForm(form.string(), form.combiningForm(), new Value[] { arg });
        }
        Value[] funs = form.value(0).values();
        Value res = arg;        
        for (int i = 0; i < funs.length; i++) {
            res = evaluator.apply(funs[i], res, environment);
            if (res.isabnormal()) return res;
        }
        return res;
    };

    public static CombiningForm cons = (form, arg, evaluator, environment) -> {    
        Value[] values = form.values();
        if (values.length == 0) {
            if (!arg.isseq()) makeAbnormal(ARGUMENT_TYPE_ERROR, "cons", "arg1", arg);
            return makeCombiningForm(form.string(), form.combiningForm(), new Value[] { arg });
        }
        Value[] funs = form.value(0).values();
        Value[] res = new Value[funs.length];
        for (int i = 0; i < res.length; i++) {
            res[i] = evaluator.apply(funs[i], arg, environment);
            if (res[i].isabnormal()) return res[i];
        }
        return makeSequence(res);
    };
    
    public static Value consValue = makeCombiningForm("cons", cons);

    public static Value makeCons(Value value, NaiveEvaluator evaluator, Environment environment) {
            return cons.apply(consValue, value, evaluator, environment);
    }

    public static CombiningForm cond = (form, arg, evaluator, environment) -> {
        Value[] values = form.values();
        if (values.length == 0) {
            if (!arg.isseq())
                return makeAbnormal(ARGUMENT_TYPE_ERROR, "cond", "arg1", arg);
            else if (arg.values().length == 3 || arg.values().length == 2)
                return makeCombiningForm(form.string(), form.combiningForm(), new Value[] { arg });
            else
                return makeAbnormal(ARGUMENT_COUNT_ERROR, "cond", "arg1", arg);
        }
        Value condition = evaluator.apply(values[0].value(0), arg, environment);
        if (condition.isabnormal())
            return condition;
        else if (condition.istrue())
            return evaluator.apply(values[0].value(1), arg, environment);
        else if (values[0].values().length == 3)
            return evaluator.apply(values[0].value(2), arg, environment);
        else
            return makeAbnormal(SIGNAL, "cond", "iarm", arg);
    };

    public static Value condValue = makeCombiningForm("cond", cond);

    public static Value makeCond(Value value, NaiveEvaluator evaluator, Environment environment) {
            return cons.apply(condValue, value, evaluator, environment);
    }

    public static PrimitiveFunction apply = (arg, evaluator, environment) -> {
        if (arg.isseq() && arg.values().length == 2)
            return evaluator.apply(arg.value(0), arg.value(1), environment);
        else
            return makeAbnormal(ARGUMENT_TYPE_ERROR, "apply", "arg1", arg);
    };

    public static Value applyValue = makePrimitiveFunction("apply", apply);

    public static CombiningForm constant = (form, arg, evaluator, environment) -> {
        Value[] values = form.values();
        if (values.length == 0) {
            return makeCombiningForm(form.string(), form.combiningForm(), new Value[] { arg });
        }
        return form.value(0);
    };
    
    public static Value constantValue = makeCombiningForm("K", constant);

    public static Value makeConstant(Value value, NaiveEvaluator evaluator, Environment environment) {
        return constant.apply(constantValue, value, evaluator, environment);
    }

    public static CombiningForm lift = (form, arg, evaluator, environment) -> {    
        Value[] values = form.values();
        if (values.length == 0)
            return makeCombiningForm(form.string(), form.combiningForm(), new Value[] { arg });
        if (values.length == 1)
            return makeCombiningForm(form.string(), form.combiningForm(), new Value[] { values[0], arg });

        Value f = form.value(0);
        Value c = form.value(1);
        if (!c.isseq()) return makeAbnormal(ARGUMENT_TYPE_ERROR, "lift", "arg2", arg);
        values = new Value[c.values().length];
        for (int i = 0; i < values.length; i++) {
            values[i] = evaluator.apply(c.value(i), arg, environment);
            if (values[i].isabnormal()) return values[i];
        }
        return evaluator.apply(f, makeSequence(values), environment);
    };

    public static Value liftValue = makeCombiningForm("lift", lift);

    public static Value makeLifted(Value value, NaiveEvaluator evaluator, Environment environment) {
        return lift.apply(liftValue, value, evaluator, environment);
    }

    public static CombiningForm curry = (form, arg, evaluator, environment) -> {    
        Value[] values = form.values();
        if (values.length == 0) {
            return makeCombiningForm(form.string(), form.combiningForm(), new Value[] { arg });
        }
        else if (values.length == 1) {
            return makeCombiningForm(form.string(), form.combiningForm(), new Value[] { values[0], arg });
        }
        else {
            Value fun = form.value(0);
            Value x = form.value(1);
            return evaluator.apply(fun, makePair(x, arg), environment);
        }
    };

    public static CombiningForm raise = (form, arg, evaluator, environment) -> {
        Value[] values = form.values();
        if (values.length == 0) {
            return makeCombiningForm(form.string(), form.combiningForm(), new Value[] { arg });
        }
        if (values.length == 1) {
            Value applicable = form.value(0);
            if (!arg.isseq() || arg.values().length == 0) 
                return evaluator.apply(applicable, arg, environment);
            Value[] funs = arg.values();
            for (int i = 0; i < funs.length; i++) {
                if (!funs[i].isapplicable()) {
                    return evaluator.apply(applicable, arg, environment);
                }
            }
            return makeCombiningForm(form.string(), form.combiningForm(), new Value[] { form.value(0), arg });
        }
        
        Value applicable = form.value(0);
        Value[] funs = form.value(1).values();
        values = new Value[funs.length];
        for (int i = 0; i < funs.length; i++) {
            values[i] = evaluator.apply(funs[i], arg, environment);
            if (values[i].isabnormal()) return values[i];
        }

        return evaluator.apply(applicable, makeSequence(values), environment);
    };

    public static Value raiseValue = makeCombiningForm("raise", raise);

    public static Value makeRaised(Value value, Value arg, NaiveEvaluator evaluator, Environment environment) {
        Value raised = raise.apply(raiseValue, value, evaluator, environment);
        return raise.apply(raised, arg, evaluator, environment);
    }

    /**
     * Creates a raised version of the primitive function with the given name.
     */
    public static Value makeRaised(String name, PrimitiveFunction fun) {
        Value f = makePrimitiveFunction(name, fun);
        return makeCombiningForm("raise", raise, new Value[] { f } );
    }
    
    public static CombiningForm Catch = (form, arg, evaluator, environment) -> {
        Value[] values = form.values();
        if (values.length == 0) {
            if (!arg.isseq() || arg.values().length != 2)
                return makeAbnormal(ARGUMENT_TYPE_ERROR, "catch", "arg1", arg);
            return makeCombiningForm(form.string(), form.combiningForm(), new Value[] { arg });
        }
        Value f = form.value(0).value(0);
        Value g = form.value(0).value(1);
        Value res = evaluator.apply(f, arg, environment);
        if (res.isabnormal())
            return  evaluator.apply(g, makePair(arg, res.value()), environment);
        else
            return res;
    };

    public static CombiningForm delay = (form, arg, evaluator, environment) -> {
        Value[] values = form.values();
        if (values.length == 0) {
            return makeCombiningForm(form.string(), form.combiningForm(), new Value[] { arg });
        }
        else if (values.length == 1) {
            return makeCombiningForm(form.string(), form.combiningForm(), new Value[] { form.value(0), arg });
        }
        Value f = form.value(0);
        Value x = form.value(1);
        Value y = arg;
        Value res =  evaluator.apply(f, x, environment);
        if (res.isabnormal()) return res;
        return evaluator.apply(res, y, environment);
    };
}
