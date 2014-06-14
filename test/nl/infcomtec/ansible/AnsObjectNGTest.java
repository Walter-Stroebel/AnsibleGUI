/*
 * Copyright (c) 2014 by Walter Stroebel and InfComTec.
 * All rights reserved.
 */
package nl.infcomtec.ansible;

import com.esotericsoftware.yamlbeans.YamlException;
import static org.testng.Assert.*;
import org.testng.annotations.Test;

/**
 *
 * @author walter
 */
public class AnsObjectNGTest {

    private static final String testYAML = "name: Nathan Sweet\n"
            + "age: 28\n"
            + "address: 4011 16th Ave S\n"
            + "phone numbers:\n"
            + " - name: Home\n"
            + "   number: 206-555-5138\n"
            + " - name: Work\n"
            + "   number: 425-555-2306\n";

    public AnsObjectNGTest() {
    }

    @Test
    public void testSomeMethod() throws YamlException {
        AnsObject ao = new AnsObject(null, null, testYAML);
        System.out.println(ao);
        assertEquals(ao.getMap().get("address").toString(), "4011 16th Ave S");
        System.out.println(ao.getMap().get("phone numbers"));
    }

}
