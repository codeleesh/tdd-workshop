package io.codelee.tddworkshop.shopping;

import java.util.Optional;

public interface BasketRepository {
    Basket save(Basket basket);
    Optional<Basket> findById(Long id);
}
