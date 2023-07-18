package com.example.iOrderService.service;

import com.example.iOrderService.entity.OrderEntity;
import com.example.iOrderService.external.client.PaymentServiceFeignClient;
import com.example.iOrderService.external.client.ProductServiceFeignClient;
import com.example.iOrderService.model.OrderRequest;
import com.example.iOrderService.model.OrderResponse;
import com.example.iOrderService.model.PaymentMode;
import com.example.iOrderService.model.PaymentRequest;
import com.example.iOrderService.repository.OrderRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
class OrderServiceImplTest {

    @Mock
    RestTemplate restTemplate;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductServiceFeignClient productServiceFeignClient;

    @Mock
    private PaymentServiceFeignClient paymentServiceFeignClient;

    @InjectMocks
    OrderService orderService = new OrderServiceImpl();

    @DisplayName("Get Order Detail - SUCCESS")
    @Test
    void testWhenGetOrderSuccess() {
        // Mock Part
        OrderEntity orderEntity = getMockOrderEntity();
        when(orderRepository.findById(anyLong())).thenReturn(Optional.of(orderEntity));

        when(restTemplate.getForObject(
                "http://PRODUCT-SERVICE/products/"+orderEntity.getProductId(),
                OrderResponse.ProductResponse.class
        )).thenReturn(getMockProductResponse());

        when(restTemplate.getForObject(
                "http://PAYMENT-SERVICE/payments/"+orderEntity.getOrderId(),
                OrderResponse.PaymentResponse.class
        )).thenReturn(getMockPaymentResponse());

        // Actual Call
        OrderResponse orderResponse = orderService.getOrderDetailByOrderId(1);

        // Verify Call
        verify(orderRepository, times(1)).findById(anyLong());
        verify(restTemplate, times(1)).getForObject(
                "http://PRODUCT-SERVICE/products/"+orderEntity.getProductId(),
                OrderResponse.ProductResponse.class
        );
        verify(restTemplate, times(1)).getForObject(
                "http://PAYMENT-SERVICE/payments/"+orderEntity.getOrderId(),
                OrderResponse.PaymentResponse.class
        );

        // Assert Response
        Assertions.assertNotNull(orderResponse);
        Assertions.assertEquals(orderEntity.getOrderId(), orderResponse.getOrderId());
    }

    @DisplayName("Place Order - SUCCESS")
    @Test
    void testWhenPlaceOrderSuccess() {
        OrderEntity orderEntity = getMockOrderEntity();
        OrderRequest orderRequest = getMockOrderRequest();

        when(orderRepository.save(any(OrderEntity.class)))
                .thenReturn(orderEntity);
        when(productServiceFeignClient.reduceQuantity(anyLong(), anyLong()))
                .thenReturn(new ResponseEntity<Void>(HttpStatus.OK));
        when(paymentServiceFeignClient.doPayment(any(PaymentRequest.class)))
                .thenReturn(new ResponseEntity<Long>(1L, HttpStatus.OK));

        long orderId = orderService.placeOrder(orderRequest);

        verify(orderRepository, times(2)).save(any());
        verify(productServiceFeignClient, times(1)).reduceQuantity(anyLong(), anyLong());
        verify(paymentServiceFeignClient, times(1)).doPayment(any());

        Assertions.assertEquals(orderEntity.getOrderId(), orderId);
    }

    @DisplayName("Place Order Payment Failed - Failed")
    @Test
    void testWhenPlaceOrderFailed() {
        OrderEntity orderEntity = getMockOrderEntity();
        OrderRequest orderRequest = getMockOrderRequest();

        when(orderRepository.save(any(OrderEntity.class)))
                .thenReturn(orderEntity);
        when(productServiceFeignClient.reduceQuantity(anyLong(), anyLong()))
                .thenReturn(new ResponseEntity<Void>(HttpStatus.OK));
        when(paymentServiceFeignClient.doPayment(any(PaymentRequest.class)))
                .thenThrow(new RuntimeException("Payment Failed"));

        long orderId = orderService.placeOrder(orderRequest);

        verify(orderRepository, times(2)).save(any());
        verify(productServiceFeignClient, times(1)).reduceQuantity(anyLong(), anyLong());
        verify(paymentServiceFeignClient, times(1)).doPayment(any());

        Assertions.assertEquals(orderEntity.getOrderId(), orderId);
    }

    private OrderRequest getMockOrderRequest() {
        return OrderRequest.builder()
                .productId(1)
                .orderQuantity(1)
                .totalAmount(1299)
                .paymentMode(PaymentMode.CASH)
                .build();
    }

    @DisplayName("Get Order Detail OrderId Not Found - FAILED")
    @Test
    void testWhenOrderIdNotFound(){
        when(orderRepository.findById(anyLong())).thenReturn(Optional.ofNullable(null));

//        orderService.getOrderDetailByOrderId(1);

        RuntimeException runtimeException = Assertions.assertThrows(RuntimeException.class, () -> orderService.getOrderDetailByOrderId(1));
        Assertions.assertEquals("OrderService getOrderDetailByOrderId NOT FOUND FOR 1", runtimeException.getMessage());

        verify(orderRepository, times(1)).findById(anyLong());
    }

    private OrderEntity getMockOrderEntity() {
        return OrderEntity.builder()
                .orderId(1)
                .productId(5)
                .orderQuantity(1)
                .totalAmount(1299)
                .orderDate(Instant.now())
                .orderStatus("PLACED")
                .build();
    }

    private OrderResponse.PaymentResponse getMockPaymentResponse() {
        return OrderResponse.PaymentResponse.builder()
                .orderId(2)
                .paymentId(1)
                .paymentDate(Instant.now())
                .paymentMode(PaymentMode.CASH)
                .paymentStatus("SUCCESS")
                .totalAmount(2599)
                .build();
    }

    private OrderResponse.ProductResponse getMockProductResponse() {
        return OrderResponse.ProductResponse.builder()
                .productId(2)
                .productName("MacMini")
                .productQuantity(2)
                .productPrice(1299)
                .build();
    }

    @Test
    void placeOrder() {
    }

    @Test
    void getOrderDetailByOrderId() {
    }
}