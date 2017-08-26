package org.ei.opensrp.domain.form;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FormDataCouch {
    @JsonProperty
    private String bind_type;
    @JsonProperty
    private String default_bind_path;
    @JsonProperty
    private List<FormField> fields;
    @JsonProperty
    private List<SubFormData> sub_forms;

    @JsonIgnore
    private Map<String, String> fieldsAsMap;

    public FormDataCouch() {
    }

    public FormDataCouch(String bind_type, String default_bind_path, List<FormField> fields, List<SubFormData> sub_forms) {
        this.bind_type = bind_type;
        this.default_bind_path = default_bind_path;
        this.fields = fields;
        this.sub_forms = sub_forms;
    }

    public List<FormField> fields() {
        return fields;
    }

    public String getField(String name) {
        if (fieldsAsMap == null) {
            createFieldMapByName();
        }
        return fieldsAsMap.get(name);
    }

    public String bindType() {
        return bind_type;
    }

    public String defaultBindPath() {
        return default_bind_path;
    }

    private void createFieldMapByName() {
        fieldsAsMap = new HashMap<>();
        for (FormField field : fields) {
            fieldsAsMap.put(field.name(), field.value());
        }
    }

    public Map<String, String> getFieldsAsMap() {
        if (fieldsAsMap == null) {
            createFieldMapByName();
        }
        return fieldsAsMap;
    }

    public SubFormData getSubFormByName(String name) {
        for (SubFormData sub_form : sub_forms) {
            if (StringUtils.equalsIgnoreCase(name, sub_form.name()))
                return sub_form;
        }
        throw new RuntimeException(MessageFormat.format("No sub form with the given name: {0}, in formData: {1}", name, this));
    }

    public List<SubFormData> subForms() {
        return sub_forms;
    }

    @Override
    public boolean equals(Object o) {
        return EqualsBuilder.reflectionEquals(this, o);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}