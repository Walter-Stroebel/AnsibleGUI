/*
 * Copyright (c) 2014 by Walter Stroebel and InfComTec.
 * All rights reserved.
 */

package nl.infcomtec.ansible;

/**
 *
 * @author walter
 */
public class AnsDocument implements AnsElement {
    public AnsElement root = null;

    @Override
    public String toString() {
        return "AnsDocument{" + "root=" + root + '}';
    }

    @Override
    public String getString() {
        return null;
    }

    @Override
    public AnsObject.AnsList getList() {
        return root.getList();
    }

    @Override
    public AnsObject.AnsMap getMap() {
        return root.getMap();
    }
    
}
