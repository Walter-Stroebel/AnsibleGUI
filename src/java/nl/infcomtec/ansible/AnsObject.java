/*
 * Copyright (c) 2014 by Walter Stroebel and InfComTec.
 * All rights reserved.
 */
package nl.infcomtec.ansible;

import com.esotericsoftware.yamlbeans.YamlReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import nl.infcomtec.javahtml.JHFragment;

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

    private static void toHtml(JHFragment frag, Set<Map.Entry<AnsString, AnsElement>> set) {
        for (Entry<AnsString, AnsElement> e : set) {
            frag.push("li");
            frag.appendText(e.getKey().getString()).appendText(": ");
            toHtml(frag, e.getValue());
            frag.pop();
        }
    }

    public static void toHtml(JHFragment frag, AnsElement ae) {
        if (ae.getMap() != null) {
            frag.push("ul");
            toHtml(frag, ae.getMap().entrySet());
            frag.pop();
        } else if (ae.getList() != null) {
            frag.push("ul");
            toHtml(frag, ae.getList());
            frag.pop();
        } else if (ae.getString() != null) {
            frag.appendP(ae.getString());
        } else {
            throw new RuntimeException("What is this? " + ae.getClass().getName());
        }
    }

    private final AnsElement root;
    public final File inFile;

    public String getStringFor(String name) {
        return root.getStringFor(name);
    }

    private static class SFile extends File {

        final long lastMod;

        public SFile(final File f) {
            super(f.getAbsolutePath());
            lastMod = lastModified();
        }
    }

    public AnsObject(final PlayBooks books, final File inFile) throws IOException {
        YamlReader reader = new YamlReader(new FileReader(inFile));
        Object object = reader.read();
        reader.close();
        this.inFile = inFile;
        this.root = parse(object);
        if (books != null) {
            books.scanForVars(inFile, root);
        }
    }

    public AnsObject(final PlayBooks books, final File inFile, final String yaml) throws IOException {
        YamlReader reader = new YamlReader(new FileReader(inFile));
        Object object = reader.read();
        this.inFile = inFile;
        root = parse(object);
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
//
//    /**
//     * Serialize any object to YAML.
//     *
//     * @param obj The object to serialize.
//     * @return A String with the serialized object.
//     * @throws IOException If it does.
//     */
//    public static String makeString(AnsElement obj) throws IOException {
//        //return YamlJson.json2Yaml(toJSON(obj));
//        StringBuilder ret = new StringBuilder();
//        blech(ret, 0, obj);
//        return ret.toString();
//    }
//
//    /**
//     * Serialize this object to YAML.
//     *
//     * @return A String with the serialized object.
//     * @throws IOException If it does.
//     */
//    public String makeString() throws IOException {
//        return makeString(root);
//    }

    private AnsElement parse(Object _obj) {
        if (_obj instanceof Map) {
            Map<Object,Object> obj = (Map<Object,Object>) _obj;
            AnsMap ret = new AnsMap();
            for (Map.Entry<Object,Object> e: obj.entrySet()){
                ret.put(new AnsString(e.getKey().toString()), parse(e.getValue()));
            }
            return ret;
        } else if (_obj instanceof List) {
            AnsList ret = new AnsList();
            for (Object o : ((List)_obj)) {
                ret.add(parse(o));
            }
            return ret;
        } else {
            return new AnsString(_obj.toString());
        }
    }
//
//    private static Object toJSON(AnsElement elm) {
//        if (elm.getList() != null) {
//            JSONArray ret = new JSONArray();
//            for (AnsElement e : elm.getList()) {
//                ret.put(toJSON(e));
//            }
//            return ret;
//        } else if (elm.getMap() != null) {
//            JSONObject ret = new JSONObject();
//            for (Entry<AnsString, AnsElement> e : elm.getMap().entrySet()) {
//                ret.put(e.getKey().getString(), toJSON(e.getValue()));
//            }
//            return ret;
//        }
//        return elm.getString();
//    }
}
