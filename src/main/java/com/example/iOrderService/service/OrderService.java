package com.example.iOrderService.service;

import com.example.iOrderService.model.OrderRequest;
import com.example.iOrderService.model.OrderResponse;

public interface OrderService {

    long placeOrder(OrderRequest orderRequest);

    OrderResponse getOrderDetailByOrderId(long orderId);
}
