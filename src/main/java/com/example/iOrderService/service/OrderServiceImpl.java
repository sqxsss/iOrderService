package com.example.iOrderService.service;

import com.example.iOrderService.entity.OrderEntity;
import com.example.iOrderService.external.client.PaymentServiceFeignClient;
import com.example.iOrderService.external.client.ProductServiceFeignClient;
import com.example.iOrderService.model.OrderRequest;
import com.example.iOrderService.model.PaymentRequest;
import com.example.iOrderService.repository.OrderRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@Log4j2
public class OrderServiceImpl implements OrderService{

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductServiceFeignClient productServiceFeignClient;

    @Autowired
    private PaymentServiceFeignClient paymentServiceFeignClient;

    @Override
    public long placeOrder(OrderRequest orderRequest) {//TODO: make this method as transaction
        // call order service to create order entity with status CREATED, save to db
        log.info("OrderService: placeOrder save to order_db start");
        OrderEntity orderEntity = OrderEntity.builder()
                .productId(orderRequest.getProductId())
                .orderQuantity(orderRequest.getOrderQuantity())
                .totalAmount(orderRequest.getTotalAmount())
                .orderDate(Instant.now())
                .orderStatus("CREATED")
                .paymentMode(orderRequest.getPaymentMode().name())
                .build();
        orderEntity = orderRepository.save(orderEntity);
        log.info("OrderService: placeOrder save to order_db done");

        // call product service to check quantity and reduceQuantity if ok
        log.info("ProductServiceFeignClient: reduce quantity start");
        productServiceFeignClient.reduceQuantity(orderRequest.getProductId(), orderRequest.getOrderQuantity());
        log.info("ProductServiceFeignClient: reduce quantity done");

        // call payment service to charge payment mode, mark order COMPLETED if success
        log.info("PaymentServiceFeignClient: doPayment start");
        PaymentRequest paymentRequest = PaymentRequest.builder()
                .orderId(orderEntity.getOrderId())
                .paymentMode(orderRequest.getPaymentMode())
                .totalAmount(orderRequest.getTotalAmount())
                .build();
        String orderStatus = null;
        try{
            paymentServiceFeignClient.doPayment(paymentRequest);
            orderStatus = "PLACED";
        } catch (Exception c){
            orderStatus = "PAYMENT_FAILED";
        }

        orderEntity.setOrderStatus(orderStatus);
        orderRepository.save(orderEntity);

        log.info("PaymentServiceFeignClient: doPayment done");

        // , otherwise mark CANCELLED

        return orderEntity.getOrderId();
    }
}
