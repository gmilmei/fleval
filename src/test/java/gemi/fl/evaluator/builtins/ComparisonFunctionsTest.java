package gemi.fl.evaluator.builtins;

import org.junit.Test;

import gemi.fl.evaluator.AbnormalType;
import gemi.fl.evaluator.FLTest;

public class ComparisonFunctionsTest extends FLTest {

    @Test
    public void testEq() {
        assertFL("eq:<1,1,1>", true);
        assertFL("eq:<1,1,2>", false);
        assertFL("eq:<>", true);
        assertFL("eq:<id,id>", AbnormalType.ARGUMENT_TYPE_ERROR);        
        assertFL("eq:<id>", true);        
    }

    @Test
    public void testNeq() {
        assertFL("neq:<1,1,2>", true);
        assertFL("neq:<1,1,1>", false);
    }

    @Test
    public void testLess() {
        assertFL("less:<`a,1,2,<>>", true);
        assertFL("less:<1,1>", false);
        assertFL("less:<>", true);
        assertFL("less:<3>", true);
        assertFL("less:<id,id>", AbnormalType.ARGUMENT_TYPE_ERROR);        
        assertFL("less:<id>", true);        
        assertFL("less:<<>,<id>>", true);
    }

    @Test
    public void testGreater() {
        assertFL("greater:<3,2,1>", true);
        assertFL("greater:<2,2,1>", false);
        assertFL("greater:<>", true);
        assertFL("greater:<3>", true);
    }

    @Test
    public void testLesseq() {
        assertFL("lesseq:<1,1,2>", true);
        assertFL("lesseq:<1,1,1>", true);
        assertFL("lesseq:<1,2,1>", false);
    }

    @Test
    public void testGreatereq() {
        assertFL("greatereq:<2,1,1>", true);
        assertFL("greatereq:<2,2,1>", true);
        assertFL("greatereq:<1,2,1>", false);
    }
}
