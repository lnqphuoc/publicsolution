package com.app.server.database.repository;

import com.app.server.data.entity.PromoLimitEntity;
import com.app.server.data.entity.PromoOfferEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PromoOfferRepository extends JpaRepository<PromoOfferEntity, Integer> {
    List<PromoOfferEntity> findAll();
}