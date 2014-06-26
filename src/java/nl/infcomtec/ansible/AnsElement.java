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
public interface AnsElement {

    public String getString();

    public AnsList getList();

    public AnsMap getMap();

    /**
     * Find the first named element with this name and return it's text, if any.
     *
     * @param name Name to find, eg 'description';
     * @return getString() for the element ... note that this might be null even
     * if there is a named element that matches.
     */
    public String getStringFor(String name);

}
