package com.vijay.jsonwizard.comparers;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexComparer extends Comparer {
    @Override
    public boolean compare(String a, String type, String b) {
        try {
            Pattern pattern = Pattern.compile(b);
            Matcher matcher = pattern.matcher(a);
            return matcher.matches();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public String getFunctionName() {
        return "regex";
    }
}