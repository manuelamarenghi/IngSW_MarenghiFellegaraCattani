package ckb.BattleManager.repository;

import ckb.BattleManager.model.Battle;
import ckb.BattleManager.model.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {

    @Query("select p.studentId, t.score " +
            "from Team t join Participation p on t = p.team where t.battle = :battle")
    List<Object[]> findPairsIdUserPointsByBattleId(@Param("battle") Battle battle);

    // get the team of a student with his/her id and the battle_id
    @Query("SELECT t FROM Participation p JOIN Team t ON p.team = t" +
            " WHERE p.studentId = :idStudent AND t.battle = :battle")
    Optional<Team> findTeamByStudentIdAndBattle(@Param("idStudent") Long idStudent, @Param("battle") Battle battle);

    List<Team> findTeamsByBattle(Battle battle);
}
