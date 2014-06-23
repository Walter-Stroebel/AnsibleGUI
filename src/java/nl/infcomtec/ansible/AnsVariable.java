/*
 * Copyright (c) 2014 by Walter Stroebel and InfComTec.
 * All rights reserved.
 */
package nl.infcomtec.ansible;

import java.io.File;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import nl.infcomtec.ansible.AnsObject.AnsString;

/**
 *
 * @author walter
 */
public class AnsVariable {

    public final TreeSet<File> definedIn = new TreeSet<>();
    public final TreeSet<File> usedBy = new TreeSet<>();
    public final String name;
    public final TreeSet<AnsElement> values = new TreeSet<>();

    public AnsVariable(final String name) {
        this.name = name;
    }

    private AnsVariable(final String name, final AnsElement value) {
        this(name);
        this.values.add(value);
    }

    private AnsVariable(final File definer, final String name, final AnsElement value) {
        this(name, value);
        definedIn.add(definer);
    }

    public static void addOrUpdate(TreeMap<String, AnsVariable> vars, File hv, AnsObject.AnsMap entries, TreeMap<AnsString, AnsElement> hvars) {
        for ( Map.Entry<AnsObject.AnsString, AnsElement> e : entries.entrySet()) {
            if (e.getValue().getMap()!=null) {
                for ( Map.Entry<AnsObject.AnsString, AnsElement> e2 : e.getValue().getMap().entrySet()) {
                    String vNam = e.getKey().getString() + "." + e2.getKey().getString();
                    AnsElement vVal = e2.getValue();
                    addOrUpdateVar(vars, vNam, hv, vVal, hvars);
                }
            } else if (e.getValue().getList()!=null) {
                for (AnsElement e2 : e.getValue().getList()) {
                    addOrUpdate(vars, hv, e2.getMap(), hvars);
                }
            } else {
                String vNam = e.getKey().getString();
                AnsElement vVal = e.getValue();
                addOrUpdateVar(vars, vNam, hv, vVal, hvars);
            }
        }

    }

    private static void addOrUpdateVar(TreeMap<String, AnsVariable> vars, String vNam, File hv, AnsElement vVal, TreeMap<AnsString, AnsElement> hvars) {
        AnsVariable vre = vars.get(vNam);
        if (vre == null) {
            vre = new AnsVariable(hv, vNam, vVal);
            vars.put(vNam, vre);
        } else {
            vre.definedIn.add(hv);
            vre.values.add(vVal);
        }
        if (hvars != null) {
            hvars.put(new AnsString(vNam), vVal);
        }
    }

}
