package com.example.iOrderService.controller;

import com.example.iOrderService.model.OrderRequest;
import com.example.iOrderService.model.OrderResponse;
import com.example.iOrderService.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping("/placeOrder")
    public ResponseEntity<Long> placeOrder(@RequestBody OrderRequest orderRequest){
        long orderId = orderService.placeOrder(orderRequest);
        return new ResponseEntity<>(orderId, HttpStatus.OK);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrderDetailByOrderId(@PathVariable long orderId){
        OrderResponse orderResponse = orderService.getOrderDetailByOrderId(orderId);
        return new ResponseEntity<>(orderResponse, HttpStatus.OK);
    }
}
