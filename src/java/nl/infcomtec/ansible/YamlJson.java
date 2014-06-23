/*
 * Copyright (c) 2014 by Walter Stroebel and InfComTec.
 * All rights reserved.
 */
package nl.infcomtec.ansible;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

/**
 * Converts between YAML and JSON. Despite some rather major effort, I failed to
 * find a reliable method to handle YAML with Java (or by hand, for that
 * matter). However, Ansible is Python and Python can convert between JSON and
 * YAML using the commands in this class. Problem solved.
 *
 * @author walter
 */
public class YamlJson {

    public void write(File f, String s) throws IOException {
        try (FileWriter fw = new FileWriter(f)) {
            fw.write(s);
        }
    }

    public void write(File f, Object obj) throws IOException {
        write(f, obj.toString());
    }

    public static String json2Yaml(FileReader json) throws IOException {
        JSONTokener toker = new JSONTokener(json);
        return json2Yaml(new JSONObject(toker));
    }

    public static String json2Yaml(File json) throws IOException {
        try (FileReader fr = new FileReader(json)) {
            return YamlJson.json2Yaml(fr);
        }
    }

    public static Object yaml2Json(FileReader yaml) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader bfr = new BufferedReader(yaml)) {
            for (String s = bfr.readLine(); s != null; s = bfr.readLine()) {
                sb.append(s).append('\n');
            }
        }
        return yaml2Json(sb.toString());
    }

    public static Object yaml2Json(File yaml) throws IOException {
        try (FileReader fr = new FileReader(yaml)) {
            return YamlJson.yaml2Json(fr);
        }
    }

    public static Object yaml2Json(final String yaml) throws IOException {
        // python -c 'import sys, yaml, json; json.dump(yaml.load(sys.stdin), sys.stdout)'
        ProcessBuilder pb = new ProcessBuilder("python", "-c", "import sys, yaml, json; json.dump(yaml.load(sys.stdin), sys.stdout)");
        pb.redirectErrorStream(true);
        final Process p = pb.start();
        new Thread() {
            @Override
            public void run() {
                try {
                    p.getOutputStream().write(yaml.getBytes());
                    p.getOutputStream().close();
                } catch (IOException ex) {
                    // oops
                }
            }
        }.start();
        StringBuilder sb = new StringBuilder();
        try (BufferedReader bfr = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
            for (String s = bfr.readLine(); s != null; s = bfr.readLine()) {
                sb.append(s);
            }
        } finally {
            try {
                p.waitFor();
            } catch (InterruptedException ex) {
                // no need to wait any longer?
            }
        }
        if (sb.charAt(0) == '[') {
            return new JSONArray(sb.toString());
        }
        return new JSONObject(sb.toString());
    }

    public static String json2Yaml(final Object json) throws IOException {
        // python -c 'import sys, yaml, json; yaml.dump(json.load(sys.stdin), sys.stdout, default_flow_style=False)'
        ProcessBuilder pb = new ProcessBuilder("python", "-c", "import sys, yaml, json; yaml.safe_dump(json.load(sys.stdin), sys.stdout, default_flow_style=False)");
        pb.redirectErrorStream(true);
        final Process p = pb.start();
        new Thread() {
            @Override
            public void run() {
                try {
                    p.getOutputStream().write(json.toString().getBytes());
                    p.getOutputStream().close();
                } catch (IOException ex) {
                    // oops
                }
            }
        }.start();
        StringBuilder sb = new StringBuilder();
        try (BufferedReader bfr = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
            for (String s = bfr.readLine(); s != null; s = bfr.readLine()) {
                sb.append(s);
                sb.append('\n');
            }
        } finally {
            try {
                p.waitFor();
            } catch (InterruptedException ex) {
                // no need to wait any longer?
            }
        }
        return sb.toString();
    }
}
