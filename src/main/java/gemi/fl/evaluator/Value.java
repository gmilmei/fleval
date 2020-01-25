package gemi.fl.evaluator;

import static gemi.fl.evaluator.ValueType.*;

import gemi.fl.parser.Expr;
import gemi.fl.scanner.Char;

/**
 * A value is the result of an evalution.
 */
public final class Value {

    private ValueType type;

    private double real;
    private long integer;
    private String string = null;
    private char character;
    private boolean truth = false;
    private Value value = null;
    private Value[] values = null;
    private Expr[] exprs = null;
    private PrimitiveFunction primitiveFunction = null;
    private CombiningForm combiningForm = null;
    private Environment environment = null;
    private AbnormalType abnormalType = null;
    
    public static final Value TRUE;
    public static final Value FALSE;
    
    //
    // Value construction functions.
    //

    public static Value makeAbnormal(AbnormalType abnormalType, String name, String msg, Value val) {
        Value value = new Value(ABNORMAL);
        value.value = makeTriple(makeString(name), makeString(msg), val);
        value.abnormalType = abnormalType;
        return value;
    }

    public static Value makeSignal(Value val) {
        Value value = new Value(ABNORMAL);
        value.abnormalType = AbnormalType.SIGNAL;
        value.value = val;
        return value;
    }

    public static Value makeSingleton(Value val) {
        Value value = new Value(SEQ);
        value.values = new Value[] { val };
        return value;
    }

    public static Value makePair(Value val1, Value val2) {
        Value value = new Value(SEQ);
        value.values = new Value[] { val1, val2 };
        return value;
    }

    public static Value makeTriple(Value val1, Value val2, Value val3) {
        Value value = new Value(SEQ);
        value.values = new Value[] { val1, val2, val3 };
        return value;
    }

    public static Value makeSequence(Value[] values) {
        Value value = new Value(SEQ);
        value.values = values;
        return value;
    }

    public static Value makeString(String string) {
        Value[] values = new Value[string.length()];
        for (int i = 0; i < string.length(); i++) {
            values[i] = makeCharacter(string.charAt(i));
        }
        return makeSequence(values);
    }

    public static Value makeTruth(boolean truth) {
        return truth?TRUE:FALSE;
    }

    public static Value makeCharacter(char character) {
        Value value = new Value(CHARACTER);
        value.character = character;
        return value;
    }

    public static Value makeReal(double real) {
        Value value = new Value(REAL);
        value.real = real;
        return value;
    }

    public static Value makeInteger(long integer) {
        Value value = new Value(INTEGER);
        value.integer = integer;
        return value;
    }
    
    public static Value makeCombiningForm(String name, CombiningForm combiningForm) {
        Value value = new Value(COMB);        
        value.string = name;
        value.combiningForm = combiningForm;
        value.values = new Value[0];
        return value;
    }

    public static Value makeCombiningForm(String name, CombiningForm combiningForm, Value val) {
        Value value = new Value(COMB);
        value.string = name;
        value.combiningForm = combiningForm;
        value.values = new Value[] { val };
        return value;
    }

    public static Value makeCombiningForm(String name, CombiningForm combiningForm, Value[] values) {
        Value value = new Value(COMB);
        value.string = name;
        value.combiningForm = combiningForm;
        value.values = values;
        return value;
    }

    public static Value makePrimitiveFunction(String name, PrimitiveFunction primitiveFunction) {
        Value value = new Value(FUNCTION);
        value.string = name;
        value.primitiveFunction = primitiveFunction;
        return value;
    }

    public static Value makeFunction(String name, Value predicate, Value ifTrue, Value ifFalse) {
        Value value = new Value(FUNCTION);
        value.string = name;
        value.values = new Value[] { predicate, ifTrue, ifFalse };
        return value;
    }

    public static Value makeFunction(String name, Expr pattern, Expr ifTrue, Expr ifFalse, Environment environment) {
        Value value = new Value(FUNCTION);
        value.string = name;
        value.exprs = new Expr[] { pattern, ifTrue, ifFalse };
        value.environment = environment;
        return value;
    }

    public static Value makeLambda(Expr pattern, Expr body, Environment environment) {
        Value value = new Value(LAMBDA);
        value.exprs = new Expr[] { pattern, body };
        value.environment = environment;
        return value;
    }

    public static Value makeUser(String name, long tag, Value val) {
        Value value = new Value(USER);
        value.string = name;
        value.integer = tag;
        value.value = val;
        return value;
    }

