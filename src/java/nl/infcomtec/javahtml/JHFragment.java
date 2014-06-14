/*
 * Copyright (c) 2014 by Walter Stroebel and InfComTec.
 * All rights reserved.
 */
package nl.infcomtec.javahtml;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Stack;
import java.util.TreeMap;
import javax.servlet.jsp.JspWriter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 *
 * @author walter
 */
public class JHFragment {

    /**
     * Utility function.
     *
     * @param out JspWriter.
     * @param text Text with potential &lt;,&gt;, " or &amp; to encode.
     * @throws IOException If JspWriter does.
     */
    public static void html(JspWriter out, String text) throws IOException {
        out.print(html(text));
    }

    /**
     * Converts a string to a HTML entity encoded string, preserving any
     * existing entities. This method properly encodes a string like
     * &lt;&amp;EURO;&gt; to &amp;lt;&amp;EURO;&amp;gt;.
     *
     * @param text Text with potential &lt;,&gt;, " or &amp; to encode.
     * @return The text with any &lt;,&gt;, " or &amp; converted to &amp;lt;,
     * &amp;gt;, &amp;quot; and &amp;amp; while preserving any occurrences of
     * &amp;any;.
     */
    public static String html(String text) {
        if (text == null) {
            return "";
        }
        int amp = text.indexOf('&');
        if (amp >= 0) {
            int semi = text.indexOf(';', amp);
            if (semi > amp && semi - amp < 7) { // seems a valid html entity
                StringBuilder sb = new StringBuilder();
                if (amp > 0) {
                    sb.append(html(text.substring(0, amp)));
                }
                sb.append(text.substring(amp, semi));
                if (semi < text.length() - 1) {
                    sb.append(html(text.substring(semi + 1)));
                }
                return sb.toString();
            }
        }
        StringBuilder ret = new StringBuilder();
        for (char c : text.toCharArray()) {
            if (c == '"') {
                ret.append("&quot;");
            } else if (c == '&') {
                ret.append("&amp;");
            } else if (c == '<') {
                ret.append("&lt;");
            } else if (c == '>') {
                ret.append("&gt;");
            } else {
                ret.append(c);
            }
        }
        return ret.toString();
    }

    /**
     * Replaces any ASCII double quotes with &amp;quot;.
     *
     * @param text Something like "this"
     * @return Something like &amp;quot;this&amp;quot;
     */
    public static String quote(String text) {
        if (text == null) {
            return "";
        }
        return text.replaceAll("\"", "&quot;");
    }

    public Node current;
    public TreeMap<String, String> style = new TreeMap<>();

    public JHFragment(JHFragment parent) {
        this(parent.current, parent.style);
    }

    public JHFragment(JHDocument doc, String topElement) {
        this(doc.doc.createElement(topElement));
        doc.doc.appendChild(current);
    }

    public JHFragment(Node parent, TreeMap<String, String> style) {
        this.current = parent;
        this.style.putAll(style);
    }

    public JHFragment(Node parent, String... styleElms) {
        this.current = parent;
        if (styleElms != null) {
            if ((styleElms.length & 1) != 0) {
                throw new RuntimeException("Bad style definition; not an even number of strings");
            }
            for (int i = 0; i < styleElms.length; i += 2) {
                style.put(styleElms[i], styleElms[i + 1]);
            }
        }
    }

    public JHFragment createElement(String name) {
        JHFragment ret = new JHFragment(current.getOwnerDocument().createElement(name), style);
        current.appendChild(ret.current);
        ret.applyStyle();
        return ret;
    }

    public Input createInput(String type, JHParameter p) {
        Input ret = new Input(current.getOwnerDocument().createElement("INPUT"), style, type, p);
        current.appendChild(ret.current);
        ret.applyStyle();
        return ret;
    }

    public CheckBox createCheckBox(JHParameter p) {
        CheckBox ret = new CheckBox(current.getOwnerDocument().createElement("INPUT"), style, p);
        current.appendChild(ret.current);
        ret.applyStyle();
        return ret;
    }

