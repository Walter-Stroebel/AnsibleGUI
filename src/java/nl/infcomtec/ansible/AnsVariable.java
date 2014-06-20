/*
 * Copyright (c) 2014 by Walter Stroebel and InfComTec.
 * All rights reserved.
 */
package nl.infcomtec.ansible;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 *
 * @author walter
 */
public class AnsVariable {

    public final TreeSet<File> definedIn = new TreeSet<>();
    public final TreeSet<File> usedBy = new TreeSet<>();
    public final String name;
    public final TreeSet<String> values = new TreeSet<>();

    public AnsVariable(final String name) {
        this.name = name;
    }

    private AnsVariable(final String name, final String value) {
        this(name);
        this.values.add(value);
    }

    private AnsVariable(final File definer, final String name, final String value) {
        this(name, value);
        definedIn.add(definer);
    }

    public static void addOrUpdate(TreeMap<String, AnsVariable> vars, File hv, Map<Object, Object> entries, TreeMap<String, String> hvars) {
        for (Map.Entry<Object, Object> e : entries.entrySet()) {
            if (e.getValue() instanceof Map) {
                for (Map.Entry<String, Object> e2 : ((Map<String, Object>) e.getValue()).entrySet()) {
                    String vNam = e.getKey().toString() + "." + e2.getKey();
                    String vVal = e2.getValue().toString();
                    addOrUpdateVar(vars, vNam, hv, vVal, hvars);
                }
            } else if (e.getValue() instanceof List) {
                int i = 0;
                for (Object e2 : (List) e.getValue()) {
                    Map<Object, Object> m2 = new HashMap<>();
                    for (Map.Entry<Object, Object> e3 : ((Map<Object, Object>) e2).entrySet()) {
                        m2.put(e.getKey() + "[" + i + "]." + e3.getKey(), e3.getValue());
                    }
                    i++;
                    addOrUpdate(vars, hv, m2, hvars);
                }
            } else {
                String vNam = e.getKey().toString();
                String vVal = e.getValue().toString();
                addOrUpdateVar(vars, vNam, hv, vVal, hvars);
            }
        }

    }

    private static void addOrUpdateVar(TreeMap<String, AnsVariable> vars, String vNam, File hv, String vVal, TreeMap<String, String> hvars) {
        AnsVariable vre = vars.get(vNam);
        if (vre == null) {
            vre = new AnsVariable(hv, vNam, vVal);
            vars.put(vNam, vre);
        } else {
            vre.definedIn.add(hv);
            vre.values.add(vVal);
        }
        if (hvars != null) {
            hvars.put(vNam, vVal);
        }
    }

}
