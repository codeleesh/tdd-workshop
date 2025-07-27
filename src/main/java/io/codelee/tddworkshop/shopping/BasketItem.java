package io.codelee.tddworkshop.shopping;

import java.math.BigDecimal;

public class BasketItem {
    private String name;
    private BigDecimal price;
    private int quantity;
    private BigDecimal total;

    public BasketItem(String name, BigDecimal price, int quantity, BigDecimal total) {
        this.name = name;
        this.price = price;
        this.quantity = quantity;
        this.total = total;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public int getQuantity() {
        return quantity;
    }

    public BigDecimal getTotal() {
        return total;
    }
}
