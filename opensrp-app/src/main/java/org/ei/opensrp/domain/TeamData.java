package org.ei.opensrp.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.codehaus.jackson.annotate.JsonProperty;
import java.util.List;

/**
 * Created by Dani on 06/10/2017.
 */
public class TeamData {
    @JsonProperty
    private Object person;
    @JsonProperty
    private int teamMemberId;
    @JsonProperty
    private List<TeamLocation> location;
    @JsonProperty
    private Object team;

    public TeamData(Object person,int teamMemberId,List<TeamLocation> location,String team){
        this.person = person;
        this.teamMemberId = teamMemberId;
        this.location = location;
        this.team = team;
    }
    public Object person() {
        return this.person;
    }
    public int teamMemberId() {
        return this.teamMemberId;
    }
    public List<TeamLocation> location() {
        return this.location;
    }
    public String locationName() {
        return this.location.get(0).name();
    }
    public Object team() {
        return this.team;
    }
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

}