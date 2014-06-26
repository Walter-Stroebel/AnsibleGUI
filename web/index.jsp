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
    String ansInv = request.getParameter("ansinv") != null ? request.getParameter("ansinv").trim() : "";
    if (ansInv.isEmpty()) {
        if (session.getAttribute("ansinv") != null) {
            ansInv = session.getAttribute("ansinv").toString();
        }
    } else {
        session.setAttribute("ansinv", ansInv);
    }
    if (!ansInv.isEmpty()) {
        File make_abs = new File(ansInv);
        if (!make_abs.exists()) {
            make_abs = new File(ansPath, ansInv);
            if (make_abs.exists()) {
                ansInv = make_abs.getAbsolutePath();
                session.setAttribute("ansinv", ansInv);
            } // else ah well, we tried.            
        } // else all is well, we hope.
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
    <body style="
          margin:0px;
          padding:5px;
          border:0px;
          background-color: linen; 
          color: black; 
          float: left;
          ">
        <form action="index.jsp" method="POST">
            <p>Path:
                <input type="text" value="<%=ansPath%>" placeholder="Full path to your ansible files" size="50" name="anspath" />
                <input type="text" value="<%=ansInv%>" placeholder="Use inventory (optional; can be full path)" size="50" name="ansinv" />
                <input type="submit" value="Refresh" />
                <% if (hasGIT) { %>
                <input type="submit" name="GIT" value="Git..." />
                <% } %>
            </p>
            <%
                if (!ansPath.isEmpty()) {
                    File test = new File(ansPath);
                    if (!test.exists() || !test.isDirectory()) {
                        out.println("That is not a valid path.");
                        ansPath = "";
                    }
                }
                if (!ansPath.isEmpty()) {
                    PlayBooks books = new PlayBooks(new File(ansPath), request, out);
                    books.processNewPlayBookForm();
                    books.processEditRoleForm();
                    books.processEditInventory(ansInv);
                    books.scan();
                    books.writeNewPlayBookForm();
                    books.writeEditRoleForm();
                    books.writeEditInventory();
            %>
            <div style="clear: both;" > </div>
            <input type="submit" name="coll_all_play" value="Collapse all playbooks" />
            <input type="submit" name="expn_all_play" value="Expand all playbooks" />
            <%
                    books.writePlayBooks();
                    books.writeRoles();
                    books.writeVariables();
                    books.writeRandomFiles();
                }
            %>        
        </form>

    </body>
</html>
