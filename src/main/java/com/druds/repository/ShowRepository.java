package com.druds.repository;

import com.druds.model.Show;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ShowRepository extends JpaRepository<Show, Long> {

    List<Show> findByDataBetweenOrderByDataAsc(LocalDate inicio, LocalDate fim);

    @Modifying
    @Query("UPDATE Show s SET s.endereco = :para WHERE s.endereco = :de")
    int atualizarEndereco(@Param("de") String de, @Param("para") String para);

    @Modifying
    @Query("UPDATE Show s SET s.contratante = :para WHERE s.contratante = :de")
    int atualizarContratante(@Param("de") String de, @Param("para") String para);
}
