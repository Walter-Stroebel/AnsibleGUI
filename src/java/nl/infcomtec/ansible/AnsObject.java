/*
 * Copyright (c) 2014 by Walter Stroebel and InfComTec.
 * All rights reserved.
 */
package nl.infcomtec.ansible;

import java.io.File;
import java.io.IOException;
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

    private static void toHtml(JHFragment frag, AnsList l) {
        for (AnsElement e : l) {
            frag.push("li");
            toHtml(frag, e);
            frag.pop();
        }
    }

    private static void toHtml(JHFragment frag, Set<Map.Entry<AnsString,AnsElement>> set) {
        for (Entry<AnsString, AnsElement> e : set) {
            frag.push("li");
            frag.appendText(e.getKey().getString()).appendText(": ");
            toHtml(frag, e.getValue());
            frag.pop();
        }
    }

    public static void toHtml(JHFragment frag, AnsElement ae) {
        if (ae.getMap()!=null) {
            frag.push("ul");
            toHtml(frag, ae.getMap().entrySet());
            frag.pop();
        } else if (ae.getList()!=null) {
            frag.push("ul");
            toHtml(frag, ae.getList());
            frag.pop();
        } else if (ae.getString()!=null) {
            frag.appendP(ae.getString());
        } else {
            throw new RuntimeException("What is this? " + ae.getClass().getName());
        }
    }

    private static void blech(StringBuilder ret, int indent, AnsElement ae) {
        if (ae.getString()!=null) {
            StringBuilder q = new StringBuilder();
            boolean needQ = false;
            for (char c : ae.getString().toCharArray()) {
                if (c == '"') {
                    q.append("\\\"");
                    needQ = true;
                } else if (c == '{') {
                    q.append(c);
                    needQ = true;
                } else if (c == '\'') {
                    q.append(c);
                    needQ = true;
                } else if (c == '\\') {
                    q.append("\\\\");
                    needQ = true;
                } else if (c == '\n') {
                    q.append("\\n");
                    needQ = true;
                } else if (c == '\r') {
                    // double blech!
                    q.append("\\r");
                    needQ = true;
                } else if (c == '\t') {
                    // tripple blech!
                    q.append("\\t");
                    needQ = true;
                } else {
                    q.append(c);
                }
            }
            if (needQ) {
                q.insert(0, '"');
                q.append('"');
            }
            ret.append(q);
        } else if (ae.getList() != null) {
            AnsList l = ae.getList();
            for (int i = 0; i < l.size(); i++) {
                for (int j = 0; j < indent; j++) {
                    ret.append("  ");
                }
                ret.append("- ");
                blech(ret, indent + 1, l.get(i));
                if (indent == 0 && i < l.size() - 1) {
                    ret.append("\n\n");
                } else {
                    ret.append("\n");
                }
            }
        } else {
            Set<AnsString> names = ae.getMap().keySet();
            int i = 0;
            for (AnsString name:names) {
                if (i > 0) {
                    ret.append("\n");
                    for (int j = 0; j < indent; j++) {
                        ret.append("  ");
                    }
                }
                blech(ret, indent, name);
                AnsElement ae2 = ae.getMap().get(name);
                if (ae2.getString() != null) {
                    ret.append(": ");
                    blech(ret, indent, ae2);
                } else {
                    ret.append(":\n");
                    if (ae2.getMap() != null) {
                        for (int j = 0; j < indent + 1; j++) {
                            ret.append("  ");
                        }
                    }
                    blech(ret, indent + 1, ae2);
                }
                i++;
            }
        }
    }

    private final Object object;
    private final AnsElement root;
    public final File inFile;

    private static class SFile extends File {

        final long lastMod;

        public SFile(final File f) {
            super(f.getAbsolutePath());
            lastMod = lastModified();
        }
    }
    private static final TreeMap<SFile, AnsObject> cache = new TreeMap<>();

    public AnsObject(final PlayBooks books, final File inFile) throws IOException {
        synchronized (cache) {
            Entry<SFile, AnsObject> have = cache.ceilingEntry(new SFile(inFile));
            if (have != null
                    && have.getKey().getAbsolutePath().equals(inFile.getAbsolutePath())
                    && have.getKey().lastMod == inFile.lastModified()) {
                this.object = have.getValue().object;
                this.inFile = have.getValue().inFile;
                this.root = parse(this.object);
            } else {
                this.object = YamlJson.yaml2Json(inFile);
                this.inFile = inFile;
                this.root = parse(this.object);
                cache.put(new SFile(inFile), this);
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
            cache.remove(new SFile(inFile));
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
        toHtml(frag, root);
    }

    /**
     * Serialize any object to YAML.
     *
     * @param obj The object to serialize.
     * @return A String with the serialized object.
     * @throws IOException If it does.
     */
    public static String makeString(AnsElement obj) throws IOException {
        //return YamlJson.json2Yaml(toJSON(obj));
        StringBuilder ret = new StringBuilder();
        blech(ret, 0, obj);
        return ret.toString();
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
