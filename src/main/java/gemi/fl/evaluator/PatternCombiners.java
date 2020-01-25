package gemi.fl.evaluator;

import static gemi.fl.evaluator.Value.makePair;
import static gemi.fl.evaluator.Value.makeSequence;
import static gemi.fl.evaluator.builtins.BooleanFunctions.andRaised;
import static gemi.fl.evaluator.builtins.BooleanFunctions.orRaised;
import static gemi.fl.evaluator.builtins.CombiningForms.makeCompose;
import static gemi.fl.evaluator.builtins.PredicateCombiningForms.makePcons;
import static gemi.fl.evaluator.builtins.PredicateCombiningForms.makePleft;
import static gemi.fl.evaluator.builtins.PredicateCombiningForms.makePright;
import static gemi.fl.evaluator.builtins.SequenceFunctions.r;
import static gemi.fl.evaluator.builtins.SequenceFunctions.s;
import static gemi.fl.evaluator.builtins.SequenceFunctions.tlValue;
import static gemi.fl.evaluator.builtins.SequenceFunctions.tlrValue;

import java.util.Map.Entry;

public final class PatternCombiners {

    public static PatternCombiner pand = (Pattern[] patterns, NaiveEvaluator evaluator, Environment environment) -> {
        if (patterns == null || patterns.length != 2)
            return null;
        Pattern p1 = patterns[0];
        if (p1 == null) return null;
        Pattern p2 = patterns[1];
        if (p2 == null) return null;
        Pattern pattern = new Pattern();
        pattern.bindAll(p1.functions());
        pattern.bindAll(p2.functions());
        pattern.predicate(andRaised.apply(makePair(p1.predicate(), p2.predicate()), evaluator, environment));
        return pattern;
    };

    public static PatternCombiner por = (Pattern[] patterns, NaiveEvaluator evaluator, Environment environment) -> {
        if (patterns == null || patterns.length != 2)
            return null;
        Pattern p1 = patterns[0];
        if (p1 == null) return null;
        Pattern p2 = patterns[1];
        if (p2 == null) return null;
        Pattern pattern = new Pattern();
        pattern.bindAll(p1.functions());
        pattern.bindAll(p2.functions());
        pattern.predicate(orRaised.apply(makePair(p1.predicate(), p2.predicate()), evaluator, environment));
        return pattern;
    };

    public static PatternCombiner pcomp = (Pattern[] patterns, NaiveEvaluator evaluator, Environment environment) -> {
        if (patterns == null || patterns.length != 2)
            return null;
        Pattern p1 = patterns[0];
        if (p1 == null) return null;
        Pattern p2 = patterns[1];
        if (p2 == null) return null;
        Pattern pattern = new Pattern();
        for (Entry<String,Value> entry : p1.functionSet()) {
            String name = entry.getKey();
            Value f = entry.getValue();
            pattern.bind(name, makeCompose(f, p2.predicate(), evaluator, environment));
        }
        pattern.bindAll(p2.functions());
        pattern.predicate(makeCompose(p1.predicate(), p2.predicate(), evaluator, environment));
        return pattern;
    };

    
    public static PatternCombiner patcons = (Pattern[] patterns, NaiveEvaluator evaluator, Environment environment) -> {
        Pattern pattern = new Pattern();
        Value[] predicates = new Value[patterns.length];
        for (int i = 0; i < patterns.length; i++) {
            predicates[i] = patterns[i].predicate();
            for (Entry<String,Value> entry : patterns[i].functionSet()) {
                String name = entry.getKey();
                Value fun = entry.getValue();
                pattern.bind(name, makeCompose(fun, s(i+1), evaluator, environment));
            }
        }
        pattern.predicate(makePcons(makeSequence(predicates), evaluator, environment));
        return pattern;
    };
    
    public static PatternCombiner patAppendLeft = (Pattern[] patterns, NaiveEvaluator evaluator, Environment environment) -> {
        if (patterns == null || patterns.length != 2)
            return null;
        Pattern pattern = new Pattern();
        for (Entry<String,Value> entry : patterns[0].functionSet()) {
            String name = entry.getKey();
            Value fun = entry.getValue();
            pattern.bind(name, makeCompose(fun, s(1), evaluator, environment));
        }
        for (Entry<String,Value> entry : patterns[1].functionSet()) {
            String name = entry.getKey();
            Value fun = entry.getValue();
            pattern.bind(name, makeCompose(fun, tlValue, evaluator, environment));
        }
        pattern.predicate(makePleft(makePair(patterns[0].predicate(), patterns[1].predicate()), evaluator, environment));
        return pattern;
    };

    public static PatternCombiner patAppendRight = (Pattern[] patterns, NaiveEvaluator evaluator, Environment environment) -> {
        if (patterns == null || patterns.length != 2)
            return null;
        Pattern pattern = new Pattern();
        for (Entry<String,Value> entry : patterns[0].functionSet()) {
            String name = entry.getKey();
            Value fun = entry.getValue();
            pattern.bind(name, makeCompose(fun, tlrValue, evaluator, environment));
        }
        for (Entry<String,Value> entry : patterns[1].functionSet()) {
            String name = entry.getKey();
            Value fun = entry.getValue();
            pattern.bind(name, makeCompose(fun, r(1), evaluator, environment));
        }
        pattern.predicate(makePright(makePair(patterns[0].predicate(), patterns[1].predicate()), evaluator, environment));
        return pattern;
    };
}
