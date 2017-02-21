package util;

import org.ei.opensrp.AllConstants;

/**
 * Created by coder on 2/14/17.
 */
public class PathConstants extends AllConstants{
    public static String openmrsUrl(){
        String baseUrl= org.ei.opensrp.Context.getInstance().allSharedPreferences().fetchBaseURL("");
        int lastIndex=baseUrl.lastIndexOf("/");
        baseUrl=baseUrl.substring(0,lastIndex)+"/openmrs";
        return baseUrl;
    }
    public static final String OPENMRS_IDGEN_URL = "/module/idgen/exportIdentifiers.form";
    public static final int OPENMRS_UNIQUE_ID_INITIAL_BATCH_SIZE =250;
    public static final int OPENMRS_UNIQUE_ID_BATCH_SIZE =100;
    public static final int OPENMRS_UNIQUE_ID_SOURCE =2;


}
