package io.codelee.tddworkshop.shopping;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "baskets")
public class Basket {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToMany(mappedBy = "basket", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<BasketItem> items;

    protected Basket() {
        // JPA 기본 생성자
    }

    public Basket(Long id, List<BasketItem> items) {
        this.id = id;
        this.items = items;
        if (items != null) {
            items.forEach(item -> item.setBasket(this));
        }
    }

    public Basket(List<BasketItem> items) {
        this.items = items;
        if (items != null) {
            items.forEach(item -> item.setBasket(this));
        }
    }

    public Long getId() {
        return id;
    }

    public List<BasketItem> getItems() {
        return items;
    }
}
