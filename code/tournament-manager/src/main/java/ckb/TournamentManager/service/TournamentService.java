package ckb.TournamentManager.service;

import ckb.TournamentManager.dto.incoming.*;
import ckb.TournamentManager.model.Permission;
import ckb.TournamentManager.model.Tournament;
import ckb.TournamentManager.model.TournamentRanking;
import ckb.TournamentManager.model.WorkingPair;
import ckb.TournamentManager.repo.PermissionRepo;
import ckb.TournamentManager.repo.TournamentRankingRepo;
import ckb.TournamentManager.repo.TournamentRepo;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
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

    public Tournament createTournament(NewTournamentRequest request) {
        Tournament tournament = Tournament.builder()
                .name(request.getName())
                .regdeadline(request.getRegdeadline())
                .creatorID(request.getCreatorID())
                .status(true)
                .build();

        tournamentRepo.save(tournament);
        permissionRepo.save(new Permission(tournament.getTournamentID(), tournament.getCreatorID()));

        return tournament;
    }

    public Tournament getTournament(Long id) {
        return tournamentRepo.findByTournamentID(id).orElse(null);
    }

    public boolean isSubscribed(Long tournamentID, Long userID) {
        return tournamentRankingRepo.findByTournamentIDAndUserID(tournamentID, userID).isPresent();
    }

    public void addSubscription(@NotNull SubscriptionRequest request) {
        Tournament tournament = tournamentRepo.findByTournamentID(request.getTournamentID()).orElse(null);
        if (tournament == null) return;
        TournamentRanking ranking = new TournamentRanking();
        ranking.setTournamentID(request.getTournamentID());
        ranking.setUserID(request.getUserID());
        ranking.setScore(0);
        tournamentRankingRepo.save(ranking);
    }

    public String addPermission(PermissionRequest request) {
        Permission p = new Permission(request.getTournamentID(), request.getUserID());
        Tournament t = tournamentRepo.findByTournamentID(request.getTournamentID()).orElse(null);
        if (t.getCreatorID().equals(request.getCreatorID())) {
            permissionRepo.save(p);
            return "http://tournament-service/tournaments/" + request.getTournamentID();
        } else return null;
    }

    public List<TournamentRanking> getTournamentPage(GetTournamentPageRequest request) {
        return tournamentRankingRepo.findAllByTournamentIDOrderByScoreDesc(request.getTournamentID());
    }

    public List<Tournament> getAllTournaments() {
        return tournamentRepo.findAll();
    }

    public boolean closeTournament(CloseTournamentRequest request) {
        Tournament tournament = tournamentRepo.findByTournamentID(request.getTournamentID()).orElse(null);
        if (tournament.getCreatorID().equals(request.getCreatorID())) {
            tournament.setStatus(false);
            tournamentRepo.save(tournament);
            List<Permission> p = permissionRepo.findAllByTournamentID(request.getTournamentID());
            for (Permission permission : p) {
                permissionRepo.delete(permission);
            }
            return true;
        } else return false;
    }

    public boolean permissionExists(Long tournamentID, Long userID) {
        return permissionRepo.findByTournamentIDAndUserID(tournamentID, userID).isPresent();
    }

    public boolean updateScore(UpdateScoreRequest request) {
        List<TournamentRanking> records = tournamentRankingRepo.findAllByTournamentID(request.getTournamentID());
        System.out.println("before: " + records);
        if (records == null) return false;

        Map<Long, Integer> scores = new HashMap<>();
        List<WorkingPair<Long, Integer>> scoresList = request.getScores();

        for (WorkingPair<Long, Integer> score : scoresList) {
            scores.put(score.getLeft(), score.getRight());
        }

        for (TournamentRanking student : records) {
            if (scores.containsKey(student.getUserID())) {
                student.setScore(student.getScore() + scores.get(student.getUserID()));
                tournamentRankingRepo.save(student);
            }
        }
        records = tournamentRankingRepo.findAllByTournamentID(request.getTournamentID());
        System.out.println("after : " + records);
        return true;
    }

    public List<Long> getStudentsSubscribed(Long tournamentId) {
        return tournamentRankingRepo.findAllByTournamentID(tournamentId)
                .stream()
                .map(TournamentRanking::getUserID)
                .toList();
    }
}