    private Value(ValueType type) {
        this.type = type;
    }

    public final ValueType type() {
        return type;
    }

    public final boolean istype(ValueType type) {
        return this.type == type;
    }

    public final boolean isseq() {
        return this.type == SEQ;
    }

    public final boolean isabnormal() {
        return type == ABNORMAL;
    }
    
    public final AbnormalType abnormalType() {
        return abnormalType;
    }

    public final boolean isapplicable() {
        return type == FUNCTION || type == LAMBDA || type == COMB;
    }

    public final char character() {
        return character;
    }

    public final long integer() {
        return integer;
    }

    public final double real() {
        if (type == INTEGER)
            return integer;
        else
            return real;
    }

    public final boolean truth() {
        return truth;
    }

    public final PrimitiveFunction primitiveFunction() {
        return primitiveFunction;
    }

    public final CombiningForm combiningForm() {
        return combiningForm;
    }

    public final String string() {
        return string;
    }

    public final String name() {
        return string;
    }

    public final Value value() {
        return value;
    }
    
    public final Value value(int i) {
        return values[i];
    }

    public final Value[] values() {
        return values;
    }

    public final Expr pattern() {
        return exprs[0];
    }

    public final Expr body() {
        return exprs[1];
    }
    
    public final Value predicate() {
        return values[0];
    }
    
    public final Value iftruevalue() {
        if (values != null)
            return values[0];
        else
            return null;
    }

    public final Expr iftrueexpr() {
        return exprs[1];
    }

    public final Value iffalsevalue() {
        if (values.length == 2)
            return values[1];
        else
            return null;
    }

    public final Expr iffalseexpr() {
        return exprs[2];
    }

    public final Environment environment() {
        return environment;
    }

    public final boolean isstring() {
        if (type != SEQ) return false;
        if (string != null) return true;
        StringBuilder buf = new StringBuilder();
        for (Value val : values) {
            if (val.type == ValueType.CHARACTER)
                buf.append(val.character);
            else
                return false;
        }
        string = buf.toString();
        return true;
    }

    public final boolean isuser(String typename, long tag) {
        if (type != USER) return false;
        if (!name().equals(typename)) return false;
        if (integer != tag) return false;
        return true;
    }
    
    public final boolean istrue() {
        if (isabnormal())
            return false;
        else if (type == ValueType.TRUTH)
            return truth;
        else
            return true;
    }

