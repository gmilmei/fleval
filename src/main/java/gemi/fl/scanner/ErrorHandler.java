package gemi.fl.scanner;

import java.io.PrintStream;

public final class ErrorHandler {

    // current number of errors
    public int errorCount = 0;
    // maximum number of errors to output
    public int maxErrorCount = 5;
    public String filename;

    private PrintStream out = null;

    public ErrorHandler(String filename, PrintStream out) {
        this.filename = filename;
        this.out = out;
    }

    public final void error(int line, int col, String text) {
        if (errorCount < maxErrorCount)
            if (out != null) out.println(filename+":"+(line+1)+":"+(col+1)+": error: "+text);
        errorCount++;
    }

    public final void warning(int line, int col, String text) {
        if (out != null) out.println(filename+":"+(line+1)+":"+(col+1)+": warning: "+text);
    }
}
