package gemi.fl.parser;

public final class Utilities {

    public static void indent(int indent, Object s) {
        for (int i = 0; i < indent; i++) System.out.print("  ");
        System.out.print(s);
    }

    public static void indentln(int indent, Object s) {
        for (int i = 0; i < indent; i++) System.out.print("  ");
        System.out.println(s);
    }
}
