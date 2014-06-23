/*
 * Copyright (c) 2014 by Walter Stroebel and InfComTec.
 * All rights reserved.
 */
package nl.infcomtec.ansible;

import java.io.File;
import java.util.Map;

/**
 *
 * @author walter
 */
public class RoleFileMap {
    public final File file;
    public final AnsObject.AnsMap map;

    public RoleFileMap(File theFile, AnsObject.AnsMap theMap) {
        this.file = theFile;
        this.map = theMap;
    }

}
