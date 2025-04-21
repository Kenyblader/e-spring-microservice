package com.ecommerce.order.controller;

import com.ecommerce.order.model.Order;
import com.ecommerce.order.model.RequestData;
import com.ecommerce.order.service.OrderService;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/orders")
public class OrderController {
    @Autowired
    private OrderService service;

    @GetMapping
    public List<RequestData> getAllOrders() {
        return service.getAllOrders();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrderById(@PathVariable Long id) {
        Optional<Order> order = service.getOrderById(id);
        return order.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Order>> getOrdersByUserId(@PathVariable Long userId, HttpServletRequest request) {
    	System.out.println("ok");
    	String header=request.getHeader("Authorization");
    	System.out.println("yo"+header);
        if(header==null || !header.startsWith("Bearer "))
        	return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        String token= header.substring(7);
        return ResponseEntity.ok(service.getOrdersByUserId(userId,token));
    }
    
    @PostMapping
    public ResponseEntity<Order> createOrder(@RequestBody RequestData data,HttpServletRequest request) {
    	System.out.println("ok");
    	String header=request.getHeader("Authorization");
    	System.out.println("yo"+header);
        if(header==null || !header.startsWith("Bearer "))
        	return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        String token= header.substring(7);
        return ResponseEntity.ok(service.createOrder(data,token)) ;
    }

    @PutMapping("/{id}")
    public ResponseEntity<RequestData> updateOrder(@PathVariable Long id, @RequestBody RequestData data,HttpServletRequest request) {
    	String header=request.getHeader("Authorization");
    	System.out.println("header: "+header);
        if(header==null || !header.startsWith("Bearer "))
        	return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        String token= header.substring(7);
        try {
            return ResponseEntity.ok(service.updateOrder(id, data,token));
        } catch (RuntimeException e) {
        	System.out.println("error: "+e.getLocalizedMessage()+" message: "+e.getStackTrace());
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long id) {
        try {
            service.deleteOrder(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}