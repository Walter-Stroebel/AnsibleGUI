/*
 * Copyright (c) 2014 by Walter Stroebel and InfComTec.
 * All rights reserved.
 */
package nl.infcomtec.ansible;

import com.esotericsoftware.yamlbeans.YamlException;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import nl.infcomtec.javahtml.JHFragment;
import nl.infcomtec.javahtml.JHParameter;

/**
 *
 * @author walter
 */
public class PlayBook {

    public final ArrayList<String> remoteUser = new ArrayList<>();
    public final File inFile;
    public final ArrayList<String> roles = new ArrayList<>();
    public final ArrayList<String> tasks = new ArrayList<>();
    public final ArrayList<String> hosts = new ArrayList<>();
    public final ArrayList<String> includes = new ArrayList<>();
    public final PlayBooks owner;

    public PlayBook(PlayBooks owner, File f, List<Map> list) throws YamlException {
        this.owner = owner;
        this.inFile = f;
        for (Map map : list) {
            {
                String s = (String) map.remove("remote_user");
                if (s != null) {
                    remoteUser.add(s);
                }
            }
            {
                ArrayList<String> a = (ArrayList<String>) map.remove("roles");
                if (a != null) {
                    roles.addAll(a);
                }
            }
            {
                ArrayList<String> a = (ArrayList<String>) map.remove("tasks");
                if (a != null) {
                    tasks.addAll(a);
                }
            }
            {
                String s = (String) map.remove("hosts");
                if (s != null) {
                    hosts.add(s);
                }
            }
            {
                String s = (String) map.remove("include");
                if (s != null) {
                    includes.add(s);
                }
            }
            if (!map.isEmpty()) {
                System.out.println(map);
                throw new YamlException("Unknown elements found in playbook");
            }
        }
    }

    public void toHtml(HttpServletRequest request, JHFragment top) {
        //System.out.println(request.getParameterMap());
        JHParameter collP = new JHParameter(request, "collapse_" + inFile.getName(), "yes");
        JHParameter collAll = new JHParameter(request, "coll_all_play");
        JHParameter expnAll = new JHParameter(request, "expn_all_play");
        if (collAll.wasSet) {
            collP = JHParameter.overrideWasSet(collP, true);
        }
        if (expnAll.wasSet) {
            collP = JHParameter.overrideWasSet(collP, false);
        }
        top.createCheckBox(collP).appendAttr("onChange", "this.form.submit()").appendText(" ");
        top.createElement("A").appendAttr("id", inFile.getName());
        JHFragment link = top.appendA("EditYml?file=" + inFile.getAbsolutePath(), "_blank", "Playbook -> " + inFile.getAbsolutePath());
        if (collP.wasSet) {
            top.createElement("hr");
        } else {
            top.push("ul");
            if (!remoteUser.isEmpty()) {
                top.push("li");
                top.appendText("Remote user = " + remoteUser);
                top.pop();
            }
            if (!roles.isEmpty()) {
                top.push("li");
                top.push("table").appendAttr("border", "1");
                top.push("tr");
                top.appendTD("Role name");
                top.appendTD("Tasks");
                top.appendTD("Handlers");
                top.appendTD("Files");
                top.appendTD("Templates");
                top.appendTD("Vars");
                top.pop();
                for (String r : roles) {
                    top.push("tr");
                    Role rd = owner.roles.get(r);
                    if (rd != null) {
                        top.appendTD(rd.name);
                        top.push("td");
                        for (Map.Entry<String, RoleFileMap> e : rd.tasks.entrySet()) {
                            top.appendA("EditYml?file=" + e.getValue().file.getAbsolutePath(), "_blank", e.getKey());
                            top.createElement("br");
                        }
                        top.pop().push("td");
                        for (Map.Entry<String, RoleFileMap> e : rd.handlers.entrySet()) {
                            top.appendA("EditYml?file=" + e.getValue().file.getAbsolutePath(), "_blank", e.getKey());
                            top.createElement("br");
                        }
                        top.pop().push("td");
                        for (Map.Entry<String, File> e : rd.files.entrySet()) {
                            if (UnixFile.isItASCII(e.getValue())) {
                                top.appendA("EditAny?file=" + e.getValue().getAbsolutePath(), "_blank", e.getKey());
                            } else {
                                top.appendText(e.getKey());
                            }
                            top.createElement("br");
                        }
                        top.pop().push("td");
                        for (Map.Entry<String, File> e : rd.templates.entrySet()) {
                            if (UnixFile.isItASCII(e.getValue())) {
                                top.appendA("EditAny?file=" + e.getValue().getAbsolutePath(), "_blank", e.getKey());
                            } else {
                                top.appendText(e.getKey());
                            }
                            top.createElement("br");
                        }
                        top.pop();
                        top.push("td");
                        for (Map.Entry<String, RoleFileString> e : rd.vars.entrySet()) {
                            top.appendA("EditYml?file=" + e.getValue().file.getAbsolutePath(), "_blank", e.getKey());
                            top.createElement("br");
                        }
                        top.pop();
                    } else {
                        top.appendTD(r);
                        top.push("td").appendAttr("colspan", "5");
                        top.appendText("Role not found!");
                        top.pop();
                    }
                    top.pop();
                }
                //top.createText("Roles = " + roles);
                top.pop();
                top.pop();
            }
            if (!hosts.isEmpty()) {
                top.appendLI("Hosts = " + hosts);
            }
            if (!includes.isEmpty()) {
                top.push("li");
                top.appendText("Includes");
                String sep = " = ";
                for (String e : includes) {
                    top.appendText(sep);
                    sep = ", ";
                    top.appendA("#" + e, e);
                }
                top.pop();
            }
            if (!tasks.isEmpty()) {
                top.appendLI("Tasks = " + tasks);
            }
            top.pop();
        }
    }

    @Override
    public String toString() {
        return "PlayBook{" + "remoteUser=" + remoteUser + ", inFile=" + inFile + ", roles=" + roles + ", tasks=" + tasks + ", hosts=" + hosts + ", includes=" + includes + '}';
    }

}
