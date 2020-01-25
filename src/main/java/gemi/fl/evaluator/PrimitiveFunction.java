package gemi.fl.evaluator;

@FunctionalInterface
public interface PrimitiveFunction {

    Value apply(Value arg, NaiveEvaluator evaluator, Environment environment);    
}
