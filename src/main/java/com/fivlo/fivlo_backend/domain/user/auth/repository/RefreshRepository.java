package com.fivlo.fivlo_backend.domain.user.auth.repository;

import com.fivlo.fivlo_backend.domain.user.auth.entity.RefreshEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface RefreshRepository extends CrudRepository<RefreshEntity, Long> {

    Optional<RefreshEntity> findByToken(String token);
}
