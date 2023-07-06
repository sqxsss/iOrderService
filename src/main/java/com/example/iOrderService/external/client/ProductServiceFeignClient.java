package com.example.iOrderService.external.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "PRODUCT-SERVICE/products")
public interface ProductServiceFeignClient {
    @PutMapping
    public ResponseEntity<Void> reduceQuantity(@RequestParam long productId, @RequestParam long quantity);

}
