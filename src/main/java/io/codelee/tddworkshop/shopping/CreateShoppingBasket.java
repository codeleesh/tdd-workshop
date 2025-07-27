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
        
        // 실제 요청 데이터 사용 - 두 번째 테스트를 성공시키기 위한 최소한의 변경
        List<BasketItem> items = request.items().stream()
                .map(item -> new BasketItem(
                        item.name(), 
                        item.price(), 
                        item.quantity(), 
                        item.price().multiply(BigDecimal.valueOf(item.quantity()))
                ))
                .toList();
        
        Basket basket = new Basket(items);
        Basket savedBasket = basketRepository.save(basket);
        
        return new BasketResponse(String.valueOf(savedBasket.getId()));
    }
    
    @GetMapping("/{basketId}")
    @Transactional(readOnly = true)
    public BasketDetailsResponse getBasket(@PathVariable String basketId) {
        Long id = Long.valueOf(basketId);
        Basket basket = basketRepository.findById(id).orElseThrow();
        
        // 실제 basket 데이터 사용
        List<BasketItemDto> itemDtos = basket.getItems().stream()
                .map(item -> new BasketItemDto(
                        item.getName(), 
                        item.getQuantity(), 
                        item.getPrice(), 
                        item.getTotal()
                ))
                .toList();
        
        // 소계 계산
        BigDecimal subtotal = basket.getItems().stream()
                .map(BasketItem::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // 할인 계산
        BigDecimal discount = BigDecimal.ZERO;
        if (subtotal.compareTo(BigDecimal.valueOf(10000)) > 0 && 
            subtotal.compareTo(BigDecimal.valueOf(20000)) < 0) {
            // 10,000원 초과 20,000원 미만: 5% 할인
            discount = subtotal.multiply(BigDecimal.valueOf(0.05));
        }
        
        BigDecimal finalAmount = subtotal.subtract(discount);
        
        return new BasketDetailsResponse(
            basketId,
            itemDtos,
            subtotal,
            discount,
            finalAmount
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
