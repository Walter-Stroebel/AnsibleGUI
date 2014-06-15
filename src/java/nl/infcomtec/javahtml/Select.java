/*
 * Copyright (c) 2014 by Walter Stroebel and InfComTec.
 * All rights reserved.
 */
package nl.infcomtec.javahtml;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 *
 * @author walter
 */
public class Select extends Input {

    public abstract class Adder<Source> {

        public abstract String getOptValue();

        public abstract String getOptDesc();

        public abstract boolean next();
    }

    public Select(Node parent, TreeMap<String, String> style, JHParameter p) {
        super(parent, style, p);
    }

    public Select setAutoSubmit() {
        appendAttr("onChange", "this.form.submit()");
        return this;
    }

    public void addOptions(Adder<?> adder) {
        while (adder.next()) {
            addOption(adder.getOptValue(), adder.getOptDesc());
        }
    }

    public void addOptions(final ResultSet rs) {
        addOptions(new Adder<ResultSet>() {

            @Override
            public String getOptValue() {
                try {
                    return rs.getString(1);
                } catch (SQLException ex) {
                    return "?";
                }
            }

            @Override
            public String getOptDesc() {
                try {
                    return rs.getString(2);
                } catch (SQLException ex) {
                    return "?";
                }
            }

            @Override
            public boolean next() {
                try {
                    return rs.next();
                } catch (SQLException ex) {
                    return false;
                }
            }
        });
    }

    public void addOption(String optValue, String optDesc) {
        Element e = current.getOwnerDocument().createElement("OPTION");
        e.setAttribute("value", optValue);
        if (optValue.equals(p.getValue())) {
            e.setAttribute("selected", "selected");
        }
        e.appendChild(current.getOwnerDocument().createTextNode(optDesc));
        current.appendChild(e);
    }
}
