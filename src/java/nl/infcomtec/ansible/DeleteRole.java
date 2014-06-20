/*
 * Copyright (c) 2014 by Walter Stroebel and InfComTec.
 * All rights reserved.
 */
package nl.infcomtec.ansible;

import com.esotericsoftware.yamlbeans.YamlConfig;
import com.esotericsoftware.yamlbeans.YamlWriter;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import nl.infcomtec.javahtml.JHDocument;
import nl.infcomtec.javahtml.JHFragment;
import nl.infcomtec.javahtml.JHParameter;

/**
 *
 * @author walter
 */
@WebServlet(name = "DeleteRole", urlPatterns = {"/DeleteRole"})
public class DeleteRole extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            out.println("<!DOCTYPE html>");
            try {
                JHParameter fubar = new JHParameter(request, "fubar", "Yes, make it so!");
                JHParameter name = new JHParameter(request, "name");
                PlayBooks books = new PlayBooks(new File((String) request.getSession().getAttribute("anspath")));
                File roleDir = new File(new File(books.directory, "roles"), name.getValue());
                JHDocument doc = new JHDocument();
                JHFragment top = new JHFragment(doc, "html");
                top.push("head");
                top.createElement("title").appendText("Delete a role");
                top.pop();
                top.push("body").setStyleElement("color", "darkred");
                if (fubar.wasSet) {
                    deleteAll(roleDir);
                    for (File f : books.directory.listFiles()) {
                        if (!f.isDirectory() && f.getName().endsWith(".yml")) {
                            AnsObject pb = new AnsObject(null, f, new FileReader(f));
                            if (pb.removeElement(name.getValue())) {
                                String ymlOut = pb.makeString();                                
                                try (PrintWriter pw = new PrintWriter(f)) {
                                    pw.print(ymlOut);
                                }
                            }
                        }
                    }
                    top.appendA("index.jsp","All done, return me to the main page.");
                    doc.write(out);
                    return;
                }
                books.scan();
                top.push("form");
                top.appendAttr("action", "DeleteRole").appendAttr("method", "POST");
                top.createInput("hidden", name);
                top.appendP("You have requested to delete the role [" + name.getValue() + "].");
                boolean first = true;
                for (PlayBook b : books.playBooks.values()) {
                    if (b.roles.contains(name.getValue())) {
                        if (first) {
                            top.appendP("This will remove the role from these playbooks:");
                            top.push("UL");
                            first = false;
                        }
                        top.appendLI(books.shortFileName(b.inFile));
                    }
                }
                if (!first) {
                    top.pop();
                }
                Role role = books.roles.get(name.getValue());
                top.appendP("This will delete:");
                if (!role.files.isEmpty()) {
                    top.appendP("All of these files: " + role.files.values().toString());
                }
                if (!role.handlers.isEmpty()) {
                    top.appendP("All of these handlers: " + role.handlers.keySet().toString());
                }
                if (!role.tasks.isEmpty()) {
                    top.appendP("All of these tasks: " + role.getTaskNames().toString());
                }
                if (!role.templates.isEmpty()) {
                    top.appendP("All of these templates: " + role.templates.keySet().toString());
                }
                if (!role.vars.isEmpty()) {
                    top.appendP("All of these variables (under this role): " + role.vars.keySet().toString());
                }
                top.appendP("Or, in other words, all of these files:");
                top.push("UL");
                dumpDir(top, roleDir);
                top.pop();
                top.appendP("It that what you intended?");
                top.createInput("submit", fubar).setStyleElement("font-size", "larger");
                top.appendA("index.jsp","(Else just return to the main page here).");
                doc.write(out);
            } catch (Exception ex) {
                throw new ServletException(ex);
            }
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

    private void deleteAll(File del) {
        for (File f : del.listFiles()) {
            if (f.isDirectory()) {
                deleteAll(f);
            } else {
                f.delete();
            }
        }
        del.delete();
    }

    private void dumpDir(JHFragment frag, File d) {
        frag.appendLI(d.getAbsolutePath());
        if (d.isDirectory()) {
            frag.push("UL");
            for (File f : d.listFiles()) {
                dumpDir(frag, f);
            }
            frag.pop();
        }
    }
}
