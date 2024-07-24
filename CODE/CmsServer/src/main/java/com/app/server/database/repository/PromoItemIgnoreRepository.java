package com.app.server.database.repository;

import com.app.server.data.entity.PromoItemIgnoreEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PromoItemIgnoreRepository extends JpaRepository<PromoItemIgnoreEntity, Integer> {
    List<PromoItemIgnoreEntity> findAll();
}