package gemi.fl.io;

import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import gemi.fl.evaluator.Value;
import static gemi.fl.evaluator.Value.*;

public class ValueReader implements AutoCloseable {

    private InputStream in;
    private XMLStreamReader reader;

    public ValueReader(InputStream in) throws IOException {
        this.in = in;
        try {
            reader = XMLInputFactory.newFactory().createXMLStreamReader(in);
        } catch (Exception e) {
            throw new IOException(e);
        }
    }
    
    public Value read() throws XMLStreamException {
        Stack<List<Value>> seqstack = new Stack<>();
        while (reader.hasNext()) {
            int next = reader.next();
            switch (next) {
            case START_ELEMENT: {
                String tag = reader.getLocalName();
                switch (tag) {
                case "truth": {
                    String truth = reader.getElementText();
                    Value value;
                    if (truth.equals("false"))
                        value = makeTruth(false);
                    else
                        value = makeTruth(true);
                    if (seqstack.isEmpty())
                        return value;
                    else
                        seqstack.peek().add(value);
                    break;
                }
                case "character": {
                    String character = reader.getElementText();
                    Value value;
                    if (character.length() > 0)
                        value = makeCharacter(character.charAt(0));
                    else
                        return null;
                    if (seqstack.isEmpty())
                        return value;
                    else
                        seqstack.peek().add(value);
                    break;
                }
                case "integer": {
                    String integer = reader.getElementText();
                    Value value;
                    try {
                        value = makeInteger(Long.parseLong(integer));
                    } catch (Exception e) {
                        return null;
                    }
                    if (seqstack.isEmpty())
                        return value;
                    else
                        seqstack.peek().add(value);
                    break;
                }
                case "real": {
                    String real = reader.getElementText();
                    Value value;
                    try {
                        value = makeReal(Double.parseDouble(real));
                    } catch (Exception e) {
                        return null;
                    }
                    if (seqstack.isEmpty())
                        return value;
                    else
                        seqstack.peek().add(value);
                    break;
                }
                case "string":
                    String string = reader.getElementText();
                    Value value = makeString(string);
                    if (seqstack.isEmpty())
                        return value;
                    else
                        seqstack.peek().add(value);
                    break;
                case "seq":
                    seqstack.push(new LinkedList<>());
                    break;
                case "flvalue":
                    break;
                default:
                    return null;
                }
                break;
            }
            case END_ELEMENT:
                String tag = reader.getLocalName();
                switch (tag) {
                case "seq":
                    List<Value> values = seqstack.pop();
                    Value value = makeSequence(values.toArray(new Value[values.size()]));
                    if (seqstack.isEmpty())
                        return value;
                    else
                        seqstack.peek().add(value);
                    break;
                }
            default:
            }
        }
        
        return null;
    }

    @Override
    public void close() throws Exception {
        if (in != null) in.close();
    }
}
