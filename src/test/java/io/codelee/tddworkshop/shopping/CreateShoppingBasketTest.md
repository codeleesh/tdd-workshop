package io.codelee.tddworkshop.shopping;

import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@RestController
@RequestMapping("/api/baskets")
public class CreateShoppingBasket {
    
    private static final AtomicLong idGenerator = new AtomicLong(1);
    
    @PostMapping
    public BasketResponse createBasket(@RequestBody BasketItemRequests request) {
        // 최소한의 구현 - 단순히 ID만 반환
        String basketId = String.valueOf(idGenerator.getAndIncrement());
        return new BasketResponse(basketId);
    }
    
    @GetMapping("/{basketId}")
    public BasketDetailsResponse getBasket(@PathVariable String basketId) {
        // 최소한의 구현 - 하드코딩된 응답
        return new BasketDetailsResponse(
            basketId,
            List.of(new BasketItemDto("충전 케이블", 1, BigDecimal.valueOf(8000), BigDecimal.valueOf(8000))),
            BigDecimal.valueOf(8000),
            BigDecimal.ZERO,
            BigDecimal.valueOf(8000)
        );
    }
    
    // Inner record들
    public record BasketItemRequests(List<BasketItemRequest> items) {}
    public record BasketItemRequest(String name, BigDecimal price, int quantity) {}
    public record BasketResponse(String basketId) {}
    public record BasketDetailsResponse(String basketId, List<BasketItemDto> items, 
                                       BigDecimal subtotal, BigDecimal discount, BigDecimal finalAmount) {}
    public record BasketItemDto(String name, int quantity, BigDecimal price, BigDecimal total) {}
}
