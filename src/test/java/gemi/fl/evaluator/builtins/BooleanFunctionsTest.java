package gemi.fl.evaluator.builtins;

import org.junit.Test;

import gemi.fl.evaluator.FLTest;

public class BooleanFunctionsTest extends FLTest {

    @Test
    public void testAnd() {
        assertFL("and:<true,true,true>", true);
        assertFL("and:<1,2,3,4>", true);
        assertFL("and:<1,2,false>", false);
        assertFL("and:<>", true);
        assertFL("and:isint", false);
        assertFL("/\\:<true,true,true>", true);
        assertFL("/\\:<true,false,true>", false);
    }

    @Test
    public void testNot() {
        assertFL("not:false", true);
        assertFL("not:true", false);
        assertFL("not:<>", false);
        assertFL("not:isint", false);
    }

    @Test
    public void testOr() {
        assertFL("or:<false,false,1>", true);
        assertFL("or:<>", false);
        assertFL("or:isint", false);
        assertFL("\\/:<true,true,true>", true);
        assertFL("\\/:<true,false,true>", true);
        assertFL("\\/:<false,false,false>", false);
    }
}
