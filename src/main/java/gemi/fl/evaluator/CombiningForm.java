package gemi.fl.evaluator;

@FunctionalInterface
public interface CombiningForm {
    
    Value apply(Value form, Value arg, NaiveEvaluator evaluator, Environment environment);    
}