    public final Compare compare(Value other) {
        if (other.type == ABNORMAL) return Compare.UNCOMPARABLE;
        switch (type) {
        case ABNORMAL:
            return Compare.UNCOMPARABLE;
        case TRUTH:
            switch (other.type) {
            case TRUTH:
                if (truth == other.truth)
                    return Compare.EQUAL;
                else if (truth == false)
                    return Compare.LESS;
                else
                    return Compare.GREATER;
            case CHARACTER:
            case INTEGER:
            case REAL:
            case SEQ:
                return Compare.LESS;
            default:
                return Compare.UNCOMPARABLE;
            }
        case CHARACTER:
            switch (other.type) {
            case TRUTH:
                return Compare.GREATER;
            case CHARACTER:
                if (character < other.character)
                    return Compare.LESS;
                else if (character > other.character)
                    return Compare.GREATER;
                else
                    return Compare.EQUAL;
            case INTEGER:
            case REAL:
                return Compare.LESS;
            case SEQ:
                return Compare.LESS;
            default:
                return Compare.UNCOMPARABLE;
            }
        case INTEGER:
            switch (other.type) {
            case TRUTH:
                return Compare.GREATER;
            case CHARACTER:
                return Compare.GREATER;
            case INTEGER:
                if (integer < other.integer)
                    return Compare.LESS;
                else if (integer > other.integer)
                    return Compare.GREATER;
                else
                    return Compare.EQUAL;
            case REAL:
                if (integer < other.real)
                    return Compare.LESS;
                else if (integer > other.real)
                    return Compare.GREATER;
                else
                    return Compare.EQUAL;
            case SEQ:
                return Compare.LESS;
            default:
                return Compare.UNCOMPARABLE;
            }
        case REAL:
            switch (other.type) {
            case TRUTH:
                return Compare.GREATER;
            case CHARACTER:
                return Compare.GREATER;
            case INTEGER:
                if (integer < other.integer)
                    return Compare.LESS;
                else if (integer > other.integer)
                    return Compare.GREATER;
                else
                    return Compare.EQUAL;
            case REAL:
                if (integer < other.real)
                    return Compare.LESS;
                else if (integer > other.real)
                    return Compare.GREATER;
                else
                    return Compare.EQUAL;
            case SEQ:
                return Compare.LESS;
            default:
                return Compare.UNCOMPARABLE;
            }
        case SEQ:
            switch (other.type) {
            case TRUTH:
            case CHARACTER:
            case INTEGER:
            case REAL:
                return Compare.GREATER;
            case SEQ:
                Value[] x = values;
                Value[] y = other.values;
                int len = Math.min(x.length, y.length);
                for (int i = 0; i < len; i++) {
                    Compare c = x[i].compare(y[i]);
                    if (c == Compare.LESS)
                        return Compare.LESS;
                    else if (c == Compare.GREATER)
                        return Compare.GREATER;
                    else if (c == Compare.UNCOMPARABLE)
                        return Compare.UNCOMPARABLE;
                    i++;
                }
                if (x.length < y.length)
                    return Compare.LESS;
                else if (x.length > y.length)
                    return Compare.GREATER;
                else
                    return Compare.EQUAL;
            default:
                return Compare.UNCOMPARABLE;
            }
        case USER:
            return Compare.UNCOMPARABLE;
        case LAMBDA:
            return Compare.UNCOMPARABLE;
        case FUNCTION:
            return Compare.UNCOMPARABLE;
        case COMB:
            return Compare.UNCOMPARABLE;
        }
        return Compare.UNCOMPARABLE;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Value)) return false;
        Value other = (Value)obj;
        if (type != other.type) return false;
        switch (type) {
        case ABNORMAL:
            break;
        case TRUTH:
            return truth == other.truth;
        case CHARACTER:
            return character == other.character;
        case INTEGER:
            return integer == other.integer;
        case REAL:
            return real == other.real;
        case SEQ:
            if (values.length != other.values.length)
                return false;
            for (int i = 0; i < values.length; i++)
                if (!values[i].equals(other.values[i]))
                    return false;
            return true;
        case USER:
            return string.equals(other.string) &&
                    integer == other.integer &&
                    value.equals(other.value);
        case COMB:
            break;
        case FUNCTION:
            break;
        case LAMBDA:
            break;
        }
        return false;
    }
    
    @Override
    public final String toString() {
        switch (type) {
        case ABNORMAL: {
            StringBuilder buf = new StringBuilder();
            buf.append('!');
            buf.append(abnormalType);
            buf.append('(');
            if (string != null) buf.append(string);
            if (string != null && value != null) buf.append(',');
            if (value != null) buf.append(value);
            buf.append(')');
            return buf.toString();
        }
        case CHARACTER:
            return "`"+Char.escapeChar(character);
        case FUNCTION: {
            if (primitiveFunction != null) return "builtin("+string+")";
            StringBuilder buf = new StringBuilder();
            buf.append("fun(");
            if (name() != null)
                buf.append(name());
            else
                buf.append("&");
            buf.append(')');
            return buf.toString();
        }
        case REAL:
            return Double.toString(real);
        case INTEGER:
            return Long.toString(integer);
        case TRUTH:
            return Boolean.toString(truth);
        case LAMBDA:
            return "lambda(&)";
        case SEQ: {
            if (values.length == 0) return "<>";
            if (isstring()) return '"'+Char.escapeString(string())+'"';
            StringBuilder buf = new StringBuilder("<");
            if (values.length > 0) {
                buf.append(values[0]);
                for (int i = 1; i < values.length; i++) {
                    buf.append(",");
                    buf.append(values[i]);
                }
            }
            buf.append(">");
            return buf.toString();
        }
        case COMB: {
            if (values.length == 0) return "comb("+string+")";
            StringBuilder buf = new StringBuilder();
            buf.append("comb("+string);
            for (Value value : values) {
                buf.append(',');
                buf.append(value.toString());
            }
            buf.append(')');
            return buf.toString();
        }
        case USER:
            StringBuilder buf = new StringBuilder();
            buf.append("user(");
            buf.append(name());
            buf.append(",");
            buf.append(String.format("%x", integer));
            buf.append(",");
            buf.append(value);
            buf.append(")");
            return buf.toString();
        }
        // does not occur
        return "ERROR";
    }
    
    static {
        FALSE = new Value(TRUTH);
        FALSE.truth = false;
        TRUE = new Value(TRUTH);
        TRUE.truth = true;
    }
}
