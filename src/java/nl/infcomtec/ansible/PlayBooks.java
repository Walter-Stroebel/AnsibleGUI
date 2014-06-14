/*
 * Copyright (c) 2014 by Walter Stroebel and InfComTec.
 * All rights reserved.
 */
package nl.infcomtec.ansible;

import com.esotericsoftware.yamlbeans.YamlException;
import com.esotericsoftware.yamlbeans.YamlWriter;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspWriter;
import nl.infcomtec.javahtml.JHDocument;
import nl.infcomtec.javahtml.JHFragment;
import nl.infcomtec.javahtml.JHParameter;
import nl.infcomtec.javahtml.Select;

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

    private JHParameter parNewPlayBook;
    private JHParameter parAddPlaybookRoles;
    private JHParameter parNewRole;
    private JHParameter parSubmitPlaybook;

    public PlayBooks(File directory) throws YamlException, FileNotFoundException {
        this.directory = directory;
        dirPath = directory.getAbsolutePath();
        dirPathLen = dirPath.length() + 1;
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
            frag.appendTD(e.name).appendAImg("DeleteRole?name=" + e.name, "_blank", "icons/delete.png");
            frag.push("td");
            boolean first = true;
            for (Map.Entry<String, RoleFileMap> e2 : e.tasks.entrySet()) {
                if (!first) {
                    frag.createElement("br");
                }
                frag.appendA("EditYml?file=" + e2.getValue().file.getAbsolutePath(), "_blank", e2.getKey());
                frag.appendAImg("DeleteTask?file=" + e2.getValue().file.getAbsolutePath() + "&task=" + e2.getKey(), "_blank", "icons/delete.png");
                first = false;
            }
            frag.pop();
            frag.pop();
        }
    }

    public void scan() throws YamlException, FileNotFoundException {
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

    public void writePlayBooks(final HttpServletRequest request, final JspWriter out) throws Exception {
        JHDocument doc = new JHDocument();
        JHFragment top = new JHFragment(doc, "div");
        top.setStyleElement("background-color", "lightyellow");
        top.setStyleElement("color", "black");
        for (PlayBook book : playBooks.values()) {
            book.toHtml(request, top);
        }
        doc.write(out);
    }

    public void writeRoles(final HttpServletRequest request, final JspWriter out) throws Exception {
        JHDocument doc = new JHDocument();
        JHFragment top = new JHFragment(doc, "div");
        top.setStyleElement("background-color", "lightcyan");
        top.setStyleElement("color", "black");
        top.appendP("List of all roles");
        top.push("table").appendAttr("border", "1");
        printRolesTable(top);
        top.pop();
        doc.write(out);
    }

    public void writeVariables(final HttpServletRequest request, final JspWriter out) throws Exception {
        JHDocument doc = new JHDocument();
        JHFragment top = new JHFragment(doc, "div");
        top.setStyleElement("background-color", "lightskyblue");
        top.setStyleElement("color", "black");
        top.appendP("Cross-referenced list of all variables");
        top.push("table").appendAttr("border", "1");
        printVarsTable(top);
        top.pop();
        doc.write(out);
    }

    public void writeRandomFiles(final HttpServletRequest request, final JspWriter out) throws Exception {
        JHDocument doc = new JHDocument();
        JHFragment top = new JHFragment(doc, "div");
        top.setStyleElement("background-color", "lightsalmon");
        top.setStyleElement("color", "black");
        top.appendP("Other (random) files and/or directories");
        top.push("table").appendAttr("border", "1");
        for (File f : randomFiles) {
            if (f.isDirectory()) {
                top.createElement("tr").appendTD(f.getAbsolutePath() + " (dir)");
            } else {
                top.createElement("tr").createElement("td").appendA("EditAny?file=" + f.getAbsolutePath(), "_blank", shortFileName(f));
            }
        }
        top.pop();
        doc.write(out);
    }

    public void processNewPlayBookForm(final HttpServletRequest request, final JspWriter out) throws Exception {
        parNewPlayBook = new JHParameter(request, "newPlayBook", "");
        parAddPlaybookRoles = new JHParameter(request, "addPlaybookRoles", "");
        parNewRole = new JHParameter(request, "newRole", "");
        parSubmitPlaybook = new JHParameter(request, "submitPlaybook", "Go!");
        if (parSubmitPlaybook.wasSet && parNewPlayBook.notEmpty()) {
            String newPB = parNewPlayBook.getValue().replace(' ', '_');
            if (!newPB.endsWith(".yml")) {
                newPB = newPB + ".yml";
            }
            File newFile = new File(directory, newPB);
            if (!newFile.exists()) {
                String newRole = parNewRole.getValue().trim().replace(' ', '_');
                ArrayList<String> rolez = new ArrayList<>();
                if (parAddPlaybookRoles.values != null) {
                    for (String rz : parAddPlaybookRoles.values) {
                        if (!rz.trim().isEmpty()) {
                            rolez.add(rz);
                        }
                    }
                }
                if (!newRole.isEmpty()) {
                    rolez.add(newRole);
                    File rdir = new File(directory, "roles/" + newRole + "/tasks");
                    rdir.mkdirs();
                    File dontOverwrite = new File(rdir, "main.yml");
                    if (!dontOverwrite.exists()) {
                        try (FileWriter fw = new FileWriter(dontOverwrite)) {
                            fw.write("- name: ");
                            fw.write(newRole);
                            fw.write("_task\n");
                        }
                    }
                }
                FileWriter writer = new FileWriter(newFile);
                YamlWriter yw = new YamlWriter(writer);
                Map rm = new HashMap();
                rm.put("roles", rolez);
                yw.write(Collections.singletonList(rm));
                yw.close();
            }
            parNewPlayBook = parNewPlayBook.clear();
            parAddPlaybookRoles = parAddPlaybookRoles.clear();
            parNewRole = parNewRole.clear();
        }
    }

    public void writeNewPlayBookForm(final HttpServletRequest request, final JspWriter out) throws Exception {
        JHDocument doc = new JHDocument();
        JHFragment top = new JHFragment(doc, "table");
        top.appendAttr("border", "1");
        top.push("tr");
        top.createElement("th").appendAttr("colspan", "2").appendText("Create a new playbook.");
        top.pop();
        top.push("tr");
        top.appendTD("Enter a name for the playbook:");
        top.push("td");
        top.createInput("text", parNewPlayBook);
        top.pop("tr");
        top.push("tr");
        top.appendTD("Optionally add some roles:");
        top.push("td");
        String rolesSize = "4";
        if (!roles.isEmpty()) {
            rolesSize = "" + Math.max(roles.size(), 10);
        }
        Select sel = top.createSelect(parAddPlaybookRoles);
        sel.appendAttr("multiple").appendAttr("size", rolesSize);
        for (String rnam : roles.keySet()) {
            sel.addOption(rnam, rnam);
        }
        top.pop("tr");
        top.push("tr");
        top.appendTD("And/or create a new role:");
        top.push("td");
        top.createInput("text", parNewRole);
        top.pop("tr");
        top.push("tr").appendAttr("colspan", "2");
        top.push("td");
        top.createInput("submit", parSubmitPlaybook);
        top.pop("table");
        doc.write(out);
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
