package com.ecommerce.order.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class RequestData {

    private Long id;
    private Long userId;
    private List<ProductQuantity> products;
    private double total;
    private String status; // Peut être amélioré avec un enum
    private String createdAt;
    
    public RequestData(Order order){
    	this.id=order.getId();
    	this.createdAt=order.getCreatedAt().toString();
    	this.status=order.getStatus();
    	this.total=order.getTotal();
    	this.userId=order.getUserId();
    	this.products = new ArrayList<ProductQuantity>();
    	for(OrderItem item:order.getItems()) {
    		ProductQuantity productQ = new ProductQuantity(item);
    		this.products.add(productQ);
    	}
    }

    // Getters et Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public List<ProductQuantity> getProducts() {
        return products;
    }

    public void setProducts(List<ProductQuantity> products) {
        this.products = products;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
    
    public Order toOrder() {
    	Order order=new Order();
    	order.setCreatedAt(LocalDateTime.parse(createdAt));
    	order.setId(id);
    	order.setStatus(status);
    	order.setTotal(total);
    	order.setUserId(userId);
    	List<OrderItem> items=  products.stream().map(product->product.toOrderItem(order) ).collect(Collectors.toList()) ;
    	order.setItems(items );
    	return order;
    }

    /**
	 * 
	 */
	public RequestData() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	

	// Classe interne pour représenter les produits
    public static class ProductQuantity {
        private Long productId;
        private int quantity;
        private double price;
        private Long id;
        
        public Long getId() {
			return id;
		}

		public void setId(Long id) {
			this.id = id;
		}

		ProductQuantity(OrderItem item){
        	this.price=item.getPriceAtOrder();
        	this.productId=item.getProductId();
        	this.quantity=item.getQuantity();
        	this.id=item.getId();
        }
		
		ProductQuantity(){
			
		}

        public double getPrice() {
			return price;
		}

		public void setPrice(double price) {
			this.price = price;
		}

		public Long getProductId() {
            return productId;
        }

        public void setProductId(Long productId) {
            this.productId = productId;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }
        
        public OrderItem toOrderItem(Order order) {
        	OrderItem item=new OrderItem();
        	item.setId(id);
        	item.setOrder(order);
        	item.setPriceAtOrder(price);
        	item.setProductId(productId);
        	item.setQuantity(quantity);
        	return item;
        }
    }
}
