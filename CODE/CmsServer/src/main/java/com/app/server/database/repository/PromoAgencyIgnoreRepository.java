package com.app.server.database.repository;

import com.app.server.data.entity.PromoAgencyIgnoreEntity;
import com.app.server.data.entity.PromoAgencyIncludeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PromoAgencyIgnoreRepository extends JpaRepository<PromoAgencyIgnoreEntity, Integer> {
    List<PromoAgencyIgnoreEntity> findAll();
}