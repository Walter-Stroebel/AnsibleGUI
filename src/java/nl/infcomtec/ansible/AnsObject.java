/*
 * Copyright (c) 2014 by Walter Stroebel and InfComTec.
 * All rights reserved.
 */
package nl.infcomtec.ansible;

import com.esotericsoftware.yamlbeans.YamlConfig;
import com.esotericsoftware.yamlbeans.YamlException;
import com.esotericsoftware.yamlbeans.YamlReader;
import com.esotericsoftware.yamlbeans.YamlWriter;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import nl.infcomtec.javahtml.JHFragment;

/**
 *
 * @author walter
 */
public class AnsObject {

    /**
     *
     * @author walter
     */
    private static class MyWriter extends Writer {

        private final StringBuilder out;

        public MyWriter() {
            out = new StringBuilder();
        }

        @Override
        public void write(char[] cbuf, int off, int len) throws IOException {
            out.append(cbuf, off, len);
        }

        @Override
        public void flush() throws IOException {
        }

        @Override
        public void close() throws IOException {
            for (int dash = out.indexOf("\n-"); dash != -1; dash = out.indexOf("\n-", dash)) {
                out.insert(dash, '\n');
                dash += 3;
            }
        }

        @Override
        public String toString() {
            return out.toString();
        }

    }

    public static Map<Object, Object> getMap(Object object) {
        if (object instanceof Map) {
            return (Map) object;
        }
        return null;
    }

    private static void toHtml(JHFragment frag, List l) {
        for (Object e : l) {
            frag.push("li");
            toHtml(frag, e);
            frag.pop();
        }
    }

    private static void toHtml(JHFragment frag, Set<Map.Entry> set) {
        for (Map.Entry e : set) {
            frag.push("li");
            frag.appendText(e.getKey().toString()).appendText(": ");
            toHtml(frag, e.getValue());
            frag.pop();
        }
    }

    public static void toHtml(JHFragment frag, Object obj) {
        if (obj instanceof Map) {
            frag.push("ul");
            toHtml(frag, (Set<Map.Entry>) ((Map) obj).entrySet());
            frag.pop();
        } else if (obj instanceof List) {
            frag.push("ul");
            toHtml(frag, (List) obj);
            frag.pop();
        } else if (obj instanceof String) {
            frag.appendP(obj.toString());
        } else {
            throw new RuntimeException("What is this? " + obj.getClass().getName());
        }
    }

    public final Object object;
    public final File inFile;

    public AnsObject(final PlayBooks books, final File inFile, final FileReader fileReader) throws YamlException {
        YamlReader reader = new YamlReader(fileReader);
        this.object = reader.read();
        this.inFile = inFile;
        if (books != null) {
            books.scanForVars(inFile, object);
        }
    }

    public AnsObject(final PlayBooks books, final File inFile, final String yaml) throws YamlException {
        YamlReader reader = new YamlReader(yaml);
        this.object = reader.read();
        this.inFile = inFile;
        if (books != null) {
            books.scanForVars(inFile, object);
        }
    }

    public Map<Object, Object> getMap() {
        return getMap(object);
    }

    private boolean removeElement(Object object, String element) {
        if (object instanceof List) {
            List l = (List) object;
            if (l.remove(element)) {
                return true;
            }
            for (Iterator it = l.iterator(); it.hasNext();) {
                Object sub = it.next();
                if (removeElement(sub, element)) {
                    it.remove();
                    return true;
                }
            }
        } else if (object instanceof Map) {
            Map<Object, Object> map = getMap(object);
            if (map.containsValue(element)) {
                map.clear();
                return true;
            }
            for (Object sub : map.values()) {
                return removeElement(sub, element);
            }
        }
        return false;
    }

    public boolean removeElement(String element) {
        return removeElement(object, element);
    }

    public void toHtml(JHFragment frag) {
        toHtml(frag, object);
    }

    /**
     * Serialize any object to YAML. Special feature: adds blank lines to
     * top-level list elements.
     *
     * @param obj The object to serialize.
     * @return A String with the serialized object.
     * @throws YamlException If YamlWriter does.
     */
    public static String makeString(Object obj) throws YamlException {
        MyWriter myw = new MyWriter();
        YamlConfig config = new YamlConfig();
        config.writeConfig.setWrapColumn(150);
        YamlWriter writer = new YamlWriter(myw, config);
        writer.write(obj);
        writer.close();
        return myw.toString();

    }

    /**
     * Serialize this object to YAML. Special feature: adds blank lines to
     * top-level list elements.
     *
     * @return A String with the serialized object.
     * @throws YamlException If YamlWriter does.
     */
    public String makeString() throws YamlException {
        return makeString(object);
    }
}
