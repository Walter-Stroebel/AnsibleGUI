/*
 * Copyright (c) 2014 by Walter Stroebel and InfComTec.
 * All rights reserved.
 */
package nl.infcomtec.ansible;

import com.esotericsoftware.yamlbeans.YamlException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;
import nl.infcomtec.javahtml.JHFragment;

/**
 *
 * @author walter
 */
public class PlayBooks {

    public final TreeMap<String, PlayBook> playBooks = new TreeMap<>();
    public final TreeMap<String, Role> roles = new TreeMap<>();
    public final LinkedList<File> randomFiles = new LinkedList<>();
    public final File directory;
    public final TreeMap<String, Variable> vars = new TreeMap<>();

    public PlayBooks(File directory) throws YamlException, FileNotFoundException {
        this.directory = directory;
        scan(directory);
    }

    public void toHtml(JHFragment top, TreeMap<String, Variable> vars) {
        top.push("tr");
        top.appendTD("Var");
        top.appendTD("Value(s)");
        top.appendTD("Defined in");
        top.appendTD("Used by");
        top.pop();
        for (Variable e : vars.values()) {
            top.push("tr");
            top.appendTD(e.name);
            top.appendTD(e.values.toString());
            top.push("td");
            for (File f : e.definedIn) {
                top.appendA("EditYml?file=" + f.getAbsolutePath(), "_blank", f.getAbsolutePath().substring(directory.getAbsolutePath().length() + 1));
                top.createElement("br");
            }
            top.pop();
            top.push("td");
            for (File f : e.usedBy) {
                top.appendA("EditYml?file=" + f.getAbsolutePath(), "_blank", f.getAbsolutePath().substring(directory.getAbsolutePath().length() + 1));
                top.createElement("br");
            }
            top.pop();
            top.pop();
        }
    }

    private void scan(File directory) throws YamlException, FileNotFoundException {
        for (File f : directory.listFiles()) {
            if (f.getName().startsWith(".")) {
                continue;
            }
            if (f.isDirectory()) {
                if ("roles".equals(f.getName())) {
                    for (File rf : f.listFiles()) {
                        if (rf.isDirectory()) {
                            roles(rf);
                        } else {
                            randomFiles.add(rf);
                        }
                    }
                } else if ("host_vars".equals(f.getName())) {
                    for (File hv : f.listFiles()) {
                        if (hv.isDirectory()) {
                            randomFiles.add(hv);
                        }
                        AnsObject ao = new AnsObject(hv, new FileReader(hv));
                        for (Map.Entry<Object, Object> e : ao.getMap().entrySet()) {
                            Variable vre = vars.get(e.getKey().toString());
                            if (vre == null) {
                                vre = new Variable(hv, e.getKey().toString(), e.getValue().toString());
                                vars.put(e.getKey().toString(), vre);
                            } else {
                                vre.definedIn.add(hv);
                                vre.values.add(e.getValue().toString());
                            }
                        }
                    }
                } else if ("group_vars".equals(f.getName())) {
                    for (File hv : f.listFiles()) {
                        if (hv.isDirectory()) {
                            randomFiles.add(hv);
                        }
                        AnsObject ao = new AnsObject(hv, new FileReader(hv));
                        for (Map.Entry<Object, Object> e : ao.getMap().entrySet()) {
                            Variable vre = vars.get(e.getKey().toString());
                            if (vre == null) {
                                vre = new Variable(hv, e.getKey().toString(), e.getValue().toString());
                                vars.put(e.getKey().toString(), vre);
                            } else {
                                vre.definedIn.add(hv);
                                vre.values.add(e.getValue().toString());
                            }
                        }
                    }
                } else {
                    for (File rd : f.listFiles()) {
                        randomFiles.add(rd);
                    }
                }
            } else if (f.getName().endsWith(".yml")) {
                AnsObject obj;
                try {
                    obj = new AnsObject(f, new FileReader(f));
                } catch (YamlException ex) {
                    obj = new AnsObject(f, "- Error: \"" + ex.getMessage() + "\"");
                }
                ArrayList<Map> list = (ArrayList<Map>) obj.object;
                PlayBook pb = new PlayBook(this, f, list);
                playBooks.put(f.getName().substring(0, f.getName().length() - 4), pb);
            } else {
                randomFiles.add(f);
            }
        }
    }

    private void roles(File roleDir) throws YamlException, FileNotFoundException {
        Role role = roles.get(roleDir.getName());
        if (role == null) {
            role = new Role(roleDir.getName());
            roles.put(roleDir.getName(), role);
        }
        for (File rd : roleDir.listFiles()) {
            if (rd.isDirectory()) {
                switch (rd.getName()) {
                    case "tasks":
                        for (File aTask : rd.listFiles()) {
                            if (aTask.getName().endsWith(".yml")) {
                                AnsObject task = new AnsObject(aTask, new FileReader(aTask));
                                List<Map> tasks = (List<Map>) task.object;
                                for (Map t : tasks) {
                                    String tnam = (String) t.get("name");
                                    if (tnam == null) {
                                        tnam = UUID.randomUUID().toString();
                                    }
                                    role.tasks.put(tnam, new RoleFileMap(aTask, t));
                                }
                            } else {
                                randomFiles.add(aTask);
                            }
                        }
                        break;
                    case "handlers":
                        for (File aHandler : rd.listFiles()) {
                            if (aHandler.getName().endsWith(".yml")) {
                                AnsObject hand = new AnsObject(aHandler, new FileReader(aHandler));
                                List<Map> hands = (List<Map>) hand.object;
                                for (Map t : hands) {
                                    role.handlers.put((String) t.get("name"), new RoleFileMap(aHandler, t));
                                }
                            } else {
                                randomFiles.add(aHandler);
                            }
                        }
                        break;
                    case "files":
                        for (File aFile : rd.listFiles()) {
                            role.files.put(aFile.getName(), aFile);
                        }
                        break;
                    case "templates":
                        for (File aFile : rd.listFiles()) {
                            role.templates.put(aFile.getName(), aFile);
                        }
                        break;
                    case "vars":
                        for (File ts : rd.listFiles()) {
                            if (ts.getName().endsWith(".yml")) {
                                AnsObject var = new AnsObject(ts, new FileReader(ts));
                                for (Map.Entry<Object, Object> e : var.getMap().entrySet()) {
                                    Variable vre = vars.get(e.getKey().toString());
                                    if (vre == null) {
                                        vre = new Variable(ts, e.getKey().toString(), e.getValue().toString());
                                    } else {
                                        vre.definedIn.add(ts);
                                        vre.values.add(e.getValue().toString());
                                    }
                                    role.vars.put(e.getKey().toString(), vre);
                                }
                            } else {
                                randomFiles.add(ts);
                            }
                        }
                        break;
                    default:
                        randomFiles.add(rd);
                        break;
                }
            } else {
                randomFiles.add(rd);
            }
        }
    }

    public static class Variable {

        public final TreeSet<File> definedIn = new TreeSet<>();
        public final TreeSet<File> usedBy = new TreeSet<>();
        public final String name;
        public final TreeSet<String> values = new TreeSet<>();

        public Variable(String name, String value) {
            this.name = name;
            this.values.add(value);
        }

        public Variable(File definer, String name, String value) {
            definedIn.add(definer);
            this.name = name;
            this.values.add(value);
        }
    }

}
