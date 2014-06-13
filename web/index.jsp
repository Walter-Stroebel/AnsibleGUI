<%-- 
    Document   : index
    Created on : Jun 11, 2014, 6:43:30 AM
    Author     : walter
--%>
<%@page import="nl.infcomtec.ansible.PlayBook"%>
<%@page import="nl.infcomtec.ansible.PlayBooks"%>
<%@page import="java.util.concurrent.atomic.AtomicInteger"%>
<%@page import="java.util.LinkedList"%>
<%@page import="nl.infcomtec.ansible.AnsObject"%>
<%@page import="nl.infcomtec.javahtml.JHFragment"%>
<%@page import="nl.infcomtec.javahtml.JHDocument"%>
<%@page import="java.io.File"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
            <%
                String ansPath = request.getParameter("anspath") != null ? request.getParameter("anspath").trim() : "";
                boolean hasGIT = false;
                if (ansPath.isEmpty()){
                    if (session.getAttribute("anspath")!=null){
                        ansPath=session.getAttribute("anspath").toString();
                    }
                } else {
                    session.setAttribute("anspath", ansPath);
                }
                if (!ansPath.isEmpty()) {
                    File git = new File (ansPath,".git");
                    hasGIT=git.exists()&&git.isDirectory();                        
                }
                if (hasGIT && request.getParameter("GIT")!=null){
                    response.sendRedirect("MiniGIT");
                }
            %>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>AnsibleGUI</title>
    </head>
    <body>
        <form action="index.jsp" method="POST">
            <input type="text" value="<%=ansPath%>" placeholder="Full path to your ansible files" size="50" name="anspath" />
            <input type="submit" value="Refresh" />
            <% if (hasGIT) { %>
            <input type="submit" name="GIT" value="Git..." />
            <% } %>
            <input type="submit" name="coll_all_play" value="Collapse all playbooks" />
            <input type="submit" name="expn_all_play" value="Expand all playbooks" />
            <%
                if (!ansPath.isEmpty()) {
                    PlayBooks books = new PlayBooks(new File(ansPath));
                    {
                        JHDocument doc = new JHDocument();
                        JHFragment top = new JHFragment(doc, "div");
                        for (PlayBook book : books.playBooks.values()) {
                            book.toHtml(request, top);
                        }
                        doc.write(out);
                    }
                    {
                        JHDocument doc = new JHDocument();
                        JHFragment top = new JHFragment(doc, "div");
                        top.appendP("Other (random) files and/or directories");
                        top.push("table").appendAttr("border", "1");
                        for (File f : books.randomFiles) {
                            if (f.isDirectory()) {
                                top.createElement("tr").appendTD(f.getAbsolutePath() + " (dir)");
                            } else {
                                top.createElement("tr").createElement("td").appendA("EditAny?file=" + f.getAbsolutePath(), "_blank", f.getAbsolutePath());
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
