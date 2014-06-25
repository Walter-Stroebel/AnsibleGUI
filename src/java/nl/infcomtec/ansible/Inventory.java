/*
 * Copyright (c) 2014 by Walter Stroebel and InfComTec.
 * All rights reserved.
 */
package nl.infcomtec.ansible;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import nl.infcomtec.ansible.AnsString;
import nl.infcomtec.javahtml.JHDocument;
import nl.infcomtec.javahtml.JHFragment;
import nl.infcomtec.javahtml.JHParameter;

/**
 *
 * @author walter
 */
@WebServlet(name = "Inventory", urlPatterns = {"/Inventory"})
public class Inventory extends HttpServlet {

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
                String ansPath = (String) request.getSession().getAttribute("anspath");
                String ansInv = (String) request.getSession().getAttribute("ansinv");
                PlayBooks books = new PlayBooks(new File(ansPath));
                books.inv = new AnsInventory(books.directory, new File(ansInv));
                books.scan();
                JHParameter pGroup = new JHParameter(request, "group");
                JHParameter pHost = new JHParameter(request, "host");
                JHDocument doc = new JHDocument();
                JHFragment top = new JHFragment(doc, "html");
                top.push("head");
                top.createElement("title").appendText("Edit inventory");
                top.pop();
                top.push("body");//.setStyleElement("color", "darkgreen");
                top.push("form");
                top.appendAttr("action", "Inventory").appendAttr("method", "POST");
                top.createInput("hidden", pGroup);
                top.createInput("hidden", pHost);
                if (pGroup.notEmpty()) {
                    TreeSet<String> hosts = books.inv.groups.get(pGroup.getValue());
                    top.push("dl");
                    top.createElement("dt").appendText(pGroup.getValue());
                    TreeMap<AnsString, AnsElement> gvars = books.inv.vars.get("g_" + pGroup.getValue());
                    top.push("dd");
                    if (gvars != null) {
                        for (Map.Entry<AnsString, AnsElement> e : gvars.entrySet()) {
                            top.createElement("br");
                            top.appendText(e.getKey().getString());
                            top.appendText("=");
                            top.appendText(e.getValue().getString()!=null?e.getValue().getString():e.getValue().toString());
                        }
                    }
                    top.push("ul");
                    for (String h : hosts) {
                        top.push("li");
                        printHost(top, h, books);
                    }
                    top.pop("dl");
                } else if (pHost.notEmpty()){
                    TreeSet<String> groups = books.inv.hosts.get(pHost.getValue());
                    top.push("dl");
                    top.createElement("dt").appendText(pHost.getValue());
                    TreeMap<AnsString, AnsElement> hvars = books.inv.vars.get("h_" + pHost.getValue());
                    top.push("dd");
                    if (hvars != null) {
                        for (Map.Entry<AnsString, AnsElement> e : hvars.entrySet()) {
                            top.createElement("br");
                            top.appendText(e.getKey().getString());
                            top.appendText("=");
                            top.appendText(e.getValue().getString()!=null?e.getValue().getString():e.getValue().toString());
                        }
                    }
                    top.push("ul");
                    for (String g : groups) {
                        top.push("li");
                        printGroup(top, g, books);
                    }
                    top.pop("dl");
                }
                top.appendA("index.jsp", "Return to the editor.");
                doc.write(out);
            } catch (Exception ex) {
                throw new ServletException(ex);
            }
        }
    }

    private void printHost(JHFragment top, String h, PlayBooks books) {
        top.appendText(h);
        top.push("ul");
        TreeMap<AnsString, AnsElement> hvars = books.inv.vars.get("h_" + h);
        if (hvars != null) {
            for (Map.Entry<AnsString, AnsElement> e : hvars.entrySet()) {
                StringBuilder li = new StringBuilder(e.getKey().getString());
                li.append("=");
                li.append(e.getValue().getString()!=null?e.getValue().getString():e.getValue().toString());
                top.appendLI(li.toString());
            }
        }
        top.pop();
    }

    private void printGroup(JHFragment top, String g, PlayBooks books) {
        top.appendText(g);
        top.push("ul");
        TreeMap<AnsString, AnsElement> gvars = books.inv.vars.get("g_" + g);
        if (gvars != null) {
            for (Map.Entry<AnsString, AnsElement> e : gvars.entrySet()) {
                StringBuilder li = new StringBuilder(e.getKey().getString());
                li.append("=");
                li.append(e.getValue().getString()!=null?e.getValue().getString():e.getValue().toString());
                top.appendLI(li.toString());
            }
        }
        top.pop();
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

}
