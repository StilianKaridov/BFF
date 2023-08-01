package com.tinqin.bff.persistence.repository;

import com.tinqin.bff.persistence.entity.ShoppingCart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ShoppingCartRepository extends JpaRepository<ShoppingCart, UUID> {

    Optional<ShoppingCart> findByUserIdAndItemId(UUID userId, UUID itemId);

    List<ShoppingCart> findAllByUserId(UUID userId);

    Optional<ShoppingCart> findByUserId(UUID userId);

    void deleteAllByUserId(UUID userId);
}
