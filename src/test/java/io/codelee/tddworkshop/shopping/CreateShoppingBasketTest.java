package io.codelee.tddworkshop.shopping;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.approvaltests.Approvals;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/// - [X] 빈 장바구니에서 청구서 요청 시 예외 발생
/// - [X] 단일 상품을 1개만 장바구니에 추가 (할인 없음, 10,000원 이하)
/// - [X] 10,000원 초과 20,000원 미만 구매 시 5% 할인 적용
/// - [X] 20,000원 이상 구매 시 10% 할인 적용
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
public class CreateShoppingBasketTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @DisplayName("빈 장바구니에서 청구서 요청 시 예외 발생")
    @Test
    void empty_basket_throws_exception_when_requesting_receipt() throws Exception {
        // given: 빈 장바구니
        BasketBuilder emptyBasket = aBasket();

        // when & then: 예외 발생 검증
        basketApi.createBasketAndExpectError(emptyBasket);
    }

    @DisplayName("단일 상품을 1개만 장바구니에 추가 (할인 없음, 10,000원 이하)")
    @Test
    void single_item_no_discount_under_10000() throws Exception {
        // given
        String basketId = basketApi.createBasket(
                aBasket().withItem(anItem("보호필름").withPrice(5000))
        );

        // when & then
        BasketDetailsResponse basketDetails = basketApi.getBasketDetails(basketId);
        Approvals.verify(printBasketDetails(basketDetails));
    }

    @DisplayName("10,000원 초과 20,000원 미만 구매 시 5% 할인 적용")
    @Test
    void discount_5_percent_between_10000_and_20000() throws Exception {
        // given
        String basketId = basketApi.createBasket(
                aBasket()
                        .withItem(anItem("스마트폰 케이스").withPrice(12000))
                        .withItem(anItem("보호필름").withPrice(3000))
        );

        // when & then
        BasketDetailsResponse basketDetails = basketApi.getBasketDetails(basketId);
        Approvals.verify(printBasketDetails(basketDetails));
    }

    @DisplayName("20,000원 이상 구매 시 10% 할인 적용")
    @Test
    void discount_10_percent_over_20000() throws Exception {
        // given
        String basketId = basketApi.createBasket(
                aBasket()
                        .withItem(anItem("스마트폰 케이스").withPrice(15000))
                        .withItem(anItem("보호필름").withPrice(5000))
        );

        // when & then
        BasketDetailsResponse basketDetails = basketApi.getBasketDetails(basketId);
        Approvals.verify(printBasketDetails(basketDetails));
    }

    @DisplayName("엔드-투-엔드 기능 구현: UI부터 데이터베이스까지 전체 시스템을 관통하는 기본적인 흐름 포함")
    @Test
    void walking_skeleton_shopping_basket() throws Exception {
        // given
        String basketId = basketApi.createBasket(
                aBasket().withItem(anItem("충전 케이블").withPrice(8000))
        );

        // when & then
        BasketDetailsResponse basketDetails = basketApi.getBasketDetails(basketId);
        Approvals.verify(printBasketDetails(basketDetails));
    }

    /**
     * 영수증을 출력하는 메소드
     */
    private String printBasketDetails(BasketDetailsResponse basketDetails) {
        StringBuilder receipt = new StringBuilder();
        receipt.append("===== 영수증 =====\n");
        receipt.append("품목:\n");
        
        // 실제 basketDetails의 items를 사용
        for (BasketItemDto item : basketDetails.items()) {
            receipt.append(String.format("- %s %d개 (단가: %,d원, 총액: %,d원)\n", 
                item.name(), 
                item.quantity(), 
                item.price().intValue(), 
                item.total().intValue()));
        }
        
        receipt.append(String.format("소계: %,d원\n", basketDetails.subtotal().intValue()));
        
        if (basketDetails.discount().compareTo(BigDecimal.ZERO) > 0) {
            // 할인율 계산 (5% 또는 10%)
            double discountRate = basketDetails.discount().doubleValue() / basketDetails.subtotal().doubleValue() * 100;
            receipt.append(String.format("할인: %,d원 (%.0f%% 할인)\n", 
                basketDetails.discount().intValue(), discountRate));
        } else {
            receipt.append("할인: 0원 (할인 없음)\n");
        }
        
        receipt.append(String.format("최종 결제 금액: %,d원\n", basketDetails.finalAmount().intValue()));
        receipt.append("==================");
        
        return receipt.toString();
    }

    // DTO record들 - CreateShoppingBasket의 inner record로 작성
    public record BasketItemRequests(List<BasketItemRequest> items) {}
    public record BasketItemRequest(String name, BigDecimal price, int quantity) {}
    public record BasketResponse(String basketId) {}
    public record BasketDetailsResponse(String basketId, List<BasketItemDto> items,
                                        BigDecimal subtotal, BigDecimal discount, BigDecimal finalAmount) {}
    public record BasketItemDto(String name, int quantity, BigDecimal price, BigDecimal total) {}

    // Protocol Driver
    private class BasketApi {
        public String createBasket(BasketBuilder basketBuilder) throws Exception {
            BasketItemRequests request = basketBuilder.build();
            
            MvcResult result = mockMvc.perform(post("/api/baskets")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andReturn();

            BasketResponse response = objectMapper.readValue(
                    result.getResponse().getContentAsString(),
                    BasketResponse.class);
            
            return response.basketId();
        }

        public MvcResult createBasketAndExpectError(BasketBuilder basketBuilder) throws Exception {
            BasketItemRequests request = basketBuilder.build();
            
            return mockMvc.perform(post("/api/baskets")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andReturn();
        }

        public BasketDetailsResponse getBasketDetails(String basketId) throws Exception {
            MvcResult result = mockMvc.perform(get("/api/baskets/" + basketId))
                    .andExpect(status().isOk())
                    .andReturn();

            return objectMapper.readValue(
                    result.getResponse().getContentAsString(),
                    BasketDetailsResponse.class);
        }
    }

    private BasketApi basketApi = new BasketApi();

    // Test Data Builders
    private static BasketBuilder aBasket() {
        return new BasketBuilder();
    }

    private static BasketItemBuilder anItem(String name) {
        return new BasketItemBuilder(name);
    }

    private static class BasketBuilder {
        private final List<BasketItemRequest> items = new ArrayList<>();

        public BasketBuilder withItem(BasketItemBuilder itemBuilder) {
            items.add(itemBuilder.build());
            return this;
        }

        public BasketItemRequests build() {
            return new BasketItemRequests(new ArrayList<>(items));
        }
    }

    private static class BasketItemBuilder {
        private final String name;
        private BigDecimal price = BigDecimal.ZERO;
        private int quantity = 1;

        public BasketItemBuilder(String name) {
            this.name = name;
        }

        public BasketItemBuilder withPrice(int price) {
            this.price = BigDecimal.valueOf(price);
            return this;
        }

        public BasketItemBuilder withQuantity(int quantity) {
            this.quantity = quantity;
            return this;
        }

        public BasketItemRequest build() {
            return new BasketItemRequest(name, price, quantity);
        }
    }
}
