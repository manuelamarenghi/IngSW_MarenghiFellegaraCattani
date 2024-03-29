package ckb.BattleManager.controller;

import ckb.BattleManager.dto.in.AcceptStudentTeamRequest;
import ckb.BattleManager.model.Battle;
import ckb.BattleManager.model.Participation;
import ckb.BattleManager.model.Team;
import ckb.BattleManager.repository.BattleRepository;
import ckb.BattleManager.repository.ParticipationRepository;
import ckb.BattleManager.repository.TeamRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RegisterStudentToTeamControllerTest {
    private final RegisterStudentToTeamController registerStudentToTeamController;
    private final BattleRepository battleRepository;
    private final TeamRepository teamRepository;
    private final ParticipationRepository participationRepository;
    private Team newTeam;
    private Participation participationOld;

    @Autowired
    public RegisterStudentToTeamControllerTest(RegisterStudentToTeamController registerStudentToTeamController,
                                               TeamRepository teamRepository,
                                               ParticipationRepository participationRepository,
                                               BattleRepository battleRepository) {
        this.registerStudentToTeamController = registerStudentToTeamController;
        this.battleRepository = battleRepository;
        this.teamRepository = teamRepository;
        this.participationRepository = participationRepository;
    }

    @BeforeEach
    void setUp() {
        Battle battle = new Battle();
        battleRepository.save(battle);

        Team oldTeam = new Team();
        oldTeam.setBattle(battle);
        participationOld = new Participation();
        participationOld.setStudentId(1L);
        participationOld.setTeam(oldTeam);
        oldTeam.setParticipation(
                List.of(participationOld)
        );
        teamRepository.save(oldTeam);

        newTeam = new Team();
        newTeam.setBattle(battle);
        teamRepository.save(newTeam);
    }

    @Test
    void registerStudentToTeam() {
        ResponseEntity<Object> response = registerStudentToTeamController.registerStudentToTeam(
                new AcceptStudentTeamRequest(1L, newTeam.getTeamId())
        );

        assertTrue(response.getStatusCode().is2xxSuccessful());
        assertTrue(participationRepository.existsById(participationOld.getId()));
    }

    @Test
    void registerStudentToNonExistingTeam() {
        ResponseEntity<Object> response = registerStudentToTeamController.registerStudentToTeam(
                new AcceptStudentTeamRequest(1L,0L)
        );

        assertTrue(response.getStatusCode().is4xxClientError());
    }

    @AfterEach
    void tearDown() {
        participationRepository.deleteAll();
        teamRepository.deleteAll();
        battleRepository.deleteAll();
    }
}