/*
 * Copyright (c) 2014 by Walter Stroebel and InfComTec.
 * All rights reserved.
 */
package nl.infcomtec.ansible;

import static org.testng.Assert.*;
import org.testng.annotations.Test;

/**
 *
 * @author walter
 */
public class AnsObjectNGTest {

    private static final String testYAML = "---\n"
            + "# a comment\n"
            + "\n"
            + "name: Nathan Sweet # The author\n"
            + "age: 28\n"
            + "address: 4011 16th\n"
            + " Ave S # wonder if this is the real address\n"
            + "phone numbers:\n"
            + " - name: Home\n"
            + "   number: 206-555-5138\n"
            + " - name: Work\n"
            + "   number: 425-555-2306\n";

    public AnsObjectNGTest() {
    }

    @Test
    public void testSomeMethod() throws Exception {
        AnsObject ao = new AnsObject(null, null, testYAML);
        System.out.println(ao.makeString());
        assertEquals(ao.getMap().get(new AnsObject.AnsString("address")).getString(), "4011 16th Ave S");
        System.out.println(ao.getMap().get(new AnsObject.AnsString("phone numbers")));
    }

}
