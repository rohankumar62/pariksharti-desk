package in.pariksharti.desk.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "planners")
public class PlannerRecord {
    @Id @Column(name = "user_id", length = 36) String userId;
    @Column(name = "state_json", columnDefinition = "text", nullable = false) String stateJson;

    protected PlannerRecord() {}

    public PlannerRecord(String userId, String stateJson) {
        this.userId = userId;
        this.stateJson = stateJson;
    }

    public String getStateJson() { return stateJson; }
    public void setStateJson(String stateJson) { this.stateJson = stateJson; }
}