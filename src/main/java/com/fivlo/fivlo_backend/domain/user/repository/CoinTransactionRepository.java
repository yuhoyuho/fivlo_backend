package com.fivlo.fivlo_backend.domain.user.repository;

import com.fivlo.fivlo_backend.domain.user.entity.CoinTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CoinTransactionRepository extends JpaRepository<CoinTransaction, Long> {
}
