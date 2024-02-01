package ckb.BattleManager.model;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Participation {
    @EmbeddedId
    private ParticipationId participationId;

    @Override
    public String toString() {
        return participationId.getStudentId().toString();
    }
}
