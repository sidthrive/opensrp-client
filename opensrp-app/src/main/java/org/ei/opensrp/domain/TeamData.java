package org.ei.opensrp.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.codehaus.jackson.annotate.JsonProperty;
import java.util.List;

/**
 * Created by Dani on 06/10/2017.
 */
public class TeamData {
    @JsonProperty
    private String person;
    @JsonProperty
    private String teamMemberId;
    @JsonProperty
    private List<TeamLocation> location;
    @JsonProperty
    private String team;

    public TeamData(String person,String teamMemberId,List<TeamLocation> location,String team){
        this.person = person;
        this.teamMemberId = teamMemberId;
        this.location = location;
        this.team = team;
    }
    public String person() {
        return this.person;
    }
    public String teamMemberId() {
        return this.teamMemberId;
    }
    public List<TeamLocation> location() {
        return this.location;
    }
    public String locationName() {
        return this.location.get(0).name();
    }
    public String team() {
        return this.team;
    }
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

}
