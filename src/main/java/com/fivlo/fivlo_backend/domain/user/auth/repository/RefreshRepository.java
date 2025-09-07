package com.fivlo.fivlo_backend.domain.user.auth.repository;

import com.fivlo.fivlo_backend.domain.user.auth.entity.RefreshEntity;
import org.springframework.data.repository.CrudRepository;

public interface RefreshRepository extends CrudRepository<RefreshEntity, Long> {
}
