package ckb.BattleManager.dto.output;

import ckb.BattleManager.model.WorkingPair;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

@Data
@AllArgsConstructor
public class BattleFinishedMessage {
    private Long idTournament;
    private List<WorkingPair<Long, Integer>> pairsIdUserPoints;
}
