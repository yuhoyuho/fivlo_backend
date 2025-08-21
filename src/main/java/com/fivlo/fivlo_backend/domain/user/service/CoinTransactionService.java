package com.fivlo.fivlo_backend.domain.user.service;

import com.fivlo.fivlo_backend.domain.user.entity.CoinTransaction;
import com.fivlo.fivlo_backend.domain.user.entity.User;
import com.fivlo.fivlo_backend.domain.user.repository.CoinTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CoinTransactionService {

    private final CoinTransactionRepository coinTransactionRepository;

    /**
     * 코인 변동 내역 기록
     */
    public void logTransaction(User user, int amount) {
        if(user == null || amount == 0) {
            return;
        }

        CoinTransaction transaction = CoinTransaction.builder()
                .user(user)
                .amount(amount)
                .build();

        coinTransactionRepository.save(transaction);
    }
}
