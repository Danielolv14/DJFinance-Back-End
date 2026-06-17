package com.druds.repository;

import com.druds.model.BloqueioAgenda;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface BloqueioAgendaRepository extends JpaRepository<BloqueioAgenda, Long> {

    @Query("""
        SELECT b FROM BloqueioAgenda b
        WHERE (b.dj = :dj OR (:dj = 'DRUDS' AND b.dj IS NULL))
    """)
    List<BloqueioAgenda> findByDj(@Param("dj") String dj);

    @Query("""
        SELECT COUNT(b) > 0 FROM BloqueioAgenda b
        WHERE b.dataInicio <= :data AND b.dataFim >= :data
          AND (b.dj = :dj OR (:dj = 'DRUDS' AND b.dj IS NULL))
    """)
    boolean existeConflito(@Param("data") LocalDate data, @Param("dj") String dj);
}