    public Select createSelect(JHParameter p) {
        Select ret = new Select(current.getOwnerDocument().createElement("INPUT"), style, p);
        current.appendChild(ret.current);
        ret.applyStyle();
        return ret;
    }

    public JHFragment appendText(String text) {
        current.appendChild(current.getOwnerDocument().createTextNode(text));
        return this;
    }

    public JHFragment appendAttr(String name, String value) {
        ((Element) current).setAttribute(name, value);
        return this;
    }

    /**
     * Special case for HMTL. For example the 'required' attribute which does
     * not have a value. Solved by adding required="required".
     *
     * @param name Name of the attribute.
     */
    public JHFragment appendAttr(String name) {
        ((Element) current).setAttribute(name, name);
        return this;
    }

    public JHFragment applyStyle() {
        if (!style.isEmpty()) {
            StringBuilder st = new StringBuilder();
            for (Map.Entry<String, String> e : style.entrySet()) {
                st.append(e.getKey()).append(": ").append(e.getValue()).append(";");
            }
            ((Element) current).setAttribute("style", st.toString());
        }
        return this;
    }

    public JHFragment setStyleElement(String name, String value) {
        style.put(name, value);
        applyStyle();
        return this;
    }

    public JHFragment pop() {
        applyStyle();
        current = current.getParentNode();
        return this;
    }

    /**
     * Close all elements upto and including the named one. Eg. pop("table")
     * will return to the element <b>containing</b> the table as its last child.
     *
     * @param until Element to clear back from.
     * @return The element that contains the named element as the last child
     * (the <b>parent</b> of the named element).
     */
    public JHFragment pop(String until) {
        while (!current.getNodeName().equalsIgnoreCase(until)) {
            pop();
        }
        return pop();
    }

    public JHFragment push(String name) {
        JHFragment ret = createElement(name);
        current = ret.current;
        return ret;
    }

    /**
     * Shorthand for createElement("P").appendText(text)
     *
     * @param text the text for the P element.
     * @return The P child element for chaining attribute/style calls.
     */
    public JHFragment appendP(String text) {
        return createElement("P").appendText(text);
    }

    /**
     * Shorthand for createElement("TD").appendText(text)
     *
     * @param text the text for the TD element.
     * @return The TD child element for chaining attribute/style calls.
     */
    public JHFragment appendTD(String text) {
        return createElement("TD").appendText(text);
    }

    /**
     * Shorthand for createElement("LI").appendText(text)
     *
     * @param text the text for the LI element.
     * @return The LI child element for chaining attribute/style calls.
     */
    public JHFragment appendLI(String text) {
        return createElement("LI").appendText(text);
    }

    /**
     * Shorthand for createElement("A").appendAttribute(href).appendText(text)
     *
     * @param text the text for the A element.
     * @param href See HTML A element.
     * @return The A child element for chaining attribute/style calls.
     */
    public JHFragment appendA(String href, String text) {
        JHFragment ret = createElement("A").appendAttr("href", href).appendText(text);
        for (Iterator<Map.Entry<String, String>> it = ret.style.entrySet().iterator(); it.hasNext();) {
            Map.Entry<String, String> elm = it.next();
            if (elm.getKey().equals("color")) {
                it.remove();
            }
        }
        ret.applyStyle();
        ret.appendAttr("class", "anchor");
        return ret;
    }

    /**
     * Shorthand for
     * createElement("A").appendAttribute(href).appendAttribute(target).appendText(text)
     *
     * @param href See HTML A element.
     * @param target See HTML A element.
     * @param text the text for the A element.
     * @return The A child element for chaining attribute/style calls.
     */
    public JHFragment appendA(String href, String target, String text) {
        JHFragment ret = createElement("A").appendAttr("href", href).appendAttr("target", target).appendText(text);
        for (Iterator<Map.Entry<String, String>> it = ret.style.entrySet().iterator(); it.hasNext();) {
            Map.Entry<String, String> elm = it.next();
            if (elm.getKey().equals("color")) {
                it.remove();
            }
        }
        ret.applyStyle();
        ret.appendAttr("class", "anchor");
        return ret;
    }

}
