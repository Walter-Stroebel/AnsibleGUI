<%-- 
    Document   : index
    Created on : Jun 11, 2014, 6:43:30 AM
    Author     : walter
--%>
<%@page import="java.io.File"%>
<%@page import="java.util.Collections"%>
<%@page import="java.util.HashMap"%>
<%@page import="java.util.Map"%>
<%@page import="com.esotericsoftware.yamlbeans.YamlWriter"%>
<%@page import="java.io.FileWriter"%>
<%@page import="java.util.ArrayList"%>
<%@page import="nl.infcomtec.ansible.PlayBook"%>
<%@page import="nl.infcomtec.ansible.PlayBooks"%>
<%@page import="java.util.concurrent.atomic.AtomicInteger"%>
<%@page import="java.util.LinkedList"%>
<%@page import="nl.infcomtec.ansible.AnsObject"%>
<%@page import="nl.infcomtec.javahtml.JHFragment"%>
<%@page import="nl.infcomtec.javahtml.JHDocument"%>
<%@page import="nl.infcomtec.javahtml.JHFragment"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%
    String ansPath = request.getParameter("anspath") != null ? request.getParameter("anspath").trim() : "";
    boolean hasGIT = false;
    if (ansPath.isEmpty()) {
        if (session.getAttribute("anspath") != null) {
            ansPath = session.getAttribute("anspath").toString();
        }
    } else {
        session.setAttribute("anspath", ansPath);
    }
    if (!ansPath.isEmpty()) {
        File git = new File(ansPath, ".git");
        hasGIT = git.exists() && git.isDirectory();
    }
    if (hasGIT && request.getParameter("GIT") != null) {
        response.sendRedirect("MiniGIT");
    }
%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <style>
            .anchor {
                color: darkblue;
                text-decoration: underline;
            }
        </style>
        <title>AnsibleGUI</title>
    </head>
    <body style="background-color: darkslateblue; color: white;">
        <form action="index.jsp" method="POST">
            <p>Path:
                <input type="text" value="<%=ansPath%>" placeholder="Full path to your ansible files" size="50" name="anspath" />
                <input type="submit" value="Refresh" />
                <% if (hasGIT) { %>
                <input type="submit" name="GIT" value="Git..." />
                <% } %>
                <input type="submit" name="coll_all_play" value="Collapse all playbooks" />
                <input type="submit" name="expn_all_play" value="Expand all playbooks" />
            </p>
            <%
                if (!ansPath.isEmpty()) {
                    if (request.getParameter("creplaybook") != null
                            && !request.getParameter("newplaybook").trim().isEmpty()) {
                        String newPB = request.getParameter("newplaybook").trim().replace(' ', '_');
                        if (!newPB.endsWith(".yml")) {
                            newPB = newPB + ".yml";
                        }
                        File newFile = new File(ansPath, newPB);
                        if (!newFile.exists()) {
                            String[] addPbRoles = request.getParameterValues("addpbroles");
                            String newRole = request.getParameter("newrole").trim().replace(' ', '_');
                            ArrayList<String> roles = new ArrayList<String>();
                            if (addPbRoles != null) {
                                for (String ar : addPbRoles) {
                                    roles.add(ar);
                                }
                            }
                            if (!newRole.isEmpty()) {
                                roles.add(newRole);
                                new File(ansPath, "roles/" + newRole+"/tasks").mkdirs();
                                File dontOverwrite = new File(ansPath, "roles/" + newRole + "/tasks/main.yml");
                                if (!dontOverwrite.exists()) {
                                    FileWriter fw = new FileWriter(dontOverwrite);
                                    fw.write("- name: ");
                                    fw.write(newRole);
                                    fw.write("_task\n");
                                    fw.close();
                                }
                            }
                            FileWriter writer = new FileWriter(newFile);
                            YamlWriter yw = new YamlWriter(writer);
                            Map rm = new HashMap();
                            rm.put("roles", roles);
                            yw.write(Collections.singletonList(rm));
                            yw.close();
                        }
                    }
                    PlayBooks books = new PlayBooks(new File(ansPath));
                    String rolesSize = "4";
                    if (!books.roles.isEmpty()) {
                        rolesSize = "" + Math.max(books.roles.size(), 10);
                    }
            %>
            <table border="1">
                <tr>
                    <th colspan="2">Create a new playbook.</th>
                </tr>
                <tr>
                    <td>Enter a name for the playbook:</td>
                    <td><input type="text" name="newplaybook"></td>                    
                </tr>
                <tr>
                    <td>Optionally add some roles:</td>
                    <td>
                        <select name="addpbroles" multiple="yes" size="<%=rolesSize%>">
                            <%
                                for (String rnam : books.roles.keySet()) {
                                    out.println("<option>" + rnam + "</option>");
                                }
                            %>
                        </select>
                    </td>
                </tr>
                <tr>
                    <td>And/or create a new role:</td>
                    <td><input type="text" name="newrole"></td>                    
                </tr>
                <tr>
                    <td colspan="2"><input type="submit" name="creplaybook" value="Go!" /></td>                    
                </tr>
            </table>
            <%
                    {
                        JHDocument doc = new JHDocument();
                        JHFragment top = new JHFragment(doc, "div");
                        top.setStyleElement("background-color", "lightyellow");
                        top.setStyleElement("color", "black");
                        for (PlayBook book : books.playBooks.values()) {
                            book.toHtml(request, top);
                        }
                        doc.write(out);
                    }
                    {
                        JHDocument doc = new JHDocument();
                        JHFragment top = new JHFragment(doc, "div");
                        top.setStyleElement("background-color", "lightcyan");
                        top.setStyleElement("color", "black");
                        top.appendP("List of all roles");
                        top.push("table").appendAttr("border", "1");
                        books.printRolesTable(top);
                        top.pop();
                        doc.write(out);
                    }
                    {
                        JHDocument doc = new JHDocument();
                        JHFragment top = new JHFragment(doc, "div");
                        top.setStyleElement("background-color", "lightskyblue");
                        top.setStyleElement("color", "black");
                        top.appendP("Cross-referenced list of all variables");
                        top.push("table").appendAttr("border", "1");
                        books.printVarsTable(top);
                        top.pop();
                        doc.write(out);
                    }
                    {
                        JHDocument doc = new JHDocument();
                        JHFragment top = new JHFragment(doc, "div");
                        top.setStyleElement("background-color", "lightsalmon");
                        top.setStyleElement("color", "black");
                        top.appendP("Other (random) files and/or directories");
                        top.push("table").appendAttr("border", "1");
                        for (File f : books.randomFiles) {
                            if (f.isDirectory()) {
                                top.createElement("tr").appendTD(f.getAbsolutePath() + " (dir)");
                            } else {
                                top.createElement("tr").createElement("td").appendA("EditAny?file=" + f.getAbsolutePath(), "_blank", books.shortFileName(f));
                            }
                        }
                        top.pop();
                        doc.write(out);
                    }
                }
            %>        
        </form>
    </body>
</html>
