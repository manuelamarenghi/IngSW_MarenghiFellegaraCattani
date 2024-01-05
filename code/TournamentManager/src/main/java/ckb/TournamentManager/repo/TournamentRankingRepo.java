package ckb.TournamentManager.repo;

import ckb.TournamentManager.model.TournamentRanking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TournamentRankingRepo extends JpaRepository<TournamentRanking,Long> {
    Optional<TournamentRanking> findByTournamentID(@Param("tournamentID")Long tournamentID);
    Optional<TournamentRanking> findByTournamentIDAndUserID(@Param("tournamentID")Long tournamentID, @Param("userID")Long userID);
    List<TournamentRanking> findAllByTournamentID(@Param("tournamentID")Long tournamentID);

    Optional<List<TournamentRanking>> findDistinctByTournamentID(@Param("tournamentID")Long tournamentID);
    @Override
    void delete(TournamentRanking entity);
    @Override
    void deleteById(@Param("tournamentID")Long tournamentID);
    @Override
    <S extends TournamentRanking> S save(S entity);
    @Override
    <S extends TournamentRanking> List<S> saveAll(Iterable<S> entities);

    List<TournamentRanking> findByTournamentIDOrderByScoreAsc(@Param("tournamentID")Long tournamentID);
    List<TournamentRanking> findAllByTournamentIDOrderByScoreAsc(@Param("tournamentID")Long tournamentID);


}
