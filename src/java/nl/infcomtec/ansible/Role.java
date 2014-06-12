/*
 * Copyright (c) 2014 by Walter Stroebel and InfComTec.
 * All rights reserved.
 */
package nl.infcomtec.ansible;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import javax.servlet.http.HttpServletRequest;
import nl.infcomtec.javahtml.JHFragment;

/**
 *
 * @author walter
 */
public class Role {
    public File taskFile;
    public final String name;
    public final TreeMap<String, RoleFileMap> tasks = new TreeMap<>();
    public final TreeMap<String, RoleFileMap> handlers = new TreeMap<>();
    public final TreeMap<String, File> files = new TreeMap<>();    
    public final TreeMap<String, File> templates = new TreeMap<>();
    public final TreeMap<String, RoleFileString> vars = new TreeMap<>();

    public Role(String name) {
        this.name = name;
    }

    public void toHtml(HttpServletRequest request, JHFragment top) {
        top.push("dl");
        top.createElement("dt").appendText("Role -> " + name);
        top.push("dd");
        if (!tasks.isEmpty()) {
            top.appendP("Tasks: " + tasks.keySet());
        }
        if (!handlers.isEmpty()) {
            top.appendP("Handlers: " + handlers.keySet());
        }
        if (!files.isEmpty()) {
            top.appendP("Files: " + files.keySet());
        }
        if (!templates.isEmpty()) {
            top.appendP("Files: " + templates.keySet());
        }
        if (!vars.isEmpty()) {
            top.appendP("Vars: " + vars.keySet());
        }
        top.pop();
        top.pop();
    }

    @Override
    public String toString() {
        return "Role{" + "name=" + name + ", tasks=" + tasks.size() + ", handlers=" + handlers.size() + ", files=" + files.size() + ", templates=" + templates.size() + ", vars=" + vars.size() + '}';
    }

}
