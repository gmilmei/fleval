package gemi.fl.evaluator.builtins;

import org.junit.Test;

import gemi.fl.evaluator.FLTest;

public class PredicatesTest extends FLTest {

    @Test
    public void testIsint() {
        assertFL("isint:5", true);
        assertFL("isint:3.2", false);
        assertFL("isint:<5>", false);
    }

    @Test
    public void testIsreal() {
        assertFL("isreal:5", false);
        assertFL("isreal:3.2", true);
        assertFL("isreal:<3.2>", false);
    }

    @Test
    public void testIspos() {
        assertFL("ispos:5", true);
        assertFL("ispos:0", false);
        assertFL("ispos:-1", false);
        assertFL("ispos:5.0", true);
        assertFL("ispos:0.0", false);
        assertFL("ispos:-1.0", false);
        assertFL("ispos:<5>", false);
    }

    @Test
    public void testIsneg() {
        assertFL("isneg:5", false);
        assertFL("isneg:0", false);
        assertFL("isneg:-1", true);
        assertFL("isneg:5.0", false);
        assertFL("isneg:0.0", false);
        assertFL("isneg:-1.0", true);
        assertFL("isneg:< -5 >", false);
    }

    @Test
    public void testIszero() {
        assertFL("iszero:5", false);
        assertFL("iszero:0", true);
        assertFL("iszero:-1", false);
        assertFL("iszero:5.0", false);
        assertFL("iszero:0.0", true);
        assertFL("iszero:-1.0", false);
        assertFL("iszero:< -5 >", false);
    }

    @Test
    public void testIsbool() {
        assertFL("isbool:5", false);
        assertFL("isbool:true", true);
        assertFL("isbool:false", true);
        assertFL("isbool:`a", false);
        assertFL("isbool:isint", false);
        assertFL("isbool:<1,2.0>", false);
    }

    @Test
    public void testIschar() {
        assertFL("ischar:`a", true);
        assertFL("ischar:23", false);
        assertFL("ischar:\"a\"", false);
    }

    @Test
    public void testIsutype() {
        assertFL("isutype:(mkcomplex:<2,3>) where { type complex == [|isnum,isnum|] }", true);
        assertFL("isutype:<2,3>", false);
    }
    
    @Test
    public void testIsfunc() {
        assertFL("isfunc:`a", false);
        assertFL("isfunc:23", false);
        assertFL("isfunc:isint", true);
        assertFL("isfunc:f where { def f == id }", true);
        assertFL("isfunc:(C:add:4)", true);
        assertFL("isfunc:(lambda (x.) 2*x)", true);
    }

    @Test
    public void testIsobj() {
        assertFL("isobj:2", true);
        assertFL("isobj:<>", true);
        assertFL("isobj:\"isobj\"", true);
        assertFL("isobj:<2,3,<4>>", true);
        assertFL("isobj:<and,3,<4>>", false);
    }

    @Test
    public void testIsnull() {
        assertFL("isnull:<>", true);
        assertFL("isnull:\"\"", true);
        assertFL("isnull:\"foo\"", false);
        assertFL("isnull:<1,2,3>", false);
        assertFL("isnull:5", false);
    }

    @Test
    public void testIspair() {
        assertFL("ispair:<1,2>", true);
        assertFL("ispair:\"ab\"", true);
        assertFL("ispair:<>", false);
        assertFL("ispair:\"\"", false);
        assertFL("ispair:\"foo\"", false);
        assertFL("ispair:5", false);
    }

    @Test
    public void testIsseq() {
        assertFL("isseq:<1,2>", true);
        assertFL("isseq:\"ab\"", true);
        assertFL("isseq:<>", true);
        assertFL("isseq:\"\"", true);
        assertFL("isseq:5", false);
    }

    @Test
    public void testIsstring() {
        assertFL("isstring:<1,2>", false);
        assertFL("isstring:\"ab\"", true);
        assertFL("isstring:<>", true);
        assertFL("isstring:\"\"", true);
        assertFL("isstring:<`a,`b>", true);
        assertFL("isstring:5", false);
    }

    @Test
    public void testFf() {
        assertFL("ff:<1,2>", false);
        assertFL("ff:<>", false);
        assertFL("ff:true", false);
        assertFL("ff:isnull", false);
    }

    @Test
    public void testTt() {
        assertFL("tt:<1,2>", true);
        assertFL("tt:<>", true);
        assertFL("tt:true", true);
        assertFL("tt:false", true);
        assertFL("tt:isnull", true);
    }

    @Test
    public void testIsval() { 
        assertFL("isval:<1,2>", true);
        assertFL("isval:<>", true);
        assertFL("isval:true", true);
        assertFL("isval:false", true);
        assertFL("isval:isnull", true);
    }
}
