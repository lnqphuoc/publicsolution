package com.app.server.database.repository;

import com.app.server.data.entity.PromoEntity;
import com.app.server.data.entity.PromoItemGroupEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PromoItemGroupRepository extends JpaRepository<PromoItemGroupEntity, Integer> {
    List<PromoItemGroupEntity> findAll();
}