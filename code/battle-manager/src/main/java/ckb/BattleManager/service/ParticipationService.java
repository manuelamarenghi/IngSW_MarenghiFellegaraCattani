package ckb.BattleManager.service;

import ckb.BattleManager.model.Participation;
import ckb.BattleManager.model.ParticipationId;
import ckb.BattleManager.model.Team;
import ckb.BattleManager.repository.ParticipationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ParticipationService {
    private final ParticipationRepository participationRepository;
    private final TeamService teamService;

    @Autowired
    public ParticipationService(ParticipationRepository participationRepository, @Lazy TeamService teamService) {
        this.participationRepository = participationRepository;
        this.teamService = teamService;
    }

    public void registerStudentToTeam(Long idTeam, Long idStudent) {
        // delete the record in participation and if the team has 0 members
        // delete the team, also insert a new line in participation

    }

    public void createParticipation(Long studentId, Team team) {
        participationRepository.save(
                new Participation(
                        new ParticipationId(
                                studentId, team
                        )
                )
        );
    }

    // Delete participation and the team if the team has 0 members
    public void deleteParticipationHavingIdBattle(Long idStudent, Long idBattle) throws Exception {
        Optional<Team> studentTeam = participationRepository.findTeamByBattleIdAndStudentId(idStudent, idBattle);

        // delete the record in participation
        Optional<Participation> participation = participationRepository.findById(
                new ParticipationId(
                        idStudent, studentTeam.orElseThrow(
                        () -> new Exception("Participation not found")
                )
                )
        );

        if(participation.isPresent()) {
            participationRepository.delete(participation.get());
            if (participationRepository.existsParticipationByParticipationId_TeamId(studentTeam.get())) {
                teamService.deleteTeam(studentTeam.get().getTeamId());
            }
        }
    }

    public void deleteParticipationHavingIdTeam(Long idStudent, Team team) {
        participationRepository.deleteById(
                new ParticipationId(
                        idStudent, team
                )
        );
    }
}
