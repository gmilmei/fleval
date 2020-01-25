package gemi.fl.evaluator.builtins;

import static gemi.fl.evaluator.AbnormalType.ARGUMENT_COUNT_ERROR;
import static gemi.fl.evaluator.AbnormalType.ARGUMENT_TYPE_ERROR;

import org.junit.Test;

import gemi.fl.evaluator.FLTest;

public class ArithmeticFunctionsTest extends FLTest {

    @Test
    public void testAdd() {
        assertFL("add:`a", ARGUMENT_TYPE_ERROR);
        assertFL("add:<>", 0);
        assertFL("add:<2>", 2);
        assertFL("add:<2,3>", 2+3);
        assertFL("2 add 3", 2+3);
        assertFL("add:<2,3.0>", 2+3.0);
        assertFL("add:<2,3,-7>", 2+3-7);
        assertFL("add:<2,`a,-7>", ARGUMENT_TYPE_ERROR);
    }

    @Test
    public void testSub() {
        assertFL("sub:`a", ARGUMENT_TYPE_ERROR);
        assertFL("sub:<>", ARGUMENT_COUNT_ERROR);
        assertFL("sub:<2>", ARGUMENT_COUNT_ERROR);
        assertFL("sub:<2,3>", 2-3);
        assertFL("2 sub 3", 2-3);
        assertFL("sub:<2,3.0>", 2-3.0);
        assertFL("sub:<2,3,-7>", ARGUMENT_COUNT_ERROR);
        assertFL("sub:<2,`a,-7>", ARGUMENT_TYPE_ERROR);
    }

    @Test
    public void testMul() {
        assertFL("mul:`a", ARGUMENT_TYPE_ERROR);
        assertFL("mul:<>", 1);
        assertFL("mul:<2>", 2);
        assertFL("mul:<2,-3>", 2*-3);
        assertFL("2 mul -3", 2*-3);
        assertFL("mul:<2,-3.0>", 2*-3.0);
        assertFL("mul:<2,3,-7>", 2*3*-7);
        assertFL("mul:<2,`a,-7>", ARGUMENT_TYPE_ERROR);
    }

    @Test
    public void testDiv() {
        assertFL("div:`a", ARGUMENT_TYPE_ERROR);
        assertFL("div:<>", ARGUMENT_COUNT_ERROR);
        assertFL("div:<2>", ARGUMENT_COUNT_ERROR);
        assertFL("div:<2,-3>", -2/3.0);
        assertFL("2 div -3", -2/3.0);
        assertFL("div:<2,-3.0>", -2/3.0);
        assertFL("div:<2,3,-7>", ARGUMENT_COUNT_ERROR);
        assertFL("div:<2,`a,-7>", ARGUMENT_TYPE_ERROR);
    }

    @Test
    public void testNeg() {
        assertFL("neg:3", -3);
        assertFL("neg:5.3e-3", -.0053);
    }

    @Test
    public void testFloor() {
        assertFL("floor:3", 3);
        assertFL("floor:3.7", 3);
        assertFL("floor:-3.7", -4);
    }

    @Test
    public void testCeiling() {
        assertFL("ceiling:3", 3);
        assertFL("ceiling:3.7", 4);
        assertFL("ceiling:-3.7", -3);
    }

    @Test
    public void testAbs() {
        assertFL("abs:3", 3);
        assertFL("abs:-3", 3);
        assertFL("abs:-3.7", 3.7);
    }

    @Test
    public void testPlus() {
        assertFL("+:<1,2,3>", 1+2+3);
        assertFL("+:<>", 0);
        assertFL("2 + 3", 5);
        assertFL("+:<ceiling,floor,id>:8.5", 8+9+8.5);
        assertFL("(ceiling + floor):8.5", 8+9);
    }

    @Test
    public void testMinus() {
        assertFL("-:<1,2>", -1);
        assertFL("1 - 2", -1);
        assertFL("-:<ceiling,floor>:8.5", 9-8);
        assertFL("-:<ceiling,floor>:8.5", 9-8);
        assertFL("-:<ceiling,floor,id>:8.5", ARGUMENT_COUNT_ERROR);
        assertFL("-:<id>:8.5", ARGUMENT_COUNT_ERROR);
        assertFL("-:<>:8.5", ARGUMENT_COUNT_ERROR);
        assertFL("(ceiling - floor):8.5", 9-8);
    }

    @Test
    public void testTimes() {
        assertFL("*:<3,7.0>", 21.0);
        assertFL("3 * 7.0", 21.0);
        assertFL("*:<>", 1);
        assertFL("*:<ceiling,floor,id>:8.5", 8*9*8.5);
        assertFL("(ceiling * floor):8.5", 8*9);
    }

    @Test
    public void testDivide() {
        assertFL("/:<3,7>", 3/7.0);
        assertFL("3 / 7", 3/7.0);
        assertFL("/:<ceiling,floor>:8.5", 9.0/8.0);
        assertFL("(ceiling / floor):8.5", 9.0/8.0);
        assertFL("/:<ceiling>:8.5", ARGUMENT_COUNT_ERROR);
        assertFL("/:<>:8.5", ARGUMENT_COUNT_ERROR);
    }
}
