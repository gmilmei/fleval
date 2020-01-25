package gemi.fl.evaluator;

import static org.junit.Assert.*;

import java.io.StringReader;

import gemi.fl.parser.Expr;
import gemi.fl.parser.Parser;
import gemi.fl.scanner.ErrorHandler;
import gemi.fl.scanner.Scanner;

public class FLTest {

    protected Value eval(String expression) {
        try {
            ErrorHandler errorHandler = new ErrorHandler("test", System.err);
            Scanner scanner = new Scanner(new StringReader(expression), errorHandler);
            Parser parser = new Parser(scanner, errorHandler);
            Expr expr = parser.parse();
            if (errorHandler.errorCount > 0) fail("syntax error");
            NaiveEvaluator evaluator = new NaiveEvaluator(PrimitiveFunctions.environment);
            return evaluator.evaluate(expr);
        } catch (Exception e) {
            return null;
        }
    }
    
    protected void assertFL(String expression, Value value) {
        assertEquals(eval(expression), value);
    }

    protected void assertFL(String expression, long integer) {
        assertFL(expression, Value.makeInteger(integer));
    }

    protected void assertFL(String expression, double real) {
        assertFL(expression, Value.makeReal(real));
    }

    protected void assertFL(String expression, boolean truth) {
        assertFL(expression, Value.makeTruth(truth));
    }

    protected void assertFL(String expression, AbnormalType abnormalType) {
        Value value = eval(expression);
        assertEquals(value.type(), ValueType.ABNORMAL);
        assertEquals(value.abnormalType(), abnormalType);
    }
}
