package com.app.server.database.repository;

import com.app.server.data.entity.PromoFilterDetailEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PromoFilterDetailRepository extends JpaRepository<PromoFilterDetailEntity, Integer> {
    List<PromoFilterDetailEntity> findAll();
}