package com.app.server.database.repository;

import com.app.server.data.entity.PromoFilterEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PromoFilterRepository extends JpaRepository<PromoFilterEntity, Integer> {
    List<PromoFilterEntity> findAll();
}