package ckb.dto.tournament;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class GetAllTournamentsRequest {
    String request;
}
