package org.ei.opensrp.commonregistry;


import android.content.ContentValues;

import org.apache.commons.lang3.StringUtils;
import org.ei.opensrp.repository.AlertRepository;
import org.ei.opensrp.repository.TimelineEventRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Raihan Ahmed on 2/12/15.
 */
public class AllCommonsRepository {
    private CommonRepository personRepository;
    private final TimelineEventRepository timelineEventRepository;
    private final AlertRepository alertRepository;

    public AllCommonsRepository(CommonRepository personRepository, AlertRepository alertRepository, TimelineEventRepository timelineEventRepository) {
        this.personRepository = personRepository;
        this.timelineEventRepository = timelineEventRepository;
        this.alertRepository = alertRepository;
    }

    public List<CommonPersonObject> all() {
        return personRepository.allcommon();
    }

    public CommonPersonObject findByCaseID(String caseId) {
        return personRepository.findByCaseID(caseId);
    }

    public long count() {
        return personRepository.count();
    }




    public List<CommonPersonObject> findByCaseIDs(List<String> caseIds) {
        return personRepository.findByCaseIDs(caseIds.toArray(new String[caseIds.size()]));
    }

    public List<CommonPersonObject> findByRelationalIDs(List<String> RelationalID) {
        return personRepository.findByRelationalIDs(RelationalID.toArray(new String[RelationalID.size()]));
    }

    public void close(String entityId) {
        alertRepository.deleteAllAlertsForEntity(entityId);
        timelineEventRepository.deleteAllTimelineEventsForEntity(entityId);
        personRepository.close(entityId);
    }

    public void mergeDetails(String entityId, Map<String, String> details) {
        personRepository.mergeDetails(entityId, details);
    }

    public void update(String tableName,ContentValues contentValues,String caseId){
        personRepository.updateColumn(tableName, contentValues, caseId);
    }

    public List<CommonPersonObject> customQuery(String sql , String[] selections , String tableName){
        return personRepository.customQuery(sql,selections,tableName);
    }

    public List<CommonPersonObject> customQueryForCompleteRow(String sql , String[] selections , String tableName){
        return personRepository.customQueryForCompleteRow(sql, selections, tableName);
    }

    public List<String> updateSearch(List<String> caseIds) {
        List<String> remainingIds = new ArrayList<>();
        if(caseIds == null || caseIds.isEmpty()){
            return remainingIds;
        }
        Map<String, ContentValues> searchMap = new HashMap<String, ContentValues>();
        for(String caseId: caseIds){
            ContentValues contentValues = personRepository.populateSearchValues(caseId);
            if(contentValues != null){
                searchMap.put(caseId, contentValues);
            }else{
                remainingIds.add(caseId);
            }
        }

        if(!searchMap.isEmpty()){
            personRepository.searchBatchInserts(searchMap);
        }

        return remainingIds;
    }

    public boolean updateSearch(String caseId) {
        if(StringUtils.isBlank(caseId)){
            return false;
        }
        Map<String, ContentValues> searchMap = new HashMap<String, ContentValues>();
        ContentValues contentValues = personRepository.populateSearchValues(caseId);
        if(contentValues != null){
            searchMap.put(caseId, contentValues);
        }else{
            return false;
        }

        if(!searchMap.isEmpty()){
            personRepository.searchBatchInserts(searchMap);
        }

        return true;
    }

    public boolean updateSearch(String caseId, String field, String value, String[] listToRemove) {
        if(StringUtils.isBlank(caseId)){
            return false;
        }

        return personRepository.populateSearchValues(caseId, field, value, listToRemove);

    }
}
