/*
 * Copyright (c) 2014 by Walter Stroebel and InfComTec.
 * All rights reserved.
 */
package nl.infcomtec.ansible;

import java.io.IOException;
import java.io.Writer;

/**
 *
 * @author walter
 */
class MyWriter extends Writer {

    private final StringBuilder out;

    public MyWriter() {
        out = new StringBuilder();
    }

    @Override
    public void write(char[] cbuf, int off, int len) throws IOException {
        out.append(cbuf, off, len);
    }

    @Override
    public void flush() throws IOException {
    }

    @Override
    public void close() throws IOException {
        for (int dash = out.indexOf("\n-"); dash != -1; dash = out.indexOf("\n-", dash)) {
            out.insert(dash, '\n');
            dash += 3;
        }
    }

    @Override
    public String toString() {
        return out.toString();
    }

}
