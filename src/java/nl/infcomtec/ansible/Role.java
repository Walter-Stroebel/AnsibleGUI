/*
 * Copyright (c) 2014 by Walter Stroebel and InfComTec.
 * All rights reserved.
 */
package nl.infcomtec.ansible;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

/**
 *
 * @author walter
 */
public class Role {

    public final String name;
    public final ArrayList<Task> tasks = new ArrayList<>();
    public final TreeMap<String, RoleFileMap> handlers = new TreeMap<>();
    public final TreeMap<String, File> files = new TreeMap<>();
    public final TreeMap<String, File> templates = new TreeMap<>();
    public final TreeMap<String, PlayBooks.Variable> vars = new TreeMap<>();
    public AnsObject meta;

    public Role(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Role{" + "name=" + name + ", tasks=" + tasks.size() + ", handlers=" + handlers.size() + ", files=" + files.size() + ", templates=" + templates.size() + ", vars=" + vars.size() + '}';
    }

    public List<String> getTaskNames() {
        ArrayList<String> ret = new ArrayList<>();
        for (Task e:tasks){
            ret.add(e.name);
        }
        return ret;
    }

}
