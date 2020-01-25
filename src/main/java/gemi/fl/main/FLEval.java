package gemi.fl.main;

import java.io.*;
import java.util.Stack;

import gemi.fl.evaluator.*;
import gemi.fl.io.ValueReader;
import gemi.fl.io.ValueWriter;
import gemi.fl.parser.Env;
import gemi.fl.parser.Expr;
import gemi.fl.parser.Parser;
import gemi.fl.scanner.ErrorHandler;
import gemi.fl.scanner.Scanner;
import gemi.fl.scanner.Token;
import gemi.fl.scanner.TokenType;

public final class FLEval {

    private static String versionString = "FLEval 1.0";

    private boolean printResult = false;
    private Environment environment = PrimitiveFunctions.environment;

    public FLEval() {}

    public void setPrintResult(boolean printResult) {
        this.printResult = printResult;
    }

    public void addLibraryPath(String path) {
        Libraries.addSearchpath(path);
    }

    public int checkSyntax(String filename) throws IOException {
        Reader reader = null;
        if (filename.equals("--")) {
            reader = new InputStreamReader(System.in, "UTF-8");
            filename = "stdin";
        }
        else {
            reader = new InputStreamReader(new FileInputStream(filename), "UTF-8");
        }
        ErrorHandler errorHandler = new ErrorHandler(filename, System.err);
        Scanner scanner = new Scanner(reader, errorHandler);
        Parser parser = new Parser(scanner, errorHandler);
        parser.parse();
        if (errorHandler.errorCount > 0)
            return 1;
        else
            return 0;
    }

    public int evalFile(String filename) throws IOException {
        Reader reader = null;
        if (filename.equals("--")) {
            reader = new InputStreamReader(System.in, "UTF-8");
            filename = "stdin";
        }
        else {
            reader = new InputStreamReader(new FileInputStream(filename), "UTF-8");
        }
        ErrorHandler errorHandler = new ErrorHandler(filename, System.err);
        Scanner scanner = new Scanner(reader, errorHandler);
        Parser parser = new Parser(scanner, errorHandler);
        Expr expr = parser.parse();
        if (errorHandler.errorCount > 0) return 1;
        NaiveEvaluator evaluator = new NaiveEvaluator(PrimitiveFunctions.environment);
        Value value = evaluator.evaluate(expr);
        if (value.isabnormal()) {
            error(value);
            return 1;
        }
        if (printResult) {
            output(value);
        }
        return 0;
    }


