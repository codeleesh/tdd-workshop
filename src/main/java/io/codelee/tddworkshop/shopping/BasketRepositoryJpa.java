package io.codelee.tddworkshop.shopping;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import java.util.Optional;

public interface BasketRepositoryJpa extends JpaRepository<Basket, Long> {
    
    @EntityGraph(attributePaths = {"items"})
    Optional<Basket> findById(Long id);
}
