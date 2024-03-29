package ckb.BattleManager.controller;

import ckb.BattleManager.dto.in.JoinRequest;
import ckb.BattleManager.dto.in.LeaveRequest;
import ckb.BattleManager.model.Battle;
import ckb.BattleManager.model.Team;
import ckb.BattleManager.repository.BattleRepository;
import ckb.BattleManager.repository.ParticipationRepository;
import ckb.BattleManager.repository.TeamRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class JoinLeaveBattleControllerTest {
    private final JoinLeaveBattleController joinLeaveBattleController;
    private final BattleRepository battleRepository;
    private final TeamRepository teamRepository;
    private final ParticipationRepository participationRepository;

    @Autowired
    public JoinLeaveBattleControllerTest(JoinLeaveBattleController joinLeaveBattleController,
                                         BattleRepository battleRepository, TeamRepository teamRepository,
                                         ParticipationRepository participationRepository) {
        this.joinLeaveBattleController = joinLeaveBattleController;
        this.battleRepository = battleRepository;
        this.teamRepository = teamRepository;
        this.participationRepository = participationRepository;
    }

    @Test
    public void joinBattleLeaveBattle() {
        Battle battle = Battle.builder()
                .tournamentId(1L)
                .name("Battle test")
                .authorId(1L)
                .minStudents(1)
                .maxStudents(1)
                .battleToEval(false)
                .regDeadline(LocalDateTime.now().plusHours(10))
                .subDeadline(LocalDateTime.now().plusHours(20))
                .hasStarted(false)
                .hasEnded(false)
                .isClosed(false)
                .build();
        battleRepository.save(battle);

        // Join
        Long idStudent = 2L;
        ResponseEntity<Object> response = joinLeaveBattleController.joinBattle(new JoinRequest(idStudent, battle.getBattleId()));
        assertTrue(response.getStatusCode().is2xxSuccessful());

        // Leave
        response = joinLeaveBattleController.leaveBattle(new LeaveRequest(idStudent, battle.getBattleId()));
        assertTrue(response.getStatusCode().is2xxSuccessful());
    }

    @Test
    public void wrongJoinAfterRegDeadline() {
        Battle battle = Battle.builder()
                .tournamentId(1L)
                .name("Battle test")
                .authorId(1L)
                .minStudents(1)
                .maxStudents(1)
                .battleToEval(false)
                .regDeadline(LocalDateTime.now().minusHours(10))
                .subDeadline(LocalDateTime.now().minusHours(20))
                .hasStarted(false)
                .hasEnded(false)
                .isClosed(false)
                .build();
        battleRepository.save(battle);

        // Join
        Long idStudent = 1L;
        ResponseEntity<Object> response = joinLeaveBattleController.joinBattle(new JoinRequest(idStudent, battle.getBattleId()));
        assertTrue(response.getStatusCode().is4xxClientError());

        Optional<Team> optionalTeam = teamRepository.findTeamByStudentIdAndBattle(idStudent, battle);

        assertTrue(optionalTeam.isEmpty());
    }

    @Test
    public void wrongLeftWithoutJoin() {
        Battle battle = Battle.builder()
                .tournamentId(1L)
                .name("Battle test")
                .authorId(1L)
                .minStudents(1)
                .maxStudents(1)
                .battleToEval(false)
                .regDeadline(LocalDateTime.now().minusHours(10))
                .subDeadline(LocalDateTime.now().minusHours(20))
                .hasStarted(false)
                .hasEnded(false)
                .isClosed(false)
                .build();
        battleRepository.save(battle);

        long idStudent = 1L;

        // Leave
        ResponseEntity<Object> response = joinLeaveBattleController.leaveBattle(new LeaveRequest(idStudent, battle.getBattleId()));
        assertTrue(response.getStatusCode().is4xxClientError());
    }

    @AfterEach
    void tearDown() {
        assertEquals(0, participationRepository.findAll().size());
        for (Team team : teamRepository.findAll()) {
            assertTrue(team.getParticipation().isEmpty());
        }
        participationRepository.deleteAll();
        teamRepository.deleteAll();
        battleRepository.deleteAll();
    }
}