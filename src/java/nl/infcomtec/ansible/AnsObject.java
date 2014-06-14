/*
 * Copyright (c) 2014 by Walter Stroebel and InfComTec.
 * All rights reserved.
 */
package nl.infcomtec.ansible;

import com.esotericsoftware.yamlbeans.YamlException;
import com.esotericsoftware.yamlbeans.YamlReader;
import java.io.File;
import java.io.FileReader;
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
}
