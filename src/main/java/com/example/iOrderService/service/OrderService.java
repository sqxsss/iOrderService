package com.example.iOrderService.service;

import com.example.iOrderService.model.OrderRequest;

public interface OrderService {

    long placeOrder(OrderRequest orderRequest);
}
