package util.uniqueIDGenerator;

import org.ei.opensrp.Context;
import org.ei.opensrp.util.Cache;
import java.util.List;

/**
 * Created by Null on 2016-10-13.
 */
public class Generator {
    private UniqueIdRepository uniqueIdRepository;
    private Cache<List<Long>> uIdsCache;
    private AllSettingsINA allSettingsINA;
    private UniqueIdController uniqueIdController;
    private UniqueIdService uniqueIdService;
    private Context context;
    public String status;

    public Generator(Context context){
        this.context=context;
    }

    public AllSettingsINA allSettingsINA() {
        context.initRepository();
        if(allSettingsINA == null)
            allSettingsINA = new AllSettingsINA(context.allSharedPreferences(), context.settingsRepository());

        return allSettingsINA;
    }
    public Cache<List<Long>> uIdsCache() {
        if (uIdsCache == null)
            uIdsCache = new Cache<>();
        return uIdsCache;
    }
    public UniqueIdRepository uniqueIdRepository() {
        if(uniqueIdRepository==null)
            uniqueIdRepository = new UniqueIdRepository(context.applicationContext());
        return uniqueIdRepository;
    }
    public UniqueIdController uniqueIdController() {
        if(uniqueIdController == null)
            uniqueIdController = new UniqueIdController(uniqueIdRepository(), allSettingsINA(), uIdsCache());
        return uniqueIdController;
        }
    public UniqueIdService uniqueIdService() {
        if(uniqueIdService == null)
            uniqueIdService = new UniqueIdService(context.httpAgent(), context.configuration(), uniqueIdController(), allSettingsINA(), context.allSharedPreferences());
        return uniqueIdService;
    }

    public void showStatus(){
        System.out.println(status);
    }

}
