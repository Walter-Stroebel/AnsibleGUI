/*
 * Copyright (c) 2014 by Walter Stroebel and InfComTec.
 * All rights reserved.
 */
package nl.infcomtec.ansible;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
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
    public final AnsMap others=new AnsMap();
    public final PlayBooks owner;
    private final String parName;
    private final String desc;

    public PlayBook(PlayBooks owner, File f, AnsList list) throws IOException {
        this.owner = owner;
        this.inFile = f;
        this.parName = "desc_" + owner.makeId(f);
        this.desc = owner.getDoc(f, parName);
        for (AnsElement elm : list) {
            AnsMap map = elm.getMap();
            if (map != null) {
                AnsElement s = map.remove("remote_user");
                if (s != null && s.getString() != null) {
                    remoteUser.add(s.getString());
                }
            } else {
                System.err.println("Why is 'remote_user' a " + elm + "?");
                return;
            }
            {
                AnsElement _rls = map.remove("roles");
                AnsList rls = _rls != null ? _rls.getList() : null;
                if (rls != null) {
                    ArrayList<String> l = new ArrayList<>();
                    for (AnsElement o : rls) {
                        if (o.getString() != null) {
                            l.add(o.getString());
                        } else if (o.getMap() != null) {
                            AnsMap rmap = o.getMap();
                            AnsElement rname = rmap.remove("role");
                            if (rname.getString() != null) {
                                l.add(rname.getString());
                                Role parRole = owner.roles.get(rname.getString());
                                if (parRole == null) {
                                    parRole = new Role(rname.getString());
                                }
                                AnsVariable.addOrUpdate(parRole.vars, f, rmap, null);
                                owner.roles.put(rname.getString(), parRole);
                            }
                        }// else ignore it, a list here?
                    }
                    roles.addAll(l);
                }
            }
            {
                AnsElement a = map.remove("tasks");
                if (a != null && a.getList() != null) {
                    for (AnsElement e : a.getList()) {
                        if (e.getString() != null) {
                            tasks.add(e.getString());
                        }
                    }
                }
            }
            others.putAll(map);
        }
    }

    public void toHtml(HttpServletRequest request, JHFragment top) throws IOException {
        JHParameter expandP = new JHParameter(request, "expand_" + inFile.getName(), "yes");
        JHParameter expandPH = new JHParameter(request.getSession(), request, "expandh_" + inFile.getName(), "yes");
        JHParameter collAll = new JHParameter(request, "coll_all_play");
        JHParameter expnAll = new JHParameter(request, "expn_all_play");
        if (collAll.wasSet) {
            expandP = JHParameter.overrideWasSet(expandP, false);
            expandPH.setValue(0);
            expandPH.setInSession(request.getSession());
        }
        if (expnAll.wasSet) {
            expandP = JHParameter.overrideWasSet(expandP, true);
            expandPH.setValue(1);
            expandPH.setInSession(request.getSession());
        }
        if (!expandP.wasSet && expandPH.getIntValue() == 0) {
            expandPH.setValue(0);
            expandPH.setInSession(request.getSession());
            top.createInput("hidden", expandPH);
        } else {
            expandPH.setValue(1);
            expandPH.setInSession(request.getSession());
            expandP = JHParameter.overrideWasSet(expandP, true);
            top.createInput("hidden", expandPH);
        }
        top.createCheckBox(expandP).appendAttr("onChange", "this.form.submit()").appendAttr("id", expandP.varName);
        if (!expandP.wasSet) {
            top.push("label").appendAttr("for", expandP.varName);
            top.appendText(" ");
            top.appendIMG("icons/application_side_expand.png");
            top.pop();
            top.appendText(" ");
        } else {
            top.push("label").appendAttr("for", expandP.varName);
            top.appendText(" ");
            top.appendIMG("icons/application_side_contract.png");
            top.pop();
            top.appendText(" ");
        }
        top.createElement("A").appendAttr("id", inFile.getName());
        JHFragment link = top.appendA("EditYml?file=" + inFile.getAbsolutePath(), "Playbook -> " + owner.shortFileName(inFile));
        top.appendAImg("DeletePlayBook?file=" + inFile.getAbsolutePath(), "icons/delete.png");
        if (!expandP.wasSet) {
            top.createElement("hr");
        } else {
            top.push("p");
            JHFragment area = top.createElement("textarea");
            area.appendAttr("name", parName).setStyleElement("background-color", "white");
            area.appendAttr("rows", "10");
            area.appendAttr("cols", "100");
            area.appendText(desc);
            top.createElement("input").appendAttr("type", "submit").appendAttr("value", "Update description").removeStyleElement("background-color");
            top.pop();
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
                        for (Task e : rd.tasks) {
                            top.appendA("EditYml?file=" + e.file.getAbsolutePath(), e.name);
                            top.createElement("br");
                        }
                        top.pop().push("td");
                        for (Map.Entry<String, RoleFileMap> e : rd.handlers.entrySet()) {
                            top.appendA("EditYml?file=" + e.getValue().file.getAbsolutePath(), e.getKey());
                            top.createElement("br");
                        }
                        top.pop().push("td");
                        for (Map.Entry<String, File> e : rd.files.entrySet()) {
                            if (UnixFile.isItASCII(e.getValue())) {
                                top.appendA("EditAny?file=" + e.getValue().getAbsolutePath(), e.getKey());
                            } else {
                                top.appendText(e.getKey());
                            }
                            top.createElement("br");
                        }
                        top.pop().push("td");
                        for (Map.Entry<String, File> e : rd.templates.entrySet()) {
                            if (UnixFile.isItASCII(e.getValue())) {
                                top.appendA("EditAny?file=" + e.getValue().getAbsolutePath(), e.getKey());
                            } else {
                                top.appendText(e.getKey());
                            }
                            top.createElement("br");
                        }
                        top.pop();
                        top.push("td");
                        for (String vn : rd.vars.keySet()) {
                            top.appendP(vn);
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
                top.pop();
                top.pop();
            }
            AnsObject.toHtml(top, others);
            if (!tasks.isEmpty()) {
                top.appendLI("Tasks = " + tasks);
            }
            top.pop();
        }
    }
}
