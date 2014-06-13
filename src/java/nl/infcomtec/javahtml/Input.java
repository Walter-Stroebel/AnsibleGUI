/*
 * Copyright (c) 2014 by Walter Stroebel and InfComTec.
 * All rights reserved.
 */
package nl.infcomtec.javahtml;

import java.util.TreeMap;
import org.w3c.dom.Node;

/**
 *
 * @author walter
 */
public class Input extends JHFragment {

    public final JHParameter p;

    public Input(Node parent, TreeMap<String, String> style, String type, JHParameter p) {
        super(parent, style);
        this.p = p;
        appendAttr("name", p.varName).appendAttr("type", type);
        if (p.notEmpty()) {
            appendAttr("value", p.getValue());
        }
    }

    protected Input(Node parent, TreeMap<String, String> style, JHParameter p) {
        super(parent, style);
        this.p = p;
        appendAttr("name", p.varName);
        if (p.notEmpty()) {
            appendAttr("value", p.getValue());
        }
    }

}
