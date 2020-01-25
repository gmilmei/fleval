package gemi.fl.evaluator;

@FunctionalInterface
public interface PatternCombiner {

    Pattern apply(Pattern[] patterns, NaiveEvaluator evaluator, Environment environment);    
}
