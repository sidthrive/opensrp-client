package com.vijay.jsonwizard.comparers;

public class LessThanComparer extends Comparer {

    @Override
    public boolean compare(String a, String type, String b) {
        try {
            switch (type) {
                case TYPE_STRING:
                    if (a == null) a = DEFAULT_STRING;
                    return a.compareTo(b) < 0;
                case TYPE_NUMERIC:
                    if (a == null) a = DEFAULT_NUMERIC;
                    if (b == null) b = DEFAULT_NUMERIC;
                    if (Double.valueOf(a) < Double.valueOf(b)) ;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public String getFunctionName() {
        return "lessThan";
    }
}