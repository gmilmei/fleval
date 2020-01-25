package gemi.fl.evaluator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import gemi.fl.parser.Env;
import gemi.fl.parser.Parser;
import gemi.fl.scanner.ErrorHandler;
import gemi.fl.scanner.Scanner;

public class Libraries {

    private static Map<String,Library> libraries = new HashMap<>();
    private static List<String> searchpaths = new LinkedList<>();
    
    public static void addSearchpath(String path) {
        if (!searchpaths.contains(path)) searchpaths.add(path);
    }
    
    public static Library lookup(String name) {
        return libraries.get(name);
    }
    
    /**
     * Loads the library <code>name</code>.
     * @return <code>null</code> if the library cannot be loaded or parsed
     */
    public static Library load(String name) throws FileNotFoundException, IOException {
        Library library = libraries.get(name);
        if (library != null) return library;
        File file = findfile(name);
        if (file == null) throw new FileNotFoundException();
        try {
            ErrorHandler errorHandler = new ErrorHandler(name, System.err);
            Scanner scanner = new Scanner(new FileReader(file), errorHandler);
            Parser parser = new Parser(scanner, errorHandler);
            Env env = parser.parseEnv();
            if (errorHandler.errorCount > 0) throw new IOException();
            library = new Library(name, env);
            libraries.put(name, library);
            return library;
        } catch (FileNotFoundException e) {
            throw e;
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new IOException(e);
        }
    }
    
    public static Library get(String name) {
        return libraries.get(name);
    }
    
    private static File findfile(String name) {
        File file = new File(name);
        if (file.exists() && file.isFile() && file.canRead())
            return file;
        for (String searchpath : searchpaths) {
            file = new File(searchpath, name);
            if (file.exists() && file.isFile() && file.canRead())
                return file;
        }
        return null;
    }
}
