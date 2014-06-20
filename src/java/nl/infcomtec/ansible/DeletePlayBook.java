/*
 * Copyright (c) 2014 by Walter Stroebel and InfComTec.
 * All rights reserved.
 */
package nl.infcomtec.ansible;

import com.esotericsoftware.yamlbeans.YamlWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
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
@WebServlet(name = "DeletePlayBook", urlPatterns = {"/DeletePlayBook"})
public class DeletePlayBook extends HttpServlet {

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
            JHParameter fubar = new JHParameter(request, "fubar", "Yes, make it so!");
            JHParameter file = new JHParameter(request, "file");
            File tFile = new File(file.getValue());
            AnsObject tObj = new AnsObject(null, tFile, new FileReader(tFile));
            JHDocument doc = new JHDocument();
            JHFragment top = new JHFragment(doc, "html");
            top.push("head");
            top.createElement("title").appendText("Delete a playbook");
            top.pop();
            top.push("body");
            if (fubar.wasSet) {
                tFile.delete();
                top.appendA("index.jsp", "All done, return me to the main page.");
                doc.write(out);
                return;
            }
            top.push("form");
            top.appendAttr("action", "DeletePlayBook").appendAttr("method", "POST");
            top.createInput("hidden", file);
            String myw = tObj.makeString();
            top.appendP("Delete playbook [" + tFile.getAbsolutePath() + "]?");
            top.createElement("pre").appendText(myw.toString());
            top.createInput("submit", fubar).setStyleElement("font-size", "larger");
            top.appendA("index.jsp", "(Else just return to the main page here).");
            doc.write(out);
        } catch (Exception ex) {
            throw new ServletException(ex);
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

}
