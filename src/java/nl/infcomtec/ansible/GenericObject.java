/*
 * Copyright (c) 2014 by Walter Stroebel and InfComTec.
 * All rights reserved.
 */
package nl.infcomtec.ansible;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Either a String, a JSONObject or a JSONArray.
 *
 * @author walter
 */
public class GenericObject {

    public final String string;
    public final JSONObject object;
    public final JSONArray array;

    /**
     * Make it a String
     */
    public GenericObject(String string) {
        this.string = string;
        this.object = null;
        this.array = null;
    }

    /**
     * Make it a JSONObject
     */
    public GenericObject(JSONObject object) {
        this.string = null;
        this.object = object;
        this.array = null;
    }

    /**
     * Make it a JSONArray
     */
    public GenericObject(JSONArray array) {
        this.string = null;
        this.object = null;
        this.array = array;
    }

    /**
     * Determine what this is from some object fetched from JSON.
     */
    public GenericObject(Object object) {
        if (object instanceof JSONObject) {
            this.string = null;
            this.object = (JSONObject) object;
            this.array = null;
        } else if (object instanceof JSONArray) {
            this.string = null;
            this.object = null;
            this.array = (JSONArray) object;
        } else {
            this.string = object.toString();
            this.object = null;
            this.array = null;
        }
    }

    /**
     * Make whatever this is an Object
     */
    public Object toObject() {
        if (string != null) {
            return string;
        }
        if (object != null) {
            return object;
        }
        return array;
    }

    @Override
    public String toString() {
        return toObject() == null ? "null" : toObject().toString();
    }
}
