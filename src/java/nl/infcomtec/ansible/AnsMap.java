/*
 * Copyright (c) 2014 by Walter Stroebel and InfComTec.
 * All rights reserved.
 */
package nl.infcomtec.ansible;

import java.util.Map.Entry;
import java.util.TreeMap;

/**
 *
 * @author walter
 */
public class AnsMap extends TreeMap<AnsString, AnsElement> implements AnsElement {

    @Override
    public String toString() {
        StringBuilder ret = new StringBuilder("\nAnsMap{");
        for (Entry<AnsString, AnsElement> e : this.entrySet()) {
            ret.append(", ");
            ret.append(e.getKey());
            ret.append("=");
            ret.append(e.getValue());
        }
        return ret.append("}").toString();
    }

    @Override
    public String getString() {
        return null;
    }

    @Override
    public AnsList getList() {
        return null;
    }

    @Override
    public AnsMap getMap() {
        return this;
    }

    public AnsElement get(String key) {
        return get(new AnsString(key));
    }

    public AnsElement remove(String key) {
        return remove(new AnsString(key));
    }

    @Override
    public String getStringFor(String name) {
        if (get(name) != null) {
            return get(name).getString();
        }
        for (AnsElement e : values()) {
            String ret = e.getStringFor(name);
            if (ret != null) {
                return ret;
            }
        }
        return null;
    }

}
