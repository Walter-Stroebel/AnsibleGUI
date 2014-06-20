/*
 * Copyright (c) 2014 by Walter Stroebel and InfComTec.
 * All rights reserved.
 */
package nl.infcomtec.ansible;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.TreeSet;
import java.util.StringTokenizer;
import java.util.TreeMap;

/**
 *
 * @author walter
 */
public class AnsInventory {

    public final TreeMap<String, TreeSet<String>> groups = new TreeMap<>();
    public final TreeMap<String, TreeSet<String>> hosts = new TreeMap<>();
    public final File file;

    public AnsInventory(PlayBooks books, File file) throws IOException {
        this.file = file;
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
                            PlayBooks.Variable v = books.vars.get(vd[0]);
                            if (v == null) {
                                v = new PlayBooks.Variable(file, vd[0], vd[1]);
                                books.vars.put(vd[0], v);
                            } else {
                                v.definedIn.add(file);
                                v.values.add(vd[1]);
                            }
                        }
                        bfr.reset();
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
                        groups.put(group, l = new TreeSet<>());
                    }
                    l.add(host);
                    l = hosts.get(host);
                    if (l == null) {
                        hosts.put(host, l = new TreeSet<>());
                    }
                    l.add(group);
                    if (toker.hasMoreTokens()) {
                        String tok = toker.nextToken();
                        if (tok.equals(":") && toker.hasMoreTokens()) {
                            PlayBooks.Variable v = books.vars.get("ansible_ssh_port");
                            if (v == null) {
                                v = new PlayBooks.Variable(file, "ansible_ssh_port", toker.nextToken());
                                books.vars.put("ansible_ssh_port", v);
                            } else {
                                v.definedIn.add(file);
                                v.values.add(toker.nextToken());
                            }
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
                                if (vd.length==2){
                                    PlayBooks.Variable v = books.vars.get(vd[0]);
                                    if (v == null) {
                                        v = new PlayBooks.Variable(file, vd[0], vd[1]);
                                        books.vars.put(vd[0], v);
                                    } else {
                                        v.definedIn.add(file);
                                        v.values.add(vd[1]);
                                    }
                                }else break;
                            }
                            if (toker.hasMoreTokens()) {
                                tok = toker.nextToken();
                            }else {
                                tok="";
                            }
                        } while (!tok.isEmpty()||toker.hasMoreTokens());
                    }
                }
            }
        }
    }
}
