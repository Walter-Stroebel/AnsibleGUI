/*
 * Copyright (c) 2014 by Walter Stroebel and InfComTec.
 * All rights reserved.
 */
package nl.infcomtec.ansible;

import com.esotericsoftware.yamlbeans.YamlException;
import com.esotericsoftware.yamlbeans.YamlReader;
import java.io.File;
import java.io.FileReader;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import nl.infcomtec.javahtml.JHFragment;

/**
 *
 * @author walter
 */
public class AnsObject {

    public final Object object;
    public final File inFile;

    public AnsObject(final File inFile, final FileReader fileReader) throws YamlException {
        YamlReader reader = new YamlReader(fileReader);
        this.object = reader.read();
        this.inFile = inFile;
    }

    public AnsObject(final File inFile, String yaml) throws YamlException {
        YamlReader reader = new YamlReader(yaml);
        this.object = reader.read();
        this.inFile = inFile;
    }

    public Map<Object, Object> getMap() {
        if (object instanceof Map) {
            return (Map) object;
        }
        return null;
    }

    private static void toHtml(HttpServletRequest request, JHFragment top, List l) {
        for (Object e : l) {
            top.push("li");
            toHtml(request, top, e);
            top.pop();
        }

    }

    private static void toHtml(HttpServletRequest request, JHFragment top, Set<Map.Entry> set) {
        for (Map.Entry e : set) {
            top.push("li");
            top.appendText(e.getKey().toString()).appendText(": ");
            toHtml(request, top, e.getValue());
            top.pop();
        }
    }

    public static void toHtml(HttpServletRequest request, JHFragment top, Object obj) {
        if (obj instanceof Map) {
            top.push("ul");
            toHtml(request, top, (Set<Map.Entry>) ((Map) obj).entrySet());
            top.pop();
        } else if (obj instanceof List) {
            top.push("ul");
            toHtml(request, top, (List) obj);
            top.pop();
        } else if (obj instanceof String) {
            top.appendP(obj.toString());
        } else {
            throw new RuntimeException("What is this? " + obj.getClass().getName());
        }
    }
}
