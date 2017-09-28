package org.ei.opensrp.domain.form;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FormSubmissionCouch {
    @JsonProperty
    private String type;
    @JsonProperty
    private String anmId;
    @JsonProperty
    private String instanceId;
    @JsonProperty
    private String formName;
    @JsonProperty
    private String entityId;
    @JsonProperty
    private long clientVersion;
    @JsonProperty
    private String formDataDefinitionVersion;
    @JsonProperty
    private FormInstanceCouch formInstance;
    @JsonProperty
    private long serverVersion;
    @JsonProperty
    private Map<String, Object> metadata;

    public FormSubmissionCouch() {
    }

    public FormSubmissionCouch(String type, String anmId, String instanceId, String formName, String entityId, long clientVersion, String formDataDefinitionVersion, FormInstanceCouch formInstance, long serverVersion) {
        this.instanceId = instanceId;
        this.type = type;
        this.formName = formName;
        this.anmId = anmId;
        this.clientVersion = clientVersion;
        this.entityId = entityId;
        this.formInstance = formInstance;
        this.serverVersion = serverVersion;
        this.formDataDefinitionVersion = formDataDefinitionVersion;
    }

    public FormSubmissionCouch(String type, String anmId, String instanceId, String formName, String entityId, String formDataDefinitionVersion, long clientVersion, FormInstanceCouch formInstance) {
        this(type, anmId, instanceId, formName, entityId, clientVersion, formDataDefinitionVersion, formInstance, 0L);
    }

    public String type() {
        return this.type;
    }
    public String anmId() {
        return this.anmId;
    }

    public String instanceId() {
        return this.instanceId;
    }

    public String entityId() {
        return this.entityId;
    }

    public String formName() {
        return this.formName;
    }

    public String bindType() {
        return formInstance.bindType();
    }

    public String defaultBindPath() {
        return formInstance.defaultBindPath();
    }

    public FormInstanceCouch instance() {
        return formInstance;
    }

    public long clientVersion() {
        return this.clientVersion;
    }

    public String  formDataDefinitionVersion() {
        return this.formDataDefinitionVersion;
    }

    public long serverVersion() {
        return serverVersion;
    }

    public void setServerVersion(long serverVersion) {
        this.serverVersion = serverVersion;
    }

    public String getField(String name) {
        return formInstance.getField(name);
    }

    public Map<String, String> getFields(List<String> fieldNames) {
        Map<String, String> fieldsMap = new HashMap<>();
        for (String fieldName : fieldNames) {
            fieldsMap.put(fieldName, getField(fieldName));
        }
        return fieldsMap;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public Object getMetadata(String key) {
        if(metadata == null){
            return null;
        }
        return metadata.get(key);
    }

    void addMetadata(String key, Object value) {
        if(metadata == null){
            metadata = new HashMap<>();
        }
        metadata.put(key, value);
    }

    void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    public FormSubmissionCouch forSubmission(long serverVersion){
        this.serverVersion = serverVersion;
        this.instance().form().getFieldsAsMap();
        return this;
    }

    @Override
    public boolean equals(Object o) {
        return EqualsBuilder.reflectionEquals(o, this);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this, "id");
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
