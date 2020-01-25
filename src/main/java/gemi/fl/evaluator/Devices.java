package gemi.fl.evaluator;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class Devices {

    private static Map<String,PrintStream> openWriteDevices = new HashMap<>();
    private static Map<String,Reader> openReadDevices = new HashMap<>();
    
    public static Reader getReadDevice(String name) {
        Reader in = openReadDevices.get(name);
        if (in == null) {
            try {
                in = new FileReader(name);
                openReadDevices.put(name, in);
            } catch (Exception e) {}
        }
        return in;
    }
    
    public static PrintStream getWriteDevice(String name) {
        PrintStream out = openWriteDevices.get(name);
        if (out == null) {
            try {
                out = new PrintStream(name);
                openWriteDevices.put(name, out);
            } catch (Exception e) {}
        }
        return out;
    }
    
    static {
        openWriteDevices.put("scr", System.out);
        openReadDevices.put("kbd", new InputStreamReader(System.in));
    }
}
