/*
 * Copyright (c) 2014 by Walter Stroebel and InfComTec.
 * All rights reserved.
 */
package nl.infcomtec.javahtml;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Writer;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;

/**
 * Mostly a wrapper around a HTML DOM document.
 *
 * @author walter
 */
public class JHDocument {

    public final Document doc;
    private final DocumentBuilder docBuilder;

    public JHDocument() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        docBuilder = factory.newDocumentBuilder();
        this.doc = docBuilder.newDocument();
    }


    public JHDocument(BufferedReader template,String topElement) throws Exception {
        this();
        JHFragment frag = new JHFragment(this,topElement);
        for (String s = template.readLine(); s != null; s = template.readLine()) {
            if (s.isEmpty()) {
                frag.createElement("br");
            } else {
                switch (s.charAt(0)) {
                    case '/':
                        frag=frag.pop();
                        break;
                    case '"':
                        frag.appendText(s.substring(1));
                        break;
                    case '+': {
                        int eq = s.indexOf('=');
                        if (eq > 0) {
                            frag.appendAttr(s.substring(1, eq).trim(), s.substring(eq + 1).trim());
                        } else {
                            frag.appendAttr(s.substring(1).trim());
                        }
                    }
                    break;
                    default: {
                        frag=frag.push(s);
                    }
                    break;
                }
            }
        }
    }

    public void write(Writer out) throws Exception {
        Transformer t = TransformerFactory.newInstance().newTransformer();
        t.setOutputProperty(OutputKeys.METHOD, "html");
        t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        t.setOutputProperty(OutputKeys.INDENT, "yes");
        t.transform(new DOMSource(doc), new StreamResult(out));
    }

    public void write(final PrintStream out) throws Exception {
        write(new Writer() {
            @Override
            public void write(char[] cbuf, int off, int len) throws IOException {
                String s = new String(cbuf, off, len);
                out.write(s.getBytes());
            }

            @Override
            public void flush() throws IOException {
                out.flush();
            }

            @Override
            public void close() throws IOException {
                out.close();
            }
        });
    }

    public void write(final StringBuilder out) throws Exception {
        write(new Writer() {
            @Override
            public void write(char[] cbuf, int off, int len) throws IOException {
                out.append(cbuf, off, len);
            }

            @Override
            public void flush() throws IOException {
            }

            @Override
            public void close() throws IOException {
            }
        });
    }
}
