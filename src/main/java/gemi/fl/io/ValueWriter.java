package gemi.fl.io;

import java.io.IOException;
import java.io.OutputStream;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import gemi.fl.evaluator.Value;

public class ValueWriter implements AutoCloseable {

    private OutputStream out;
    private XMLStreamWriter writer;

    public ValueWriter(OutputStream out) throws IOException {
        this.out = out;
        try {
            writer = XMLOutputFactory.newFactory().createXMLStreamWriter(out);
            writer.writeStartDocument("UTF-8", "1.0");
            nl();
            writer.writeStartElement("flvalue");
            nl();
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    public boolean write(Value value) throws XMLStreamException {
        return write(value, 1);
    }

    private boolean write(Value value, int indent) throws XMLStreamException {
        switch (value.type()) {
        case ABNORMAL:
            return false;
        case CHARACTER:
            indent(indent);
            writer.writeStartElement("character");
            writer.writeCharacters(Character.toString(value.character()));
            writer.writeEndElement();
            nl();
            break;
        case COMB:
            return false;
        case FUNCTION:
//            indent(indent);
//            if (value.primitiveFunction() != null) {
//                writer.writeStartElement("primitive");
//                writer.writeCharacters(value.string());
//                writer.writeEndElement();
//                nl();
//            }
            return false;
        case USER:
            return false;
        case INTEGER:
            indent(indent);
            writer.writeStartElement("integer");
            writer.writeCharacters(Long.toString(value.integer()));
            writer.writeEndElement();
            nl();
            break;
        case LAMBDA:
            return false;
        case REAL:
            indent(indent);
            writer.writeStartElement("real");
            writer.writeCharacters(Double.toString(value.real()));
            writer.writeEndElement();
            nl();
            break;
        case SEQ:
            if (value.isstring()) {
                indent(indent);
                writer.writeStartElement("string");
                writer.writeCharacters(value.string());
                writer.writeEndElement();
                nl();
            }
            else {
                indent(indent);
                writer.writeStartElement("seq");
                nl();
                for (Value val : value.values()) {
                    if (!write(val, indent+1)) return false;
                }
                indent(indent);
                writer.writeEndElement();
                nl();
            }
            break;
        case TRUTH:
            indent(indent);
            writer.writeStartElement("truth");
            writer.writeCharacters(Boolean.toString(value.truth()));
            writer.writeEndElement();
            nl();
            break;
        }
        return true;
    }

    @Override
    public void close() {
        if (out != null) try {
            if (writer != null) try {
                writer.writeEndElement();
                nl();
                writer.writeEndDocument();
                writer.close();
            } catch (Exception e) {}
            out.close();
        } catch (Exception e) {}
    }
    
    private void indent(int indent) throws XMLStreamException {
        for (int i = 0; i < indent; i++)
            writer.writeCharacters("  ");
    }

    private void nl() throws XMLStreamException {
        writer.writeCharacters("\n");
    }
}
