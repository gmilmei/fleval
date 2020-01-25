package gemi.fl.parser;

import static gemi.fl.parser.Utilities.indentln;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import gemi.fl.evaluator.Libraries;
import gemi.fl.evaluator.Library;
import gemi.fl.evaluator.PrimitiveFunctions;

public final class Env {

    private EnvType type;

    private Env env1 = null;
    private Env env2 = null;
    private List<Defn> defnlist = null;
    private Collection<String> namelist = null;
    private String lib = null;
    private Collection<String> D = null;

    public Env(EnvType type) {
        this.type = type;
    }

    public Env(EnvType type, Env env1, Env env2) {
        this.type = type;
        this.env1 = env1;
        this.env2 = env2;
    }

    public final EnvType type() {
        return type;
    }

    public final Env env1() {
        return env1;
    }

    public final void env1(Env env) {
        this.env1 = env;
    }

    public final Env env2() {
        return env2;
    }

    public final void env2(Env env) {
        this.env2 = env;
    }

    public final void lib(String lib) {
        this.lib = lib;
    }

    public final String lib() {
        return lib;
    }

    public final Collection<String> namelist() {
        if (namelist == null) namelist = new HashSet<>();
        return namelist;
    }

    public final List<Defn> defnlist() {
        return defnlist;
    }

    public final void defnlist(List<Defn> defnlist) {
        this.defnlist = defnlist;
    }

    public final Collection<String> D() {
        if (D != null) return D;
        D = new HashSet<>();
        switch (type) {
        case DEFNLIST:
            for (Defn defn : defnlist) {
                switch (defn.type()) {
                case NRDEF:
                case DEF:
                    D.add(defn.name());
                    break;
                case TYPE:
                    D.add("mk"+defn.name());
                    D.add("is"+defn.name());
                    D.add("un"+defn.name());
                    break;
                case SIG:
                case ASN:
                default:
                    break;
                }
            }
            break;
        case EXPORT:
            D.addAll(namelist);
        case HIDE:
            for (String name : env1.D()) {
                if (!namelist.contains(name)) D.add(name);
            }
            break;
        case LIB:
            Library library = Libraries.lookup(lib);
            if (library != null) D.addAll(library.env().D());
            break;
        case PF:
            D.addAll(PrimitiveFunctions.environment.names());
            break;
        case REC:
            D.addAll(env1.D());
            break;
        case UNION:
            D.addAll(env1.D());
            D.addAll(env2.D());
            break;
        case USES:
            D.addAll(env1.D());
            D.addAll(env2.D());
            break;
        case WHERE:
            D.addAll(env1.D());
            break;
        default:
            break;
        }
        return D;
    }
    
    public final void dump(int indent) {
        switch (type) {
        case DEFNLIST:
            indentln(indent, type);
            for (Defn defn: defnlist) {
                defn.dump(indent+1);
            }
            break;
        case EXPORT:
        case HIDE:
            indentln(indent, type+": "+namelist);
            env1.dump(indent+1);
            break;
        case LIB:
            indentln(indent, type+": "+lib);
            break;
        case PF:
            indentln(indent, type);
            break;
        case REC:
            indentln(indent, type);
            break;
        case UNION:
            indentln(indent, type);
            env1.dump(indent+1);
            env2.dump(indent+1);
            break;
        case USES:
            indentln(indent, type);
            break;
        case WHERE:
            indentln(indent, type);
            env1.dump(indent+1);
            env2.dump(indent+1);
            break;
        case ABNORMAL:
            indentln(indent, type);
            break;
        }
    }
}
