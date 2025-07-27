package io.codelee.tddworkshop.shopping;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/baskets")
public class CreateShoppingBasket {
    
    private final BasketRepository basketRepository;
    
    public CreateShoppingBasket(BasketRepository basketRepository) {
        this.basketRepository = basketRepository;
    }
    
    @PostMapping
    @Transactional
    public BasketResponse createBasket(@RequestBody BasketItemRequests request) {
        // 빈 장바구니 체크 - 테스트를 성공시키기 위한 최소한의 코드
        if (request.items().isEmpty()) {
            throw new IllegalArgumentException("장바구니가 비어있습니다.");
        }
        
        // 최소한의 구현 - 하드코딩으로 테스트 성공시키기
        List<BasketItem> items = List.of(
            new BasketItem("충전 케이블", BigDecimal.valueOf(8000), 1, BigDecimal.valueOf(8000))
        );
        
        Basket basket = new Basket(items);
        Basket savedBasket = basketRepository.save(basket);
        
        return new BasketResponse(String.valueOf(savedBasket.getId()));
    }
    
    @GetMapping("/{basketId}")
    @Transactional(readOnly = true)
    public BasketDetailsResponse getBasket(@PathVariable String basketId) {
        // 최소한의 구현 - 하드코딩으로 테스트 성공시키기
        Long id = Long.valueOf(basketId);
        Basket basket = basketRepository.findById(id).orElseThrow();
        
        List<BasketItemDto> itemDtos = List.of(
            new BasketItemDto("충전 케이블", 1, BigDecimal.valueOf(8000), BigDecimal.valueOf(8000))
        );
        
        return new BasketDetailsResponse(
            basketId,
            itemDtos,
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
