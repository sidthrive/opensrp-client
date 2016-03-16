package org.ei.opensrp.clientandeventmodel;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

public class Obs {

    @JsonProperty
    private String fieldType;
    @JsonProperty
    private String fieldDataType;
    @JsonProperty
    private String fieldCode;
    @JsonProperty
    private String parentCode;
    @JsonProperty
    private List<Object> values;
    @JsonProperty
    private String comments;
    @JsonProperty
    private String formSubmissionField;

    public Obs() { }

    public Obs(String fieldType, String fieldDataType, String fieldCode, String parentCode,
               List<Object> values, String comments, String formSubmissionField) {
        this.setFieldType(fieldType);
        this.fieldDataType = fieldDataType;
        this.fieldCode = fieldCode;
        this.parentCode = parentCode;
        this.values = values;
        this.comments = comments;
        this.formSubmissionField = formSubmissionField;
    }

    public String getFieldType() {
        return fieldType;
    }

    public void setFieldType(String fieldType) {
        this.fieldType = fieldType;
    }

    public String getFieldDataType() {
        return fieldDataType;
    }

    public void setFieldDataType(String fieldDataType) {
        this.fieldDataType = fieldDataType;
    }

    public String getFieldCode() {
        return fieldCode;
    }

    public void setFieldCode(String fieldCode) {
        this.fieldCode = fieldCode;
    }

    public String getParentCode() {
        return parentCode;
    }

    public void setParentCode(String parentCode) {
        this.parentCode = parentCode;
    }

    @JsonIgnore
    public Object getValue() {
        if(values.size() > 1){
            throw new RuntimeException("Multiset values can not be handled like single valued fields. Use function getValues");
        }
        if(values == null || values.size() == 0){
            return null;
        }

        return values.get(0);
    }

    @JsonIgnore
    public void setValue(Object value) {
        addToValueList(value);
    }

    public List<Object> getValues() {
        return values;
    }

    public void setValues(List<Object> values) {
        this.values = values;
    }


    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public String getFormSubmissionField() {
        return formSubmissionField;
    }

    public void setFormSubmissionField(String formSubmissionField) {
        this.formSubmissionField = formSubmissionField;
    }

    public Obs withFieldType(String fieldType) {
        this.fieldType = fieldType;
        return this;
    }

    public Obs withFieldDataType(String fieldDataType) {
        this.fieldDataType = fieldDataType;
        return this;
    }

    public Obs withFieldCode(String fieldCode) {
        this.fieldCode = fieldCode;
        return this;
    }

    public Obs withParentCode(String parentCode) {
        this.parentCode = parentCode;
        return this;
    }

    public Obs withValue(Object value) {
        return addToValueList(value);
    }

    public Obs withValues(List<Object> values) {
        this.values = values;
        return this;
    }

    public Obs addToValueList(Object value) {
        if(values == null){
            values = new ArrayList<>();
        }
        values.add(value);
        return this;
    }

    public Obs withComments(String comments) {
        this.comments = comments;
        return this;
    }

    public Obs withFormSubmissionField(String formSubmissionField) {
        this.formSubmissionField = formSubmissionField;
        return this;
    }
}

