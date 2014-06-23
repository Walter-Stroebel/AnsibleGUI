/*
 * Copyright (c) 2014 by Walter Stroebel and InfComTec.
 * All rights reserved.
 */
package nl.infcomtec.ansible;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import nl.infcomtec.javahtml.JHFragment;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Using JSON.
 *
 * @author walter
 */
public class AnsObject {


    public static class AnsString implements AnsElement, Comparable<AnsString> {

        final String s;

        public AnsString(String s) {
            this.s = s;
        }

        @Override
        public String toString() {
            throw new RuntimeException("I don't think you want this");
            //return "\nAnsString{" + "s=" + s + '}';
        }

        /**
         * @return the s
         */
        @Override
        public String getString() {
            return s;
        }

        @Override
        public int compareTo(AnsString o) {
            return s.compareTo(o.s);
        }

        @Override
        public AnsList getList() {
            return null;
        }

        @Override
        public AnsMap getMap() {
            return null;
        }

    }

    public static class AnsList extends ArrayList<AnsElement> implements AnsElement {

        @Override
        public String toString() {
            StringBuilder ret = new StringBuilder("\nAnsList{");
            for (AnsElement e : this) {
                ret.append(", ");
                ret.append(e);
            }
            return ret.append("}").toString();
        }

        @Override
        public String getString() {
            return null;
        }

        @Override
        public AnsList getList() {
            return this;
        }

        @Override
        public AnsMap getMap() {
            return null;
        }

    }

    public static class AnsMap extends TreeMap<AnsString, AnsElement> implements AnsElement {

        @Override
        public String toString() {
            StringBuilder ret = new StringBuilder("\nAnsMap{");
            for (Entry<AnsString, AnsElement> e : this.entrySet()) {
                ret.append(", ");
                ret.append(e.getKey());
                ret.append("=");
                ret.append(e.getValue());
            }
            return ret.append("}").toString();
        }

        @Override
        public String getString() {
            return null;
        }

        @Override
        public AnsList getList() {
            return null;
        }

        @Override
        public AnsMap getMap() {
            return this;
        }

        public AnsElement get(String key) {
            return get(new AnsString(key));
        }

        public AnsElement remove(String key) {
            return remove(new AnsString(key));
        }
    }

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

    private final Object object;
    private final AnsElement root;
    public final File inFile;
    private static final TreeMap<File, AnsObject> cache = new TreeMap<>();

    public AnsObject(final PlayBooks books, final File inFile) throws IOException {
        synchronized (cache) {
            Entry<File, AnsObject> have = cache.ceilingEntry(inFile);
            if (have != null
                    && have.getKey().getAbsolutePath().equals(inFile.getAbsolutePath())
                    && have.getKey().lastModified() == inFile.lastModified()) {
                this.object = have.getValue().object;
                this.inFile = have.getValue().inFile;
                this.root = parse(this.object);
            } else {
                this.object = YamlJson.yaml2Json(inFile);
                this.inFile = inFile;
                this.root = parse(this.object);
                cache.put(inFile, this);
            }
        }
        if (books != null) {
            books.scanForVars(inFile, root);
        }
    }

    public AnsObject(final PlayBooks books, final File inFile, final String yaml) throws IOException {
        synchronized (cache) {
            this.object = YamlJson.yaml2Json(yaml);
            this.inFile = inFile;
            // invalidated, if exists
            cache.remove(inFile);
            root = parse(object);
        }
        if (books != null) {
            books.scanForVars(inFile, root);
        }
    }

    private boolean removeElement(AnsElement object, String element) {
        if (object.getList() != null) {
            if (object.getList().remove(new AnsString(element))) {
                return true;
            }
            for (AnsElement sub : object.getList()) {
                if (removeElement(sub, element)) {
                    return true;
                }
            }
        } else if (object.getMap() != null) {
            if (object.getMap().remove(element) != null) {
                return true;
            }
            for (AnsElement sub : object.getMap().values()) {
                return removeElement(sub, element);
            }
        }
        return false;
    }

    public AnsElement get() {
        return root;
    }

    public AnsMap getMap() {
        return root.getMap();
    }

    public AnsList getList() {
        return root.getList();
    }

    public boolean removeElement(String element) {
        return removeElement(root, element);
    }

    public void toHtml(JHFragment frag) {
        toHtml(frag, object);
    }

    /**
     * Serialize any object to YAML.
     *
     * @param obj The object to serialize.
     * @return A String with the serialized object.
     * @throws IOException If it does.
     */
    public static String makeString(AnsElement obj) throws IOException {
        return YamlJson.json2Yaml(toJSON(obj));
    }

    /**
     * Serialize this object to YAML.
     *
     * @return A String with the serialized object.
     * @throws IOException If it does.
     */
    public String makeString() throws IOException {
        return makeString(root);
    }

    private AnsElement parse(Object _obj) {
        if (_obj instanceof JSONObject) {
            JSONObject obj = (JSONObject) _obj;
            AnsMap ret = new AnsMap();
            for (Iterator it = obj.keys(); it.hasNext();) {
                String key = (String) it.next();
                ret.put(new AnsString(key), parse(obj.get(key)));
            }
            return ret;
        } else if (_obj instanceof JSONArray) {
            JSONArray arr = (JSONArray) _obj;
            AnsList ret = new AnsList();
            for (int i = 0; i < arr.length(); i++) {
                ret.add(parse(arr.get(i)));
            }
            return ret;
        } else {
            return new AnsString(_obj.toString());
        }
    }

    private static Object toJSON(AnsElement elm) {
        if (elm.getList() != null) {
            JSONArray ret = new JSONArray();
            for (AnsElement e : elm.getList()) {
                ret.put(toJSON(e));
            }
            return ret;
        } else if (elm.getMap() != null) {
            JSONObject ret = new JSONObject();
            for (Entry<AnsString, AnsElement> e : elm.getMap().entrySet()) {
                ret.put(e.getKey().getString(), toJSON(e.getValue()));
            }
            return ret;
        }
        return elm.getString();
    }
}
