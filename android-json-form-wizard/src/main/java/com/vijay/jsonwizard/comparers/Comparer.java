package com.vijay.jsonwizard.comparers;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Jason Rogena - jrogena@ona.io
 * @since 09/02/2017
 */
public abstract class Comparer {
    protected static final String TYPE_STRING = "string";
    protected static final String TYPE_NUMERIC = "numeric";
    protected static final String DEFAULT_STRING = "";
    protected static final String DEFAULT_NUMERIC = "0";

    public abstract boolean compare(String a, String type, String b);

    public abstract String getFunctionName();


}
