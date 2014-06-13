/*
 * Copyright (c) 2014 by Walter Stroebel and InfComTec.
 * All rights reserved.
 */
package nl.infcomtec.ansible;

import com.esotericsoftware.yamlbeans.YamlConfig;
import com.esotericsoftware.yamlbeans.YamlException;
import com.esotericsoftware.yamlbeans.YamlWriter;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
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
@WebServlet(name = "EditYml", urlPatterns = {"/EditYml"})
public class EditYml extends HttpServlet {

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
        String fnam = request.getParameter("file");
        File f = new File(fnam);
        AnsObject o;
        try {
            o = new AnsObject(f, new FileReader(f));
        } catch (YamlException ex) {
            // not YAML, send to general editor to fix
            response.sendRedirect("EditAny?warn=true&file=" + fnam);
            return;
        }
        if (request.getParameter("save") != null) {
            try (PrintWriter pw = new PrintWriter(new File(fnam))) {
                pw.print(request.getParameter("edit"));
            }
            // and reload the file!
            try {
                o = new AnsObject(f, new FileReader(f));
            } catch (YamlException ex) {
                // not YAML (anymore), send to general editor to fix
                response.sendRedirect("EditAny?warn=true&file=" + fnam);
                return;
            }
        }
        response.setContentType("text/html;charset=UTF-8");
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
            out.println("<form action=\"EditYml\" method=\"POST\">");
            out.println("<input type=\"hidden\" name=\"file\" value=\"" + fnam + "\" />");
            out.println("<h1>" + fnam + "</h1>");
            out.println("<textarea name=\"edit\" rows=\"36\" cols=\"150\">");
            YamlConfig config = new YamlConfig();
            config.writeConfig.setWrapColumn(150);
            MyWriter toHtml = new MyWriter();
            YamlWriter writer = new YamlWriter(toHtml, config);
            writer.write(o.object);
            writer.close();
            out.println(toHtml.toString());
            out.println("</textarea><br />");
            out.println("<input type=\"submit\" name=\"save\" value=\"Save\" />");
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

    private static class MyWriter extends Writer {

        private final StringBuilder out;

        public MyWriter() {
            out = new StringBuilder();
        }

        @Override
        public void write(char[] cbuf, int off, int len) throws IOException {
            out.append(cbuf, off, len);
        }

        @Override
        public void flush() throws IOException {
        }

        @Override
        public void close() throws IOException {
            for (int dash = out.indexOf("\n-"); dash != -1; dash = out.indexOf("\n-", dash)) {
                out.insert(dash, '\n');
                dash += 3;
            }
        }

        @Override
        public String toString() {
            return JHFragment.html(out.toString());
        }
    }

}
