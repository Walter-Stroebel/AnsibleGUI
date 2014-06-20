/*
 * Copyright (c) 2014 by Walter Stroebel and InfComTec.
 * All rights reserved.
 */
package nl.infcomtec.ansible;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
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
@WebServlet(name = "MiniGIT", urlPatterns = {"/MiniGIT"})
public class MiniGIT extends HttpServlet {

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
        String ansPath = (String) request.getSession().getAttribute("anspath");
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            out.println("<!DOCTYPE html>");
            try {
                JHParameter pull = new JHParameter(request, "pull", "Pull");
                JHParameter push = new JHParameter(request, "push", "Push");
                JHParameter commit = new JHParameter(request, "commit", "Commit");
                JHParameter ctext = new JHParameter(request, "ctext");
                JHDocument doc = new JHDocument();
                JHFragment html = new JHFragment(doc, "html");
                html.push("head");
                html.createElement("title").appendText("MiniGIT");
                html.pop();
                html.push("body");
                html.push("form").appendAttr("action", "MiniGIT").appendAttr("method", "POST");
                html.appendP("(Very) minimal GIT support.");
                html.appendA("index.jsp", "(Or return to the editor if done here).");
                html.appendP("Just supports the standard work-flow of"
                        + " [git pull origin master],[edit],[git add -A .], [git commit -a -m'what I did'],[git push origin master]");
                html.appendP("In other words, this will only work if:");
                html.push("ul");
                html.appendLI("You have GIT installed and accessible to the Tomcat user.");
                html.appendLI("You have GIT set up to pull/push from/to a remote without asking for anything, not even a password.");
                html.appendLI("You did not introduce any conflicts.");
                html.appendLI("You used standard names 'origin' and 'master'.");
                html.pop();
                html.createInput("submit", pull);
                html.createInput("submit", push);
                html.appendP("Enter a comment here if you are going to commit:");
                html.createElement("textarea").appendAttr("rows", "24").appendAttr("cols", "80").appendAttr("name", "ctext");
                html.createInput("submit", commit);
                html.createElement("hr");
                html.appendA("index.jsp", "All done, return to editor");
                html.createElement("hr");
                if (pull.wasSet) {
                    html.push("pre");
                    html.appendText(doPull(ansPath));
                    html.pop();
                }
                if (push.wasSet) {
                    html.push("pre");
                    html.appendText(doPush(ansPath));
                    html.pop();
                }
                if (commit.wasSet) {
                    html.push("pre");
                    if (ctext.notEmpty()) {
                        html.appendText(doCommit(ansPath, ctext.getValue()));
                    } else {
                        html.appendText("You are required to enter a commit message.");
                    }
                    html.pop();
                }
                html.pop("html");
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

    private String doPull(String ansPath) {
        try {
            ProcessBuilder pb = new ProcessBuilder("git", "pull", "origin", "master");
            pb.redirectErrorStream(true);
            pb.directory(new File(ansPath));
            Process p = pb.start();
            StringBuilder ret = new StringBuilder();
            try (BufferedReader bfr = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                for (String s = bfr.readLine(); s != null; s = bfr.readLine()) {
                    ret.append(s).append("\n");
                }
                return ret.toString();
            } finally {
                p.waitFor();
            }
        } catch (Exception all) {
            // ignore, should not be critical
        }
        return "???";
    }

    private String doPush(String ansPath) {
        try {
            ProcessBuilder pb = new ProcessBuilder("git", "push", "origin", "master");
            pb.redirectErrorStream(true);
            pb.directory(new File(ansPath));
            Process p = pb.start();
            StringBuilder ret = new StringBuilder();
            try (BufferedReader bfr = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                for (String s = bfr.readLine(); s != null; s = bfr.readLine()) {
                    ret.append(s).append("\n");
                }
                return ret.toString();
            } finally {
                p.waitFor();
            }
        } catch (Exception all) {
            // ignore, should not be critical
        }
        return "???";
    }

    private String doCommit(String ansPath, String ctext) {
        StringBuilder ret = new StringBuilder();
        try {
            ProcessBuilder pb = new ProcessBuilder("git", "add", "-A", ".");
            pb.redirectErrorStream(true);
            pb.directory(new File(ansPath));
            Process p = pb.start();
            try (BufferedReader bfr = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                for (String s = bfr.readLine(); s != null; s = bfr.readLine()) {
                    ret.append(s).append("\n");
                }
            } finally {
                p.waitFor();
            }
        } catch (Exception all) {
            // ignore, should not be critical
        }
        try {
            ProcessBuilder pb = new ProcessBuilder("git", "commit", "-a", "-m", ctext);
            pb.redirectErrorStream(true);
            pb.directory(new File(ansPath));
            Process p = pb.start();
            try (BufferedReader bfr = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                for (String s = bfr.readLine(); s != null; s = bfr.readLine()) {
                    ret.append(s).append("\n");
                }
                return ret.toString();
            } finally {
                p.waitFor();
            }
        } catch (Exception all) {
            // ignore, should not be critical
        }
        return "???";
    }

}
