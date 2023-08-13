package jpabook.jpashop.service;

import jakarta.persistence.EntityManager;
import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.domain.item.Book;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.exception.NotEnoughStockException;
import jpabook.jpashop.repository.OrderRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;


@SpringBootTest
@Transactional
class OrderServiceTest {

    @Autowired
    EntityManager em;
    @Autowired
    OrderService orderService;
    @Autowired
    OrderRepository orderRepository;

    @Test
    public void 상품주문() throws Exception{
        //given
        Member member = createMember();

        Book book = createBook("시골 JPA", 10000, 10);

        int orderCount = 2;

        //when
        Long orderId = orderService.order(member.getId(), book.getId(), orderCount);

        //then
        Order order = orderRepository.findOne(orderId);

        Assertions.assertThat(order.getStatus()).isEqualTo(OrderStatus.ORDER);  // 상품 주문시 상태는 ORDER
        Assertions.assertThat(order.getOrderItems().size()).isEqualTo(1);   // 주문한 상품 종류 수가 정확해야 한다
        Assertions.assertThat(order.getTotalPrice()).isEqualTo(10000 * orderCount);  // 주문 가격은 가격 * 수량이다
        Assertions.assertThat(book.getStockQuantity()).isEqualTo(8);  // 주무 수량만큼 재고가 줄어야 한다
    }

    @Test
    public void 상품주문_재고수량초과() throws Exception{
        //given
        Member member = createMember();
        Item item = createBook("시골 JPA", 10000, 10);

        int orderCount = 11;

        //when

        //then
        Assertions.assertThatThrownBy(() -> orderService.order(member.getId(), item.getId(), orderCount))
                        .isInstanceOf(NotEnoughStockException.class);   // 재고 수량 예외가 발생해야 한다
    }

    @Test
    public void 주문취소() throws Exception{
        //given
        Member member = createMember();
        Item item = createBook("시골 JPA", 10000, 10);

        int orderCount = 2;
        Long orderId = orderService.order(member.getId(),item.getId(), orderCount);

        //when
        orderService.cancelOrder(orderId);

        //then
        Order getOrder = orderRepository.findOne(orderId);

        Assertions.assertThat(getOrder.getStatus()).isEqualTo(OrderStatus.CANCEL);  // 주문 취소시 상태는 CANCEL 이다
        Assertions.assertThat(item.getStockQuantity()).isEqualTo(10);   // 주문 취소된 상품은 그대로 재고가 증가해야 한다
    }

    private Book createBook(String name, int price, int stockQuantity) {
        Book book = new Book();
        book.setName(name);
        book.setPrice(price);
        book.setStockQuantity(stockQuantity);
        em.persist(book);
        return book;
    }

    private Member createMember() {
        Member member = new Member();
        member.setName("회원1");
        member.setAddress(new Address("서울", "강가", "123-123"));
        em.persist(member);
        return member;
    }
}