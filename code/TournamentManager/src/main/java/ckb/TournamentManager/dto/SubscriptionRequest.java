package ckb.TournamentManager.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor

public class SubscriptionRequest {
    private Long tournamentId;
    private Long userId;
}
