package com.app.server.database.repository;

import com.app.server.data.entity.PromoApplyObjectEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PromoApplyObjectRepository extends JpaRepository<PromoApplyObjectEntity, Integer> {
    List<PromoApplyObjectEntity> findAll();
}