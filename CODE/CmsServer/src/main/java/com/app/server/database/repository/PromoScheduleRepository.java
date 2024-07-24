package com.app.server.database.repository;

import com.app.server.data.entity.PromoScheduleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PromoScheduleRepository extends JpaRepository<PromoScheduleEntity, Integer> {
    List<PromoScheduleEntity> findAll();
}