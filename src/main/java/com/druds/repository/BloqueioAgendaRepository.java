package com.druds.repository;

import com.druds.model.BloqueioAgenda;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;

public interface BloqueioAgendaRepository extends JpaRepository<BloqueioAgenda, Long> {

    @Query("SELECT COUNT(b) > 0 FROM BloqueioAgenda b WHERE b.dataInicio <= :data AND b.dataFim >= :data")
    boolean existeConflito(@Param("data") LocalDate data);
}
