/*
 * Copyright (c) 2014 by Walter Stroebel and InfComTec.
 * All rights reserved.
 */
package nl.infcomtec.ansible;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import nl.infcomtec.javahtml.JHFragment;

/**
 *
 * @author walter
 */
@WebServlet(name = "EditAny", urlPatterns = {"/EditAny"})
public class EditAny extends HttpServlet {

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
        String fnam = request.getParameter("file");
        if (request.getParameter("save") != null) {
            try (PrintWriter pw = new PrintWriter(new File(fnam))) {
                pw.print(request.getParameter("edit"));
            }
        }
        try (PrintWriter out = response.getWriter()) {
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            String title;
            if (fnam.length() > 28) {
                title = fnam.substring(fnam.length() - 28);
            } else {
                title = fnam;
            }
            out.println("<title>" + title + "</title>");
            out.println("</head>");
            out.println("<body>");
            out.println("<form action=\"EditAny\" method=\"POST\">");
            out.println("<input type=\"hidden\" name=\"file\" value=\"" + fnam + "\" />");
            if (request.getParameter("warn") != null) {
                out.println("<h1 style=\"background-color: red; color: white;\">You were send here from the YAML editor ... fix this file?</h1>");
            }
            out.println("<h1>" + fnam + "</h1>");
            File f = new File(fnam);
            boolean can = UnixFile.isItASCII(f);
            if (can && f.length() < 32000) {
                out.println("<textarea name=\"edit\" rows=\"36\" cols=\"150\">");
                try (BufferedReader bfr = new BufferedReader(new FileReader(f))) {
                    for (String tmp = bfr.readLine(); tmp != null; tmp = bfr.readLine()) {
                        out.println(JHFragment.html(tmp.toString()));
                    }
                }
                out.println("</textarea><br />");
                out.println("<input type=\"submit\" name=\"save\" value=\"Save\" />");
            } else if (!can) {
                out.println("<p>It does not seem a good idea to edit a <b>" + UnixFile.whatsThatFile(f) + "</b> as if it was text...</p>");
            } else {
                out.println("<p>Sorry, applying an abbitrary limit of about 32K to maximum file size to edit, this file is " + f.length() + " bytes.</p>");
            }
            out.println("</form>");
            out.println("</body>");
            out.println("</html>");
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
