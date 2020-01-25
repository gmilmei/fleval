package gemi.fl.evaluator;

import gemi.fl.parser.Env;

public class Library {

    private String filename;
    private Env env;
    private Environment environment = null;
    
    public Library(String filename, Env env) {
        this.filename = filename;
        this.env = env;
    }
    
    public String filename() {
        return filename;
    }
    
    public Env env() {
        return env;
    }

    public Environment environment() {
        if (environment != null) return environment;
        NaiveEvaluator evaluator = new NaiveEvaluator(PrimitiveFunctions.environment);
        environment = evaluator.evaluateEnv(env, PrimitiveFunctions.environment);
        return environment;
    }
}
