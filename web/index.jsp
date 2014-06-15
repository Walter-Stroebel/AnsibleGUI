<%-- 
    Document   : index
    Created on : Jun 11, 2014, 6:43:30 AM
    Author     : walter
--%>
<%@page import="nl.infcomtec.ansible.PlayBooks"%>
<%@page import="java.io.File"%>
<%
    response.setHeader("Cache-Control", "no-store");
    response.setHeader("Pragma", "no-cache");
    response.setDateHeader("Expires", 0);
%>
<%@page contentType="text/html" pageEncoding="UTF-8" language="java" errorPage="" %>
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
        <link rel="shortcut icon" type="image/png" href="icons/user_r2d2.png?" /> 
        <style>
            .anchor {
                color: darkblue;
                text-decoration: underline;
            }
            div {
                margin: 5px;
                padding: 5px;
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
                    PlayBooks books = new PlayBooks(new File(ansPath));
                    books.processNewPlayBookForm(request, out);
                    books.processEditRoleForm(request, out);
                    books.scan();
                    books.writeNewPlayBookForm(request, out);
                    books.writeEditRoleForm(request, out);
                    out.println("<div style=\"clear: both;\" />");
                    books.writePlayBooks(request, out);
                    books.writeRoles(request, out);
                    books.writeVariables(request, out);
                    books.writeRandomFiles(request, out);
                }
            %>        
        </form>
    </body>
</html>
