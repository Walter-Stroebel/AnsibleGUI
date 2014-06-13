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
public class CheckBox extends JHFragment {

    public final JHParameter p;

    public CheckBox(Node parent, TreeMap<String, String> style, JHParameter p) {
        super(parent, style);
        this.p = p;
        appendAttr("name", p.varName).appendAttr("type", "checkbox");
        if (p.notEmpty()) {
            appendAttr("value", p.getValue());
        }
        if (p.wasSet) {
            appendAttr("checked");
        }
    }

}
