package ckb.TournamentManager.service;

import ckb.TournamentManager.dto.GetTournamentPageRequest;
import ckb.TournamentManager.dto.NewTournamentRequest;
import ckb.TournamentManager.dto.PermissionRequest;
import ckb.TournamentManager.dto.SubscriptionRequest;
import ckb.TournamentManager.model.Permission;
import ckb.TournamentManager.model.Tournament;
import ckb.TournamentManager.model.TournamentRanking;
import ckb.TournamentManager.repo.PermissionRepo;
import ckb.TournamentManager.repo.TournamentRankingRepo;
import ckb.TournamentManager.repo.TournamentRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor

public class TournamentService {
    private final TournamentRepo tournamentRepo;
    private final TournamentRankingRepo tournamentRankingRepo;
    private final PermissionRepo permissionRepo;

    public String createTournament(NewTournamentRequest request) {
        Tournament tournament = new Tournament();
        tournament.setRegdeadline(request.getRegdeadline());
        tournament.setStatus(true);
        tournamentRepo.save(tournament);
        String tournamentUrl = "http://tournament-service/tournaments/" + tournament.getTournamentID();
        return tournamentUrl;
    }

    public Tournament getTournament(Long id) {
        return tournamentRepo.findByTournamentID(id).orElse(null);
    }

    public void deleteTournament(Long id) {
        tournamentRepo.deleteById(id);
    }
    public boolean isSubscribed(Long tournamentID, Long userID){
        return tournamentRankingRepo.findByTournamentIDAndUserID(tournamentID,userID).isPresent();
    }

    public void addSubscription(SubscriptionRequest request) {
        Tournament tournament = tournamentRepo.findByTournamentID(request.getTournamentID()).orElse(null);
        TournamentRanking ranking = new TournamentRanking();
        if (tournament == null) return;
        ranking.setTournamentID(request.getTournamentID());
        ranking.setScore(0);
        ranking.setUserID(request.getUserID());
        tournamentRankingRepo.save(ranking);
    }

    public String addPermission(PermissionRequest request) {
        Permission p = new Permission(request.getTournamentID(), request.getUserID());
        permissionRepo.save(p);
        String tournamentUrl = "http://localhost:8084/tournament-service/tournaments/" + request.getTournamentID();
        return tournamentUrl;
    }

    public List<TournamentRanking> getTournamentPage(GetTournamentPageRequest request) {
        List<TournamentRanking> rankings = tournamentRankingRepo.findByTournamentIDOrderByScoreAsc(request.getTournamentID());
        System.out.println("res"+rankings);
        return rankings;
    }

    public void closeTournament(Long tournamentId) {
        Tournament tournament = tournamentRepo.findByTournamentID(tournamentId).orElse(null);
        tournament.setStatus(false);
        tournamentRepo.save(tournament);
    }

}
