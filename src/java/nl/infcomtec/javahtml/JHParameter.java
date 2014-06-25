/*
 * Copyright (c) 2014 by Walter Stroebel and InfComTec.
 * All rights reserved.
 */
package nl.infcomtec.javahtml;

import java.util.Arrays;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * Form (and/or Session) JHParameter.
 *
 * @author walter
 */
public class JHParameter {

    /**
     * Prefix used to make a JHParameter semi-permanent by storing it in the
 Session.
     */
    public final static String SESPREFIX = "par_";
    public final String varName;
    public String[] values;
    public final String[] defValues;
    public final boolean wasSet;

    private JHParameter(String varName, String[] values, String[] defValues, boolean wasSet) {
        this.varName = varName;
        this.values = values;
        this.defValues = defValues;
        this.wasSet = wasSet;
    }

    /**
     * Constructor.
     *
     * @param request The request.
     * @param varName Name of the Parameter.
     * @param defValue Default value(s), optional.
     */
    public JHParameter(final HttpServletRequest request, final String varName, final String... defValue) {
        this(null, request, varName, defValue);
    }

    /**
     * Semi-permanent Parameter. If the REQUEST has a parameter NAME, use its
     * VALUES and save it to SESSION. Else if the SESSION has an attribute NAME,
     * use its VALUES. Else use the DEFAULT VALUES.
     *
     * @param session From the request, may be null.
     * @param request The request.
     * @param name Name of the Parameter.
     * @param defValue Default value(s), optional.
     */
    public JHParameter(final HttpSession session, final HttpServletRequest request, final String name, final String... defValue) {
        this.varName = clean(name);
        this.defValues = defValue;
        values = request.getParameterValues(varName);
        if (values != null) {
            wasSet = true;
        } else {
            wasSet = false;
            if (session != null) {
                Object vals = session.getAttribute(SESPREFIX + varName);
                if (vals != null && vals instanceof String[]) {
                    values = (String[]) vals;
                } else {
                    setValues(defValue);
                }
            } else {
                setValues(defValue);
            }
        }
        setInSession(session);
    }

    public final void setInSession(final HttpSession session){
        if (session != null) {
            if (values != null) {
                session.setAttribute(SESPREFIX + varName, values);
            } else {
                session.removeAttribute(SESPREFIX + varName);
            }
        }        
    }
    
    public static JHParameter overrideWasSet(JHParameter source, boolean wasSet) {
        return new JHParameter(source.varName, source.values, source.defValues, wasSet);
    }

    /**
     * Quick test if a JHParameter was not entered and did not have a non-empty
 default. Do not use this to test a "submit" type JHParameter, use wasSet
 instead. Note that a multi-value parameter will be returned as "empty".
     *
     * @return True only if a single-value JHParameter that has a non-empty value.
     */
    public boolean isEmpty() {
        return values == null || (values.length == 1 && values[0].trim().isEmpty());
    }

    /**
     * The inverse of isEmpty().
     *
     * @see isEmpty()
     * @return
     */
    public boolean notEmpty() {
        return values != null && values.length == 1 && !values[0].trim().isEmpty();
    }

    /**
     * Returns if the submitted form contained the specified value for this
     * parameter.
     *
     * @param value The value to check for.
     * @return true if the value was submitted for this parameter.
     */
    public boolean containsValue(String value) {
        if (values != null) {
            for (String s : values) {
                if (s.equals(value)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns if the submitted form contained the specified value for this
     * parameter, regardless of case.
     *
     * @param value The value to check for.
     * @return true if the value was submitted for this parameter, regardless of
     * case.
     */
    public boolean containsValueIgnoreCase(String value) {
        if (values != null) {
            for (String s : values) {
                if (s.equalsIgnoreCase(value)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Return as an integer.
     *
     * @return The integer value or 0 if non-numeric.
     */
    public int getIntValue() {
        return (int) getLongValue();
    }

    /**
     * Return as a long.
     *
     * @return The long value or 0 if non-numeric.
     */
    public long getLongValue() {
        if (isEmpty()) {
            return 0;
        }
        try {
            return Long.valueOf(values[0]);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * Return as a double. Thousand separators are not supported; either a
     * period or a comma may be used as the floating point.
     *
     * @return The double value or 0 if non-numeric.
     */
    public double getDoubleValue() {
        if (isEmpty()) {
            return 0;
        }
        try {
            String v = values[0].replace(',', '.');
            return Double.valueOf(v);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * @return the value
     */
    public String getValue() {
        if (values == null) {
            return null;
        }
        return values[0];
    }

    /**
     * @return the value
     */
    public String quote() {
        return JHFragment.quote(values[0]);
    }

    /**
     * @param values the value to set
     */
    public final void setValues(String... values) {
        this.values = values;
    }

    /**
     * @param value the value to set
     */
    public void setValue(long value) {
        setValues("" + value);
    }

    /*
     * For debugging, normally a JHParameter is not printed.
     * @return the contents of this object with automatic entity encoding.
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("V [name=");
        builder.append(varName);
        builder.append(", values=");
        builder.append(Arrays.toString(values));
        builder.append(", set=");
        builder.append(wasSet);
        builder.append("]");
        return JHFragment.html(builder.toString());
    }

    private String clean(String varName) {
        StringBuilder ret = new StringBuilder();
        for (char c : varName.toCharArray()) {
            if (Character.isJavaIdentifierPart(c)) {
                ret.append(c);
            } else {
                ret.append('_');
            }
        }
        return ret.toString();
    }

    public JHParameter clear() {
        return new JHParameter(varName, defValues, defValues, false);
    }

}
