package gemi.fl.evaluator;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import gemi.fl.evaluator.builtins.SequenceFunctions;
import gemi.fl.parser.EnvType;

import static gemi.fl.evaluator.Value.*;

public final class Environment {

    private EnvType type;

    private Map<String,Value> bindings = new HashMap<>();
    private Environment next = null;
    private Collection<String> namelist = null;
    private Environment env1;
    private Environment env2;
    private Value abnormal = null;
    
    public static Environment makeAbnormal(Value val, Environment next) {
        Environment environment = new Environment(EnvType.ABNORMAL, next);
        environment.abnormal = val;
        return environment;
    }

    public Environment(EnvType type, Environment next) {
        this.type = type;
        this.next = next;
    }

    public Environment(EnvType type, Environment env1, Environment env2, Environment next) {
        this.type = type;
        this.next = next;
        this.env1 = env1;
        this.env2 = env2;
    }

    public Environment(EnvType type, Collection<String> namelist, Environment env1, Environment next) {
        this(type, next);
        this.namelist = namelist;
        this.env1 = env1;
    }

    public final Value lookup(String name) {
        switch (type) {
        case PF: {
            Value value = bindings.get(name);
            if (value == null) {
                if (name.matches("s[1-9][0-9]*")) {
                    value = SequenceFunctions.s(Integer.parseInt(name.substring(1)));
                    bindings.put(name, value);
                }
                else if (name.matches("r[1-9][0-9]*")) {
                    value = SequenceFunctions.r(Integer.parseInt(name.substring(1)));
                    bindings.put(name, value);
                }
            }
            return value;
        }
        case DEFNLIST: {
            Value value = bindings.get(name);
            if (value == null) value = next.lookup(name);
            return value;
        }
        case EXPORT: {
            if (namelist.contains(name))
                return env1.lookup(name);
            else
                return next.lookup(name);
        }
        case HIDE: {
            if (namelist.contains(name))
                return next.lookup(name);
            else
                return env1.lookup(name);
        }
        case REC: {
            Value value = null;
            if (namelist.contains(name) && env1 != null) value = env1.lookup(name);
            if (value == null) value = next.lookup(name);
            return value;
        }
        case UNION: {
            Value value = env1.lookup(name);
            if (value == null) value = env2.lookup(name);
            if (value == null && next != null) value = next.lookup(name);
            return value;
        }
        case USES: {
            Value value = env1.lookup(name);
            if (value == null) value = env2.lookup(name);
            if (value == null && next != null) value = next.lookup(name);
            return value;
        }
        case WHERE: {
            Value value = env1.lookup(name);
            if (value == null) value = next.lookup(name);
            return value;
        }
        case LIB:
            // does not occur
            break;
        case ABNORMAL:
            return abnormal;
        }
        return Value.makeAbnormal(AbnormalType.ENVIRONMENT_ERROR, name, "abnormal environment", makeTruth(false));
    }
    
    public final EnvType type() {
        return this.type;
    }
    
    public final Environment next() {
        return next;
    }

    public final void bind(String name, Value value) {
        bindings.put(name, value);
    }
    
    public final void bind(Map<String,Value> bindings) {
        this.bindings = bindings;
    }
    
    public final Map<String,Value> bindings() {
        return bindings;
    }
    
    public final Set<String> names() {
        return bindings().keySet();
    }
    
    public final void env1(Environment e) {
        this.env1 = e;
    }

    public final void dump() {
        for (Entry<String,Value> entries : bindings.entrySet()) {
            System.out.println(entries.getKey()+": "+entries.getValue());
        }
    }

    public final void dumpAll() {
        if (type == EnvType.PF) {
            System.out.println("PF");
            return;
        }
        for (Entry<String,Value> entries : bindings.entrySet()) {
            System.out.println(entries.getKey()+": "+entries.getValue());
        }
        if (next != null) {
            System.out.println("next:");
            next.dumpAll();
        }
    }
}
