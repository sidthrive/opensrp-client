package org.ei.opensrp.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 * Created by Dani on 06/10/2017.
 */
public class TeamLocation {
    @JsonProperty
    private String name;

    public TeamLocation(String name){
        this.name = name;
    }
    public String name() {
        return this.name;
    }
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
