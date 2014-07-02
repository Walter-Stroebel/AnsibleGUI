/*
 * Copyright (c) 2014 by Walter Stroebel and InfComTec.
 * All rights reserved.
 */
package nl.infcomtec.ansible;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 *
 * @author walter
 */
public class AnsInventory {

    public final TreeMap<String, TreeMap<AnsString, AnsElement>> vars = new TreeMap<>();
    public final TreeMap<String, TreeSet<String>> groups = new TreeMap<>();
    public final TreeMap<String, TreeSet<String>> hosts = new TreeMap<>();
    public final File file;
/**
 * Build the inventory.
 * @param pbDir Playbook (master) directory.
 * @param file Inventory file.
 * @throws IOException If it does.
 */
    public AnsInventory(File pbDir, File file) throws IOException {
        this.file = file;
        File groupVars = new File(pbDir, "group_vars");
        if (!groupVars.exists()) {
            groupVars.mkdir();
        }
        File hostVars = new File(pbDir, "host_vars");
        if (!hostVars.exists()) {
            hostVars.mkdir();
        }
        try (BufferedReader bfr = new BufferedReader(new FileReader(file))) {
            String group = "all";
            for (String _s = bfr.readLine(); _s != null; _s = bfr.readLine()) {
                String s = _s.trim();
                if (s.isEmpty() || s.startsWith("#") || s.startsWith(";")) {
                    continue;
                }
                if (s.startsWith("[")) {
                    if (s.contains(":vars")) {
                        group = s.substring(1, s.indexOf(':'));
                        File gf = new File(groupVars, group);
                        AnsMap map = new AnsMap();
                        if (gf.exists()) {
                            try {
                                AnsObject ao = new AnsObject(null, gf);
                                if (ao.getMap() != null) {
                                    map.putAll(ao.getMap());
                                }
                            } catch (Exception any) {
                            }
                        }
                        TreeMap<AnsString, AnsElement> gvars = vars.get("g_" + group);
                        if (gvars == null) {
                            gvars = new TreeMap<>();
                        }
                        for ( Map.Entry<AnsString, AnsElement> e : map.entrySet()) {
                            gvars.put(e.getKey(), e.getValue());
                        }
                        bfr.mark(1000);
                        for (_s = bfr.readLine(); _s != null; _s = bfr.readLine()) {
                            s = _s.trim();
                            if (s.isEmpty() || s.startsWith("#") || s.startsWith(";")) {
                                continue;
                            }
                            if (s.startsWith("[")) {
                                break;
                            }
                            bfr.mark(1000);
                            String[] vd = s.split("=");
                            gvars.put(new AnsString(vd[0]), new AnsString(vd[1]));
                        }
                        bfr.reset();
                        map.clear();
                        for (Map.Entry<AnsString, AnsElement> e : gvars.entrySet()) {
                            map.put(e.getKey(), e.getValue());
                        }
//                        try (PrintWriter pw = new PrintWriter(gf)) {
//                            pw.print(AnsObject.makeString(map));
//                        }
                    } else {
                        group = s.substring(1, s.length() - 1);
                    }
                } else {
                    StringTokenizer toker = new StringTokenizer(s, " \t:", true);
                    String host = toker.nextToken();
                    TreeSet<String> l = groups.get(group);
                    if (l == null) {
                        groups.put(group, l = new TreeSet<>());
                    }
                    l.add(host);
                    l = groups.get("all");
                    if (l == null) {
                        groups.put("all", l = new TreeSet<>());
                    }
                    l.add(host);
                    l = hosts.get(host);
                    if (l == null) {
                        hosts.put(host, l = new TreeSet<>());
                    }
                    l.add(group);
                    l.add("all");
                    File hf = new File(hostVars, host);
                    AnsMap map = new AnsMap();
                    if (hf.exists()) {
                        try {
                            AnsObject ao = new AnsObject(null, hf);
                            if (ao.getMap() != null) {
                                map.putAll(ao.getMap());
                            }
                        } catch (Exception any) {
                        }
                    }
                    TreeMap<AnsString, AnsElement> hvars = vars.get("h_" + host);
                    if (hvars == null) {
                        hvars = new TreeMap<>();
                    }
                    for (Map.Entry<AnsString, AnsElement> e : map.entrySet()) {
                        hvars.put(e.getKey(), e.getValue());
                    }
                    if (toker.hasMoreTokens()) {
                        String tok = toker.nextToken();
                        if (tok.equals(":") && toker.hasMoreTokens()) {
                            hvars.put(new AnsString("ansible_ssh_port"), new AnsString(toker.nextToken()));
                            if (toker.hasMoreTokens()) {
                                tok = toker.nextToken();
                            } else {
                                tok = "";
                            }
                        }
                        do {
                            tok = tok.trim();
                            if (!tok.isEmpty()) {
                                String[] vd = tok.split("=");
                                if (vd.length == 2) {
                                    hvars.put(new AnsString(vd[0]), new AnsString(vd[1]));
                                } else {
                                    break;
                                }
                            }
                            if (toker.hasMoreTokens()) {
                                tok = toker.nextToken();
                            } else {
                                tok = "";
                            }
                        } while (!tok.isEmpty() || toker.hasMoreTokens());
                        map.clear();
                        for (Map.Entry<AnsString, AnsElement> e : hvars.entrySet()) {
                            map.put(e.getKey(), e.getValue());
                        }
//                        try (PrintWriter pw = new PrintWriter(hf)) {
//                            pw.print(AnsObject.makeString(map));
//                        }
                    }
                }
            }
        }
    }
}
