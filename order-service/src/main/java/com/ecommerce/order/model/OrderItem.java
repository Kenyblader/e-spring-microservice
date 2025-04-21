package com.ecommerce.order.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "order_items")
public class OrderItem {
    public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "order_id")
    @JsonBackReference
    private Order order;

    private Long productId;

    private Integer quantity;

    private Double priceAtOrder;
    
    

	public void setOrder(Order order) {
    	this.order=order;
    }
    
    public Order getOrder() {
    	return this.order;
    }

	public Long getProductId() {
		return productId;
	}

	public void setProductId(Long productId) {
		this.productId = productId;
	}

	public Integer getQuantity() {
		return quantity;
	}

	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}

	public Double getPriceAtOrder() {
		return priceAtOrder;
	}

	public void setPriceAtOrder(Double priceAtOrder) {
		this.priceAtOrder = priceAtOrder;
	}
}