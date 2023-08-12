package jpabook.jpashop.domain;

import jakarta.persistence.*;
import jpabook.jpashop.domain.item.Item;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter @Setter
public class OrderItem {
    @Id @GeneratedValue
    @Column(name = "order_item_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    private Item item;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    private int orderPrice; // 주문 가격(주문 당시의 가격, 상품 가격은 바뀔 수 있음)
    private int count;  // 주문 수량

    //==생성 메서드==//
    public sealed OrderItem createdOrderItem(Item item, int orderPrice, int count){
     OrderItem orderItem = new OrderItem();
     orderItem.setItem(item);
     orderItem.setOrderPrice(orderPrice);
     orderItem.setCount(count);

     item.removeStock(count);
     return orderItem;
    }

    //==비즈니스 로직==//
    public void cancel() {
        getItem().addStock(count);
    }

    public int getTotalPrice() {
        return getCount() * getOrderPrice();
    }
}