    public Value evalString(String line, Environment environment) {
        ErrorHandler errorHandler = new ErrorHandler("stdin", System.err);
        try {
            Scanner scanner = new Scanner(new StringReader(line), errorHandler);
            Parser parser = new Parser(scanner, errorHandler);
            Expr expr = parser.parse();
            if (errorHandler.errorCount > 0) return null;
            NaiveEvaluator evaluator = new NaiveEvaluator(environment);
            return evaluator.evaluate(expr);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String scanLine(String line, Stack<Token> stack) {
        try {
            if (line.equals(":)")) {
                // complete all open brackets
                line = "";
                for (Token token : stack) {
                    switch (token.type) {
                    case BRACE_LEFT:
                        line = "}"+line;
                        break;
                    case PAREN_LEFT:
                        line = ")"+line;
                        break;
                    case CONSTRUCTION_BRACKET_LEFT:
                        line = "]"+line;
                        break;
                    case PREDICATE_CONSTRUCTION_LEFT:
                        line = "|]"+line;
                        break;
                    case SEQUENCE_BRACKET_LEFT:
                        line = ">"+line;
                        break;
                    default:                        
                    }
                }
            }
            ErrorHandler errorHandler = new ErrorHandler("stdin", null);
            Scanner scanner = new Scanner(new StringReader(line), errorHandler);
            Token token = null;
            while ((token = scanner.next()).type != TokenType.EOF) {
                switch (token.type) {
                case BRACE_LEFT:
                    stack.push(token);
                    break;
                case PAREN_LEFT:
                    stack.push(token);
                    break;
                case CONSTRUCTION_BRACKET_LEFT:
                    stack.push(token);
                    break;
                case PREDICATE_CONSTRUCTION_LEFT:
                    stack.push(token);
                    break;
                case SEQUENCE_BRACKET_LEFT:
                    stack.push(token);
                    break;
                case BRACE_RIGHT:
                    if (stack.isEmpty()) return null;
                    if (stack.pop().type != TokenType.BRACE_LEFT) return null;
                    break;
                case PAREN_RIGHT:
                    if (stack.isEmpty()) return null;
                    if (stack.pop().type != TokenType.PAREN_LEFT) return null;
                    break;
                case CONSTRUCTION_BRACKET_RIGHT:
                    if (stack.isEmpty()) return null;
                    if (stack.pop().type != TokenType.CONSTRUCTION_BRACKET_LEFT) return null;
                    break;
                case PREDICATE_CONSTRUCTION_RIGHT:
                    if (stack.isEmpty()) return null;
                    if (stack.pop().type != TokenType.PREDICATE_CONSTRUCTION_LEFT) return null;
                    break;
                case SEQUENCE_BRACKET_RIGHT:
                    if (stack.isEmpty()) return null;
                    if (stack.pop().type != TokenType.SEQUENCE_BRACKET_LEFT) return null;
                    break;
                default:
                    break;
                }
            }
            return line;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void command(String line) {
        int i = line.indexOf(' ');
        String cmd = null;
        String arg = null;
        if (i < 0) {
            cmd = line;
        }
        else if (i >= 0) {
            cmd = line.substring(0, i);
            arg = line.substring(i+1).trim();
        }
        switch (cmd) {
        case ":u":
        case ":use":
            if (arg == null) {
                error("use: missing filename");
                break;
            }
            try {
                Library library = Libraries.load(arg);
                Env env = library.env();
                NaiveEvaluator evaluator = new NaiveEvaluator(PrimitiveFunctions.environment);
                Environment environment = evaluator.evaluateEnv(env, this.environment);
                if (environment == null)
                    error("use: cannot evaluate environment in file "+arg);
                else
                    this.environment = environment;
            } catch (Exception e) {
                error("use: cannot load file "+arg);
            }
            break;
        case ":l":
        case ":load":
            if (arg == null) {
                error("load: missing filename");
                break;
            }
            try {
                printResult = true;
                evalFile(arg);
            } catch (StackOverflowError e) {
                error("load: stack overflow error");
            } catch (FileNotFoundException e) {
                error("load: file "+arg+" not found");
            } catch (Exception e) {
                error("load: cannot load file "+arg);
            }
            break;
        case ":?":
        case ":h":
        case ":help":
            output("Commands");
            output("  :load,:l FILE    load and evaluate expression from FILE");
            output("  :use,:u FILE     prepend environment in FILE");
            output("  :help,:h:,:?     display this help");
            output("  :quit,:exit:,:q  leave evaluator");
            break;
        case ":q":
        case ":quit":
        case ":exit":
            System.exit(0);
            break;
        default:
            error("unknown command: "+cmd+(arg!=null?" "+arg:""));
        }
    }

    public void prompt() {
        System.out.print(": ");
        System.out.flush();
    }

    public void interactive() {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            output(versionString);
            output("Type :h for help on commands");
            prompt();
            String line;
            Stack<Token> stack = new Stack<>();
            StringBuilder input = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                if (stack.isEmpty() && line.startsWith(":")) {
                    command(line);
                    prompt();
                    continue;
                }
                if ((line = scanLine(line, stack)) == null) {
                    error("error: syntax");
                    // clear on error
                    stack.clear();
                    input.setLength(0);
                    prompt();
                    continue;
                }
                if (input.length() > 0) input.append("\n");
                input.append(line);
                if (stack.isEmpty()) {
                    try {
                        Value value = evalString(input.toString(), environment);
                        if (value == null) {
                            //System.out.println("!ERROR");
                        }
                        else if (value.isabnormal())
                            error("error: "+value.value());
                        else
                            output(value);
                    } catch (StackOverflowError e) {
                        error("fleval: stack overflow error");
                    }
                    stack.clear();
                    input.setLength(0);
                }

                // prompt
                for (Token token : stack) System.out.print(token.name);
                prompt();
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    private static void fromFl() {
        try (ValueReader reader = new ValueReader(System.in)) {
            Value value = reader.read();
            if (value == null) {
                error("fleval: no FL data");
                System.exit(1);
            }
            if (value.isstring()) {
                output(value.string());
            }
            else {
                error("fleval: not an FL string");
                System.exit(1);
            }
            System.exit(0);
        } catch (Exception e) {
            error("fleval: cannot read FL data");
            System.exit(1);
        }
    }

    private static void error(Object msg) {
        System.err.println(msg);
    }

    private static void output(Object o) {
        System.out.println(o);
    }

    private static void toFl() {
        try (Reader in = new InputStreamReader(System.in)) {
            char[] buf = new char[1024];
            StringBuilder string = new StringBuilder();
            int len;
            while ((len = in.read(buf)) >= 0) {
                string.append(buf, 0, len);
            }
            Value value = Value.makeString(string.toString());
            ValueWriter valueWriter = new ValueWriter(System.out);
            valueWriter.write(value);
            valueWriter.close();
            System.exit(0);
        } catch (Exception e) {
            error("fleval: "+e.getMessage());
            System.exit(1);
        }

    }

    private static void version() {
        output(versionString);
        System.exit(0);
    }

    private static void help() {
        output("Usage: fleval [OPTION]... [FILE]");
        output("Evaluate FL expression in FILE.");
        output("");
        output("With no FILE start interactive evaluator.");
        output("");
        output("  -p            print the result of the evaluation");
        output("  -L            add PATH to search path for libraries");
        output("  -c            check syntax of FILE");
        output("  -to-fl        convert standard input text to FL xml");
        output("  -from-fl      convert standard input FL xml to text");
        output("  -V,--version  display version and exit");
        output("  -h,--help     display this help and exit");
        System.exit(0);
    }

    public static void main(String[] args) throws Exception {
        FLEval fleval = new FLEval();
        String progname = null;
        boolean checksyntax = false;

        for (int i = 0; i < args.length; i++) {
            if ("-p".equals(args[i])) {
                fleval.setPrintResult(true);
            }
            else if (args[i].startsWith("-L")) {
                String s = null;
                if (!args[i].equals("-L")) {
                    s = args[i].substring("-L".length());
                }
                else if (i < args.length-1) {
                    i++;
                    s = args[i];
                }
                for (String path : s.split(":")) {
                    path = path.trim();
                    if (path.length() > 0) fleval.addLibraryPath(path);
                }
            }
            else if ("-c".equals(args[i])) {
                checksyntax = true;
            }
            else if ("-V".equals(args[i]) || "--version".equals(args[i])) {
                version();
            }
            else if ("-h".equals(args[i]) || "--help".equals(args[i])) {
                help();
            }
            else if ("-to-fl".equals(args[i])) {
                toFl();
            }
            else if ("-from-fl".equals(args[i])) {
                fromFl();
            }
            else if ("--".equals(args[i])) {
                progname = "--";
            }
            else if (args[i].startsWith("-")) {
                error("fleval: unknown option "+args[i]);
                System.exit(1);
            }
            else if (progname == null) {
                progname = args[i];
            }
        }

        if (progname != null) {
            File progfile = new File(progname);
            if (progname.equals("--") || (progfile.exists() && progfile.canRead())) {
                try {
                    if (checksyntax)
                        System.exit(fleval.checkSyntax(progname));
                    else
                        System.exit(fleval.evalFile(progname));
                } catch (StackOverflowError e) {
                    error("fleval: stack overflow error");
                    System.exit(1);
                }
            }
            else {
                error("fleval: cannot open "+progfile);
                System.exit(1);
            }
        }
        else {
            fleval.interactive();
        }
    }
}
