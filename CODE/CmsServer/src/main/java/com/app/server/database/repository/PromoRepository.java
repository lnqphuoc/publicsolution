package com.app.server.database.repository;

import com.app.server.data.entity.ProductEntity;
import com.app.server.data.entity.PromoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PromoRepository extends JpaRepository<PromoEntity, Integer> {
    List<PromoEntity> findAll();
}