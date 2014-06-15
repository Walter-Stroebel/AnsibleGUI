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
public class Task {
public final String name;
    public final File file;
    public final Map map;

    public Task(String name, File aTask, Map t) {
        this.name=name;
        this.file = aTask;
        this.map = t;
    }

}
