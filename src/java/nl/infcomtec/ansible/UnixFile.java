/*
 * Copyright (c) 2014 by Walter Stroebel and InfComTec.
 * All rights reserved.
 */
package nl.infcomtec.ansible;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

/**
 * Java interface to the Unix/Linux file(1) command.
 * @author walter
 */
public class UnixFile {

    public static String whatsThatFile(File f) {
        try {
            ProcessBuilder pb = new ProcessBuilder("/usr/bin/file", f.getAbsolutePath());
            pb.redirectErrorStream(true);
            Process p = pb.start();
            try (BufferedReader bfr = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                String s = bfr.readLine();
                if (s != null) {
                    for (String extra = bfr.readLine(); extra != null; extra = bfr.readLine()) {
                        System.err.println("Odd, did not expect more than one line of text ... " + s + " " + extra);
                    }
                    return s;
                }
            } finally {
                p.waitFor();
            }
        } catch (Exception all) {
            // ignore, should not be critical
        }
        return "???";
    }

    public static boolean isItASCII(File f){
        return whatsThatFile(f).contains("ASCII");
    }
}
