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
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;

/**
 *
 * @author walter
 */
public class PlayBooks {

    public final TreeMap<String, PlayBook> playBooks = new TreeMap<>();
    public final TreeMap<String, Role> roles = new TreeMap<>();
    public final LinkedList<File> randomFiles = new LinkedList<>();
    public final File directory;

    public PlayBooks(File directory) throws YamlException, FileNotFoundException {
        this.directory = directory;
        scan(directory);
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
                                for (Map.Entry<?, ?> e : (Set<Map.Entry<?, ?>>) var.getMap().entrySet()) {
                                    role.vars.put(e.getKey().toString(), new RoleFileString(ts, e.getValue().toString()));
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

}
