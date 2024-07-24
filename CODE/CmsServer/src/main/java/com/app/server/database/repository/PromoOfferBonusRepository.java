package com.app.server.database.repository;

import com.app.server.data.entity.PromoOfferBonusEntity;
import com.app.server.data.entity.PromoOfferEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PromoOfferBonusRepository extends JpaRepository<PromoOfferBonusEntity, Integer> {
    List<PromoOfferBonusEntity> findAll();
}