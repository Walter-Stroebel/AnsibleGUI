/*
 * Copyright (c) 2014 by Walter Stroebel and InfComTec.
 * All rights reserved.
 */
package nl.infcomtec.ansible;

import com.esotericsoftware.yamlbeans.YamlException;
import java.io.BufferedReader;
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
    private final String dirPath;
    public final TreeMap<String, Variable> vars = new TreeMap<>();
    private final int dirPathLen;

    public PlayBooks(File directory) throws YamlException, FileNotFoundException {
        this.directory = directory;
        dirPath = directory.getAbsolutePath();
        dirPathLen = dirPath.length() + 1;
        scan(directory);
    }

    /**
     * Strips the working directory part from a file name.
     *
     * @param f File to shorten the name of.
     * @return The short name or the full name if not in the working directory.
     */
    public String shortFileName(File f) {
        String longName = f.getAbsolutePath();
        if (longName.startsWith(dirPath)) {
            return longName.substring(dirPathLen);
        }
        return longName;
    }

    public void printVarsTable(JHFragment frag) {
        frag.push("tr");
        frag.appendTD("Var");
        frag.appendTD("Value(s)");
        frag.appendTD("Defined in");
        frag.appendTD("Used by");
        frag.pop();
        for (Variable e : vars.values()) {
            frag.push("tr");
            frag.appendTD(e.name);
            frag.appendTD(e.values.toString());
            frag.push("td");
            for (File f : e.definedIn) {
                frag.appendA("EditYml?file=" + f.getAbsolutePath(), "_blank", shortFileName(f));
                frag.createElement("br");
            }
            frag.pop();
            frag.push("td");
            for (File f : e.usedBy) {
                frag.appendA("EditYml?file=" + f.getAbsolutePath(), "_blank", shortFileName(f));
                frag.createElement("br");
            }
            frag.pop();
            frag.pop();
        }
    }

    public void printRolesTable(JHFragment frag) {
        frag.push("tr");
        frag.appendTD("Role");
        frag.appendTD("Task(s)");
        frag.pop();
        for (Role e : roles.values()) {
            frag.push("tr");
            frag.appendTD(e.name);
            frag.push("td");
            boolean first = true;
            for (Map.Entry<String, RoleFileMap> e2 : e.tasks.entrySet()) {
                if (!first) {
                    frag.createElement("br");
                }
                frag.appendA("EditYml?file=" + e2.getValue().file.getAbsolutePath(), "_blank", e2.getKey());
                first = false;
            }
            frag.pop();
            frag.pop();
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
                        if (rf.getName().startsWith(".")) {
                            continue;
                        }
                        if (rf.isDirectory()) {
                            roles(rf);
                        } else {
                            randomFiles.add(rf);
                        }
                    }
                } else if ("host_vars".equals(f.getName())) {
                    for (File hv : f.listFiles()) {
                        if (hv.getName().startsWith(".")) {
                            continue;
                        }
                        if (hv.isDirectory()) {
                            randomFiles.add(hv);
                        }
                        AnsObject ao = new AnsObject(this, hv, new FileReader(hv));
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
                        if (hv.getName().startsWith(".")) {
                            continue;
                        }
                        if (hv.isDirectory()) {
                            randomFiles.add(hv);
                        }
                        AnsObject ao = new AnsObject(this, hv, new FileReader(hv));
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
                        if (rd.getName().startsWith(".")) {
                            continue;
                        }
                        randomFiles.add(rd);
                    }
                }
            } else if (f.getName().endsWith(".yml")) {
                AnsObject obj;
                try {
                    obj = new AnsObject(this, f, new FileReader(f));
                    ArrayList<Map> list = (ArrayList<Map>) obj.object;
                    if (list == null) {
                        list = new ArrayList<>();
                    }
                    PlayBook pb = new PlayBook(this, f, list);
                    playBooks.put(f.getName().substring(0, f.getName().length() - 4), pb);
                } catch (Exception any) {
                    System.err.println("Bad playbook: " + f);
                }
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
            if (rd.getName().startsWith(".")) {
                continue;
            }
            if (rd.isDirectory()) {
                switch (rd.getName()) {
                    case "tasks":
                        for (File aTask : rd.listFiles()) {
                            if (aTask.getName().startsWith(".")) {
                                continue;
                            }
                            if (aTask.getName().endsWith(".yml")) {
                                AnsObject task = new AnsObject(this, aTask, new FileReader(aTask));
                                List<Map> tasks = (List<Map>) task.object;
                                for (Map t : tasks) {
                                    String tnam = (String) t.get("name");
                                    if (tnam == null) {
                                        tnam = (String) t.get("when");
                                    }
                                    if (tnam == null) {
                                        tnam = "??? " + UUID.randomUUID().toString();
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
                            if (aHandler.getName().startsWith(".")) {
                                continue;
                            }
                            if (aHandler.getName().endsWith(".yml")) {
                                AnsObject hand = new AnsObject(this, aHandler, new FileReader(aHandler));
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
                            if (aFile.getName().startsWith(".")) {
                                continue;
                            }
                            role.files.put(aFile.getName(), aFile);
                        }
                        break;
                    case "templates":
                        for (File aFile : rd.listFiles()) {
                            if (aFile.getName().startsWith(".")) {
                                continue;
                            }
                            role.templates.put(aFile.getName(), aFile);
                            if (aFile.length() < 64000) { // sanity test
                                try (BufferedReader bfr = new BufferedReader(new FileReader(aFile))) {
                                    for (String s = bfr.readLine(); s != null; s = bfr.readLine()) {
                                        scanStringForVars(s, aFile);
                                    }
                                } catch (Exception any) {
                                    // we tried
                                }
                            }
                        }
                        break;
                    case "vars":
                        for (File ts : rd.listFiles()) {
                            if (ts.getName().startsWith(".")) {
                                continue;
                            }
                            if (ts.getName().endsWith(".yml")) {
                                AnsObject var = new AnsObject(this, ts, new FileReader(ts));
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

    public void scanForVars(final File inFile, final Object object) {
        if (object instanceof String) {
            String s = (String) object;
            scanStringForVars(s, inFile);
        } else if (object instanceof List) {
            List l = (List) object;
            for (Object o : l) {
                scanForVars(inFile, o);
            }
        } else if (object instanceof Map) {
            Map<Object, Object> m = AnsObject.getMap(object);
            for (Map.Entry<Object, Object> e : m.entrySet()) {
                scanForVars(inFile, e.getKey());
                scanForVars(inFile, e.getValue());
            }
        }
    }

    private void scanStringForVars(final String s, final File inFile) {
        int idx = s.indexOf("{{");
        while (idx >= 0) {
            int end = s.indexOf("}}", idx);
            String varName = s.substring(idx + 2, end).trim();
            Variable var = vars.get(varName);
            if (var == null) {
                var = new Variable(varName);
                vars.put(varName, var);
            }
            var.usedBy.add(inFile);
            idx = s.indexOf(s, end);
        }
    }

    public static class Variable {

        public final TreeSet<File> definedIn = new TreeSet<>();
        public final TreeSet<File> usedBy = new TreeSet<>();
        public final String name;
        public final TreeSet<String> values = new TreeSet<>();

        public Variable(final String name) {
            this.name = name;
        }

        public Variable(final String name, final String value) {
            this(name);
            this.values.add(value);
        }

        public Variable(final File definer, final String name, final String value) {
            this(name, value);
            definedIn.add(definer);
        }
    }

}
