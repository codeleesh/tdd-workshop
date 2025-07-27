package io.codelee.tddworkshop.shopping;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BasketRepositoryJpa extends JpaRepository<Basket, Long> {
}
