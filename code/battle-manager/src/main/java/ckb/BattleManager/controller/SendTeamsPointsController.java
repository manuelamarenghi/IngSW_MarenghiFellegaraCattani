package ckb.BattleManager.controller;

import ckb.BattleManager.dto.out.UpdateScoreRequest;
import ckb.BattleManager.model.Battle;
import ckb.BattleManager.model.WorkingPair;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@RestController
@Slf4j
public class SendTeamsPointsController extends Controller {
    private final WebClient webClient = WebClient.create();

    public void sendIdUsersPointsFinishedBattle(Battle battle, List<WorkingPair<Long, Integer>> pairsIdUserPoints) throws Exception {
        ResponseEntity<String> response = webClient.post()
                .uri(tournamentManagerUri + "/api/tournament/update-score")
                .bodyValue(
                        new UpdateScoreRequest(
                                battle.getTournamentId(),
                                pairsIdUserPoints
                        )
                )
                .retrieve()
                .toEntity(String.class)
                .block();

        if (response == null) {
            log.error("Error sending idUsers and points of the finished battle with id: {}. The response is null", battle.getBattleId());
            throw new Exception("Error sending idUsers and points of the finished battle with id: " + battle.getBattleId());
        }

        if (!response.getStatusCode().is2xxSuccessful()) {
            log.error("Error sending idUsers and points of the finished battle with id: {}. Error {}", battle.getBattleId(), response.getStatusCode());
            throw new Exception("Error sending idUsers and points of the finished battle with id: " + battle.getBattleId());
        }

        log.info("Successfully sent IdUsers and points of the finished battle with id: {}", battle.getBattleId());
    }

}
