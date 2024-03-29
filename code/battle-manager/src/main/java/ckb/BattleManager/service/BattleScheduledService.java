package ckb.BattleManager.service;

import ckb.BattleManager.controller.MakeRepositoryPublicController;
import ckb.BattleManager.controller.SendMailsToParticipants;
import ckb.BattleManager.controller.SendTeamsPointsController;
import ckb.BattleManager.model.Battle;
import ckb.BattleManager.model.Team;
import ckb.BattleManager.repository.BattleRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class BattleScheduledService {
    private final BattleRepository battleRepository;
    private final BattleService battleService;
    private final TeamService teamService;
    private final MakeRepositoryPublicController makeRepositoryPublicController;
    private final SendTeamsPointsController sendTeamsPointsController;
    private final SendMailsToParticipants sendMailsToParticipants;

    @Autowired
    public BattleScheduledService(BattleRepository battleRepository, TeamService teamService,
                                  MakeRepositoryPublicController makeRepositoryPublicController,
                                  SendTeamsPointsController sendTeamsPointsController,
                                  SendMailsToParticipants sendMailsToParticipants,
                                  BattleService battleService) {
        this.battleRepository = battleRepository;
        this.battleService = battleService;
        this.teamService = teamService;
        this.makeRepositoryPublicController = makeRepositoryPublicController;
        this.sendTeamsPointsController = sendTeamsPointsController;
        this.sendMailsToParticipants = sendMailsToParticipants;
    }

    @Scheduled(fixedRate = 3000) // 3 Seconds
    public void startBattles() {
        List<Battle> battlesToStart = battleRepository
                .findBattlesByHasStartedIsFalseAndRegDeadlineIsBefore(LocalDateTime.now());

        for (Battle battle : battlesToStart) {
            List<Team> teamsRegistered = battle.getTeamsRegistered();
            for (Team teamRegistered : teamsRegistered) {
                int numberStudents = teamRegistered.getParticipation().size();
                if (numberStudents < battle.getMinStudents() || numberStudents > battle.getMaxStudents()) {
                    log.error("The number of students in the team {} is {}: not in the range of {} and {}",
                            teamRegistered.getTeamId(), teamsRegistered.size(), battle.getMinStudents(), battle.getMaxStudents());

                    teamRegistered.setCanParticipateToBattle(false);
                }
            }

            battle.setHasStarted(true);
            battleRepository.save(battle);

            try {
                sendMailsToParticipants.send(
                        battleService.getBattleParticipants(battle),
                        "The battle " + battle.getName() + " is started.\nCheck out the code kata following the link: "
                                + battle.getRepositoryLink(),
                        battle.getName()
                );
            } catch (Exception e) {
                log.error("Error sending emails to the participant of the battle {}. Error {}", battle.getName(), e.getMessage());
            }


            log.info("Starting battle with name: {}", battle.getName());

            try {
                // Call the GitHub manager to start the battle
                makeRepositoryPublicController.makeRepositoryPublic(battle.getRepositoryLink());
            } catch (Exception e) {
                log.error("Error making the repository public for battle with name {}. Error {}", battle.getName(), e.getMessage());
            }
        }
    }

    @Scheduled(fixedRate = 3000) // 3 Seconds
    public void closeBattles() {
        List<Battle> battlesToEnd = battleRepository.
                findBattlesByHasEndedIsFalseAndSubDeadlineIsBefore(LocalDateTime.now());

        for (Battle battle : battlesToEnd) {
            battle.setHasEnded(true);
            battleRepository.save(battle);
            log.info("The battle {} has ended", battle.getName());

            if (battle.getBattleToEval().equals(false)) {
                battle.setIsClosed(true);
                log.info("The battle {} has been closed", battle.getName());
                battleRepository.save(battle);
                try {
                    sendMailsToParticipants.send(
                            battleService.getBattleParticipants(battle),
                            "The battle " + battle.getName() + " is finished.\nCheck out the ranking on the website",
                            battle.getName()
                    );
                } catch (Exception e) {
                    log.error("Error sending emails to the participant of the battle {}. Error {}", battle.getName(), e.getMessage());
                    return;
                }

                // Get the teams and the points of each battle
                // send the class containing tournament_id, List<Pair<idTeam, points>>
                // to the Tournament manager
                try {
                    sendTeamsPointsController.sendIdUsersPointsFinishedBattle(
                            battle,
                            teamService.getListPairIdUserPoints(battle)
                    );
                } catch (Exception e) {
                    log.error("Error sending points of the battle {} to the tournament manager. Error {}", battle.getName(), e.getMessage());
                    return;
                }
            }
        }
    }
}
