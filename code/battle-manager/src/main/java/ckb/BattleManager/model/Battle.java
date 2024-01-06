package ckb.BattleManager.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Entity(name = "Battle")
@Table(name = "Battles", uniqueConstraints = {
        @UniqueConstraint(name = "repo_link_unique", columnNames = "repositoryLink")
})
@Data
@NoArgsConstructor
public class Battle {
    @Id
    @Column(name = "battleId", nullable = false, updatable = false)
    private Long battleId;

    @Column(name = "tournamentId", nullable = false, updatable = false)
    private Long tournamentId;

    @Column(name = "repositoryLink")
    private String repositoryLink;

    @Column(name = "minStudents", nullable = false, updatable = false)
    private int minStudents;

    @Column(name = "maxStudents", nullable = false, updatable = false)
    private int maxStudents;

    @Column(name = "registrationDeadline", nullable = false, updatable = false, columnDefinition = "DATE")
    private Date regDeadline;

    @Column(name = "submissionDeadline", nullable = false, updatable = false, columnDefinition = "DATE")
    private Date subDeadline;

    @Column(name = "battleToEval", nullable = false, updatable = false)
    private boolean battleToEval;

}
