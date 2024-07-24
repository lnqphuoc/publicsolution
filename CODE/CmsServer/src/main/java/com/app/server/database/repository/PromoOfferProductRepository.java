package com.app.server.database.repository;

import com.app.server.data.entity.PromoOfferEntity;
import com.app.server.data.entity.PromoOfferProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PromoOfferProductRepository extends JpaRepository<PromoOfferProductEntity, Integer> {
    List<PromoOfferProductEntity> findAll();
}