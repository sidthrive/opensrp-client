package org.ei.opensrp.domain.form;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.codehaus.jackson.annotate.JsonProperty;

public class FormSubmissionDTO {
    @JsonProperty
    private String type;
    @JsonProperty
    private String anmId;
    @JsonProperty
    private String instanceId;
    @JsonProperty
    private String entityId;
    @JsonProperty
    private String formName;
    @JsonProperty
    private String locationId;
    @JsonProperty
    private String formInstance;
    @JsonProperty
    private String clientVersion;
    @JsonProperty
    private String serverVersion;
    @JsonProperty
    private String formDataDefinitionVersion;

    public FormSubmissionDTO(String type, String anmId, String instanceId, String entityId, String formName, String locationId, String formInstance, String clientVersion, String formDataDefinitionVersion) {
        this.anmId = anmId;
        this.type = type;
        this.instanceId = instanceId;
        this.entityId = entityId;
        this.formName = formName;
        this.locationId = locationId;
        this.formInstance = formInstance;
        this.clientVersion = clientVersion;
        this.formDataDefinitionVersion = formDataDefinitionVersion;
    }

    public FormSubmissionDTO(String anmId, String instanceId, String entityId, String formName, String locationId, String formInstance, String clientVersion, String formDataDefinitionVersion) {
        this.anmId = anmId;
        this.instanceId = instanceId;
        this.entityId = entityId;
        this.formName = formName;
        this.locationId = locationId;
        this.formInstance = formInstance;
        this.clientVersion = clientVersion;
        this.formDataDefinitionVersion = formDataDefinitionVersion;
    }

    public FormSubmissionDTO withServerVersion(Long version) {
        this.serverVersion = version.toString();
        return this;
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

    public String locationId() {
        return this.locationId;
    }

    public String instance() {
        return this.formInstance;
    }

    public String clientVersion() {
        return this.clientVersion;
    }

    public String formDataDefinitionVersion() {
        return this.formDataDefinitionVersion;
    }

    public String serverVersion() {
        return this.serverVersion;
    }

    public boolean equals(Object o) {
        return EqualsBuilder.reflectionEquals(this, o, new String[0]);
    }

    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this, new String[0]);
    }

    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
