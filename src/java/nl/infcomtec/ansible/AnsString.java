/*
 * Copyright (c) 2014 by Walter Stroebel and InfComTec.
 * All rights reserved.
 */

package nl.infcomtec.ansible;

/**
 *
 * @author walter
 */
public class AnsString implements AnsElement, Comparable<AnsString> {
    final String s;

    public AnsString(String s) {
        this.s = s;
    }

    @Override
    public String toString() {
        throw new RuntimeException("I don't think you want this");
        //return "\nAnsString{" + "s=" + s + '}';
    }

    /**
     * @return the s
     */
    @Override
    public String getString() {
        return s;
    }

    @Override
    public int compareTo(AnsString o) {
        return s.compareTo(o.s);
    }

    @Override
    public AnsList getList() {
        return null;
    }

    @Override
    public AnsMap getMap() {
        return null;
    }
    
}
