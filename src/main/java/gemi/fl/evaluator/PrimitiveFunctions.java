package gemi.fl.evaluator;

import static gemi.fl.evaluator.Value.*;

import gemi.fl.evaluator.builtins.*;
import gemi.fl.parser.EnvType;
import gemi.fl.scanner.Names;

public class PrimitiveFunctions {

    public static Environment environment = create();

    public static Environment create() {
        Environment env = new Environment(EnvType.PF, null);
        // predicates for numbers
        env.bind("isint", makePrimitiveFunction("isint", Predicates.isint));
        env.bind("isreal", makePrimitiveFunction("isreal", Predicates.isreal));
        env.bind("isnum", makePrimitiveFunction("isnum", Predicates.isnum));
        env.bind("ispos", makePrimitiveFunction("ispos", Predicates.ispos));
        env.bind("isneg", makePrimitiveFunction("isneg", Predicates.isneg));
        env.bind("iszero", makePrimitiveFunction("iszero", Predicates.iszero));
        // other predicates
        env.bind("isatom", makePrimitiveFunction("isatom", Predicates.isatom));
        env.bind("isbool", makePrimitiveFunction("isbool", Predicates.isbool));
        env.bind("ischar", makePrimitiveFunction("ischar", Predicates.ischar));
        env.bind("isutype", makePrimitiveFunction("isutype", Predicates.isutype));
        env.bind("isfunc", makePrimitiveFunction("isfunc", Predicates.isfunc));
        env.bind("isobj", makePrimitiveFunction("isobj", Predicates.isobj));
        // predicates for sequences
        env.bind("isnull", makePrimitiveFunction("isnull", Predicates.isnull));
        env.bind("ispair", makePrimitiveFunction("ispair", Predicates.ispair));
        env.bind("isseq", makePrimitiveFunction("isseq", Predicates.isseq));
        env.bind("isstring", makePrimitiveFunction("isstring", Predicates.isstring));
        // identically true and false predicates
        env.bind("ff", makePrimitiveFunction("ff", Predicates.ff));
        env.bind("isval", makePrimitiveFunction("isval", Predicates.isval));
        env.bind("tt", makePrimitiveFunction("tt", Predicates.tt));
        // arithmetic functions
        env.bind(Names.ADD, CombiningForms.makeRaised("add", ArithmeticFunctions.add));
        env.bind("add", makePrimitiveFunction("add", ArithmeticFunctions.add));
        env.bind(Names.SUBTRACT, CombiningForms.makeRaised("sub", ArithmeticFunctions.sub));
        env.bind("sub", makePrimitiveFunction("sub", ArithmeticFunctions.sub));
        env.bind(Names.MULTIPLY, CombiningForms.makeRaised("mul", ArithmeticFunctions.mul));
        env.bind("mul", makePrimitiveFunction("mul", ArithmeticFunctions.mul));
        env.bind(Names.DIVIDE, CombiningForms.makeRaised("div", ArithmeticFunctions.div));
        env.bind("div", makePrimitiveFunction("div", ArithmeticFunctions.div));
        env.bind("neg", makePrimitiveFunction("neg", ArithmeticFunctions.neg));
        env.bind("floor", makePrimitiveFunction("floor", ArithmeticFunctions.floor));
        env.bind("ceiling", makePrimitiveFunction("ceiling", ArithmeticFunctions.ceiling));
        env.bind("abs", makePrimitiveFunction("abs", ArithmeticFunctions.abs));
        // boolean functions
        env.bind(Names.AND, CombiningForms.makeRaised("and", BooleanFunctions.and));
        env.bind("and", makePrimitiveFunction("and", BooleanFunctions.and));
        env.bind(Names.OR, CombiningForms.makeRaised("or", BooleanFunctions.or));
        env.bind("or", makePrimitiveFunction("or", BooleanFunctions.or));
        env.bind("Not", makePrimitiveFunction("Not", BooleanFunctions.notRaised));
        env.bind("not", makePrimitiveFunction("not", BooleanFunctions.not));
        // comparison functions
        env.bind("eq", makePrimitiveFunction("eq", ComparisonFunctions.eq));
        env.bind("neq", makePrimitiveFunction("neq", ComparisonFunctions.neq));
        env.bind("less", makePrimitiveFunction("less", ComparisonFunctions.less));
        env.bind("greater", makePrimitiveFunction("greater", ComparisonFunctions.greater));
        env.bind("lesseq", makePrimitiveFunction("lesseq", ComparisonFunctions.lesseq));
        env.bind("greatereq", makePrimitiveFunction("greatereq", ComparisonFunctions.greatereq));
        env.bind(Names.EQUAL, CombiningForms.makeRaised("eq", ComparisonFunctions.eq));
        env.bind("lt", CombiningForms.makeRaised("less", ComparisonFunctions.less));
        env.bind("gt", CombiningForms.makeRaised("greater", ComparisonFunctions.greater));
        env.bind("le", CombiningForms.makeRaised("lesseq", ComparisonFunctions.lesseq));
        env.bind("ge", CombiningForms.makeRaised("greatereq", ComparisonFunctions.greatereq));
        // combining forms
        env.bind("@", makeCombiningForm("compose", CombiningForms.compose));
        env.bind("compose", makeCombiningForm("compose", CombiningForms.compose));
        env.bind("!", makeCombiningForm("!", CombiningForms.rcompose));
        env.bind("cons", makeCombiningForm("cons", CombiningForms.cons));
        env.bind("cond", makeCombiningForm("cond", CombiningForms.cond));
        env.bind("apply", makePrimitiveFunction("apply", CombiningForms.apply));
        env.bind("K", makeCombiningForm("K", CombiningForms.constant));
        env.bind("lift", makeCombiningForm("lift", CombiningForms.lift));
        env.bind("C", makeCombiningForm("curry", CombiningForms.curry));
        env.bind("raise", makeCombiningForm("raise", CombiningForms.raise));
        env.bind("catch", makeCombiningForm("catch", CombiningForms.Catch));
        env.bind("delay", makeCombiningForm("delay", CombiningForms.delay));
        // predicate combining forms
        env.bind("pcons", makeCombiningForm("pcons", PredicateCombiningForms.pcons));
        //env.bind("=", makeCombiningForm("=", PredicateCombiningForms.equal));
        env.bind("seqof", makeCombiningForm("seqof", PredicateCombiningForms.seqof));
        env.bind("eqto", makeCombiningForm("eqto", PredicateCombiningForms.eqto));
        env.bind("lenis", makeCombiningForm("lenis", PredicateCombiningForms.lenis));
        env.bind("|->", makeCombiningForm("|->", PredicateCombiningForms.pleft));
        env.bind("<-|", makeCombiningForm("<-|", PredicateCombiningForms.pright));
        // sequence combining forms
        env.bind("/l", makeCombiningForm("ileft", SequenceCombiningForms.ileft));
        env.bind("/r", makeCombiningForm("iright", SequenceCombiningForms.iright));
        env.bind("tree", makeCombiningForm("tree", SequenceCombiningForms.tree));
        env.bind("alpha", makeCombiningForm("alpha", SequenceCombiningForms.alpha));
        env.bind("α", makeCombiningForm("alpha", SequenceCombiningForms.alpha));
        env.bind("merge", makeCombiningForm("merge", SequenceCombiningForms.merge));
        // sequence functions
        env.bind("al", makePrimitiveFunction("al", SequenceFunctions.al));
        env.bind("ar", makePrimitiveFunction("ar", SequenceFunctions.ar));
        env.bind("cat", makePrimitiveFunction("cat", SequenceFunctions.cat));
        env.bind("distl", makePrimitiveFunction("distl", SequenceFunctions.distl));
        env.bind("distr", makePrimitiveFunction("distr", SequenceFunctions.distr));
        env.bind("intsto", makePrimitiveFunction("intsto", SequenceFunctions.intsto));
        env.bind("len", makePrimitiveFunction("len", SequenceFunctions.len));
        env.bind("reverse", makePrimitiveFunction("reverse", SequenceFunctions.reverse));
        env.bind("sel", makePrimitiveFunction("sel", SequenceFunctions.sel));
        env.bind("trans", makePrimitiveFunction("trans", SequenceFunctions.trans));
        env.bind("tl", makePrimitiveFunction("tl", SequenceFunctions.tl));
        env.bind("tlr", makePrimitiveFunction("tlr", SequenceFunctions.tlr));
        // input, output and file functions
        env.bind("in", makePrimitiveFunction("in", IOFunctions.in));
        env.bind("out", makePrimitiveFunction("out", IOFunctions.out));
        env.bind("get", makePrimitiveFunction("get", IOFunctions.get));
        env.bind("put", makePrimitiveFunction("put", IOFunctions.put));
        // miscellaneous functions
        env.bind("id", makePrimitiveFunction("id", MiscFunctions.id));
        env.bind("Delta", makeCombiningForm("Delta", MiscFunctions.delta));
        env.bind("Δ", makeCombiningForm("Delta", MiscFunctions.delta));
        env.bind("signal", makePrimitiveFunction("signal", MiscFunctions.signal));
        
        for (int i = 1; i <= 10; i++) {
            env.bind("s"+i, SequenceFunctions.s(i));
            env.bind("r"+i, SequenceFunctions.r(i));
        }
        
        return env;
    }
}
