/*
 * Copyright (c) 2014 by Walter Stroebel and InfComTec.
 * All rights reserved.
 */

package nl.infcomtec.ansible;

import java.util.ArrayList;

/**
 *
 * @author walter
 */
public class AnsList extends ArrayList<AnsElement> implements AnsElement {

    @Override
    public String toString() {
        StringBuilder ret = new StringBuilder("\nAnsList{");
        for (AnsElement e : this) {
            ret.append(", ");
            ret.append(e);
        }
        return ret.append("}").toString();
    }

    @Override
    public String getString() {
        return null;
    }

    @Override
    public AnsList getList() {
        return this;
    }

    @Override
    public AnsMap getMap() {
        return null;
    }
    
}
