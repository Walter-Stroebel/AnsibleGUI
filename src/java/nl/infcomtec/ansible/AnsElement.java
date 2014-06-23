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

    public AnsObject.AnsList getList();

    public AnsObject.AnsMap getMap();
    
}
