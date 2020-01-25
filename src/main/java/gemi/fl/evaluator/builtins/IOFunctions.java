package gemi.fl.evaluator.builtins;

import static gemi.fl.evaluator.AbnormalType.ARGUMENT_COUNT_ERROR;
import static gemi.fl.evaluator.AbnormalType.ARGUMENT_TYPE_ERROR;
import static gemi.fl.evaluator.AbnormalType.IO_ERROR;
import static gemi.fl.evaluator.Value.makeAbnormal;
import static gemi.fl.evaluator.Value.makeCharacter;
import static gemi.fl.evaluator.Value.makeString;
import static gemi.fl.evaluator.Value.makeTruth;

import java.io.*;

import gemi.fl.evaluator.AbnormalType;
import gemi.fl.evaluator.Devices;
import gemi.fl.evaluator.PrimitiveFunction;
import gemi.fl.evaluator.Value;
import gemi.fl.io.ValueReader;
import gemi.fl.io.ValueWriter;

/**
 * Input, output and file functions.
 */
public final class IOFunctions {

    public static PrimitiveFunction in = (arg, evaluator, environment) -> {
        if (!arg.isstring())
            return makeAbnormal(ARGUMENT_TYPE_ERROR, "in", "arg1", arg);
        String devicename = arg.string();
        Reader device = Devices.getReadDevice(devicename);
        if (device == null)
            return makeAbnormal(IO_ERROR, "in", "dev", arg);
        try {
            int c = device.read();
            if (c >= 0)
                return makeCharacter((char)c);
            else
                return makeTruth(false);
        } catch (Exception e) {
            return makeAbnormal(IO_ERROR, "in", "ioread", arg);
        }
    };

    public static PrimitiveFunction out = (arg, evaluator, environment) -> {
        if (!arg.isseq())
            return makeAbnormal(ARGUMENT_TYPE_ERROR, "out", "arg", arg);
        if (arg.values().length != 2)
            return makeAbnormal(ARGUMENT_COUNT_ERROR, "out", "arg", arg);
        if (!arg.value(0).isstring())
            return makeAbnormal(ARGUMENT_TYPE_ERROR, "out", "arg1", arg);
        if (!arg.value(1).isstring())
            return makeAbnormal(ARGUMENT_TYPE_ERROR, "out", "arg2", arg);
        String devicename = arg.value(0).string();
        PrintStream device = Devices.getWriteDevice(devicename);
        if (device == null)
            return makeAbnormal(IO_ERROR, "out", "dev", makeString(devicename));
        try {
            device.print(arg.value(1).string());
            device.flush();
            return arg;
        } catch (Exception e) {
            return makeAbnormal(AbnormalType.IO_ERROR, "out", "iowrite", arg);
        }
    };

    public static PrimitiveFunction get = (arg, evaluator, environment) -> {
        if (!arg.isstring()) return makeAbnormal(ARGUMENT_TYPE_ERROR, "get", "arg1", arg);
        String filename = arg.string();
        try (ValueReader reader = new ValueReader(new FileInputStream(filename))) {
            Value value = reader.read();
            if (value == null)
                return makeAbnormal(IO_ERROR, "get", "ioread", arg);
            else
                return value;
        }
        catch (Exception e) {
            return makeAbnormal(IO_ERROR, "get", "ioread", arg);
        }
    };

    public static PrimitiveFunction put = (arg, evaluator, environment) -> {
        if (!arg.isseq())
            makeAbnormal(ARGUMENT_TYPE_ERROR, "put", "arg1", arg);
        if (arg.values().length != 2)
            makeAbnormal(ARGUMENT_COUNT_ERROR, "put", "arg1", arg);
        if (!arg.value(0).isstring())
            return makeAbnormal(ARGUMENT_TYPE_ERROR, "put", "arg1", arg);
        String filename = arg.value(0).string();
        try (ValueWriter writer = new ValueWriter(new PrintStream(filename))) {
            boolean res = writer.write(arg.value(1));
            if (res) return arg;
            new File(filename).delete();
            return makeAbnormal(IO_ERROR, "put", "val", arg);
        }
        catch (Exception e) {
            return makeAbnormal(IO_ERROR, "put", "iowrite", arg);
        }
    };
}
