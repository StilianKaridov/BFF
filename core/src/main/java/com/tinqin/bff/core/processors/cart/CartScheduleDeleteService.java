package com.tinqin.bff.core.processors.cart;

import com.tinqin.bff.persistence.repository.ShoppingCartRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Service
public class CartScheduleDeleteService {

    private final ShoppingCartRepository shoppingCartRepository;

    @Autowired
    public CartScheduleDeleteService(ShoppingCartRepository shoppingCartRepository) {
        this.shoppingCartRepository = shoppingCartRepository;
    }

    @Transactional
    @Scheduled(cron = "0 0 * * * *")
    public void deleteCartIfNotSoldWithinOneWeek() {
        LocalDateTime oneWeekAgo = LocalDateTime.now().minusWeeks(1);
        Timestamp timestamp = Timestamp.valueOf(oneWeekAgo);
        this.shoppingCartRepository.deleteAllByAddedOnBefore(timestamp);
    }
}
