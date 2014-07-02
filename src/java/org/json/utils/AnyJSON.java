/**
 * Some utility code for using the JSON classes.
 */
package org.json.utils;

import java.io.Writer;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

/**
 * Holds either a JSONObject or JSONArray.
 *
 * @author walter
 */
public class AnyJSON {

    private final Object any;

    /**
     * Convert to either array or object, as appropriate.
     *
     * @param any Object to make JSON.
     */
    public AnyJSON(final Object any) {
        this.any = JSONObject.wrap(any);
        if (this.any == null) {
            return;
        }
        if (this.any instanceof JSONObject) {
            return;
        }
        if (this.any instanceof JSONArray) {
            return;
        }
        throw new JSONException("JSONObject.wrap could not handle " + any.getClass().getName());
    }

    @Override
    public String toString() {
        if (any == null) {
            return "null";
        }
        if (any instanceof JSONObject) {
            return getObject().toString();
        }
        if (any instanceof JSONArray) {
            return getArray().toString();
        }
        return any.toString();
    }

    /**
     * Get either an object or an array from the JSON in a string.
     *
     * @param source String with JSON.
     */
    public AnyJSON(final String source) {
        final String trim = source.trim();
        if (trim.startsWith("[")) {
            any = new JSONArray(trim);
        } else {
            any = new JSONObject(trim);
        }
    }

    /**
     * Get either an object or an array from the JSON in a JSONTokener.
     *
     * @param toker JSONTokener with JSON data.
     */
    public AnyJSON(final JSONTokener toker) {
        char c = toker.next();
        toker.back();
        if (c == '[') {
            any = new JSONArray(toker);
        } else {
            any = new JSONObject(toker);
        }
    }

    /**
     *
     * @return JSONObject if this is one, else null.
     */
    public JSONObject getObject() {
        if (any instanceof JSONObject) {
            return (JSONObject) any;
        }
        return null;
    }

    /**
     *
     * @return JSONArray if this is one, else null.
     */
    public JSONArray getArray() {
        if (any instanceof JSONArray) {
            return (JSONArray) any;
        }
        return null;
    }

    /**
     *
     * @return false if null or not a JSONObject.
     */
    public boolean isObject() {
        return ((any != null) && (any instanceof JSONObject));
    }

    /**
     *
     * @return false if null or not a JSONObject.
     */
    public boolean isArray() {
        return ((any != null) && (any instanceof JSONArray));
    }

    /**
     * @return the any
     */
    public Object getAny() {
        return any;
    }

    /**
     * Write the contents of the JSONArray as JSON text to a writer. For
     * compactness, no whitespace is added.
     * <p>
     * Warning: This method assumes that the data structure is acyclical.
     *
     * @param writer Where to write.
     * @return The writer.
     * @throws JSONException
     */
    public Writer write(Writer writer) throws JSONException {
        return this.write(writer, 0, 0);
    }

    /**
     * Write the contents of the JSONArray as JSON text to a writer. For
     * compactness, no whitespace is added.
     * <p>
     * Warning: This method assumes that the data structure is acyclical.
     *
     * @param writer Where to write.
     * @param indentFactor
     *            The number of spaces to add to each level of indentation.
     * @param indent
     *            The indention of the top level.
     * @return The writer.
     * @throws JSONException
     */
    public Writer write(Writer writer, int indentFactor, int indent)
            throws JSONException {
        if (isArray()) return getArray().write(writer, indentFactor, indent);
        if (isObject()) return getObject().write(writer, indentFactor, indent);
        throw new JSONException("Cannot write "+any.getClass().getName());
    }

}
