/*
 * Copyright (c) 2014 by Walter Stroebel and InfComTec.
 * All rights reserved.
 */

package nl.infcomtec.ansible;

import java.io.File;

/**
 *
 * @author walter
 */
public class RoleFileString {
    public final File file;
    public final String str;

    public RoleFileString(File f, String s) {
        file=f;
        str=s;
    }
    
}
