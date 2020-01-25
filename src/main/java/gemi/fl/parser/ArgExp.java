package gemi.fl.parser;

import java.util.LinkedList;
import java.util.List;

public final class ArgExp {

    public List<Expr> patterns = new LinkedList<>();
    public Expr arrowExpr = null;

    public final void dump(int indent) {
        if (patterns.size() > 0) {
            Utilities.indentln(indent, "patterns");
            for (Expr pattern : patterns) {
                pattern.dump(indent+1);
            }
        }
        if (arrowExpr != null) {
            Utilities.indentln(indent, "arrow-expr");
            arrowExpr.dump(indent+1);
        }
    }
}
