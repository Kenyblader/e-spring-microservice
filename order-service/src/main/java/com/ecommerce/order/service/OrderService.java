package com.ecommerce.order.service;

import com.ecommerce.order.model.Order;
import com.ecommerce.order.model.OrderItem;
import com.ecommerce.order.model.Product;
import com.ecommerce.order.model.RequestData;
import com.ecommerce.order.model.RequestData.ProductQuantity;
import com.ecommerce.order.model.User;
import com.ecommerce.order.repository.OrderItemRepository;
import com.ecommerce.order.repository.OrderRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository repository;
    private final OrderItemRepository itemRepository;
    private final RestTemplate restTemplate;

    @Value("${product.service.url:http://localhost:8081/products}")
    private String PRODUCT_SERVICE_URL;

    @Value("${user.service.url:http://localhost:8083/users}")
    private String USER_SERVICE_URL;

    public OrderService(OrderRepository repository, RestTemplate restTemplate,OrderItemRepository itr) {
        this.repository = repository;
		this.itemRepository = itr;
        this.restTemplate = restTemplate;
    }

    private void validateUser(Long userId,String token) {
    	
        logger.info("Validating user with ID: {}", userId);
        String url = USER_SERVICE_URL + "/" + userId;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token); // Ajout du token JWT
        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<User> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    User.class
            );
            User user= response.getBody();
            if (user == null) {
                logger.error("User with ID {} not found", userId);
                throw new RuntimeException("User not found with ID: " + userId);
            }
            logger.info("User validated: {}", user.getEmail());
        } catch (HttpClientErrorException e) {
            System.err.println("Erreur lors de la récupération de l'utilisateur : " + e.getStatusCode());
            throw e;
        }
        
    }

    public Order createOrder(RequestData data,String token) {
    	
        logger.info("Processing order creation for userId: {}", data.getUserId());
        // Valider l'utilisateur
        
        validateUser(data.getUserId(),token);

        for (ProductQuantity item : data.getProducts()) {
            Product product = restTemplate.getForObject(
                    PRODUCT_SERVICE_URL + "/" + item.getProductId(),
                    Product.class
            );
            if (product == null) {
                throw new RuntimeException("Product not found: " + item.getProductId());
            }
            if (product.getStock() < item.getQuantity()) {
                throw new RuntimeException("Insufficient stock for product: " + product.getName());
            }

            item.setPrice(product.getPrice());

            Product updatedProduct = new Product();
            updatedProduct.setId(product.getId());
            updatedProduct.setName(product.getName());
            updatedProduct.setDescription(product.getDescription());
            updatedProduct.setPrice(product.getPrice());
            updatedProduct.setStock(product.getStock() - item.getQuantity());
            restTemplate.put(PRODUCT_SERVICE_URL + "/" + item.getProductId(), updatedProduct);
        }

        double total = data.getProducts().stream()
                .mapToDouble(item -> item.getPrice() * item.getQuantity())
                .sum();
        Order order=new Order();
        order.setUserId(data.getUserId());
        order.setTotal(total);
        order.setStatus("PENDING");
        order.setCreatedAt(LocalDateTime.now());

        Order savedOrder = repository.save(order);
        
        for(ProductQuantity item : data.getProducts()) {
        	OrderItem orderItem = new OrderItem();
        	orderItem.setOrder(savedOrder);
        	orderItem.setPriceAtOrder(item.getPrice());
        	orderItem.setProductId(item.getProductId());
        	orderItem.setQuantity(item.getQuantity());
        	
        	OrderItem savedItem=itemRepository.save(orderItem);
        	savedOrder.addOrderItem(savedItem);
        	
        }
        logger.info("Order created with ID: {}", savedOrder.getId());
        return savedOrder;
    }

    public List<RequestData> getAllOrders() {
        logger.info("Fetching all orders from repository");
        List<Order> orders = repository.findAllWithItems();
        System.out.println(orders.size());
        logger.info("Found {} orders", orders.size());
        List<RequestData> datas=new ArrayList<RequestData>();
        for(Order order: orders) {
        	RequestData data=new RequestData(order);
        	datas.add(data);
        }
        
        return datas;
    }

    public Optional<Order> getOrderById(Long id) {
        logger.info("Fetching order with ID: {}", id);
        return repository.findByIdWithItems(id);
    }

    public List<Order> getOrdersByUserId(Long userId, String token) {
        logger.info("Fetching orders for userId: {}", userId);
        // Valider l'utilisateur
 
        validateUser(userId,token);
        List<Order> orders = repository.findByUserIdWithItems(userId);
        logger.info("Found {} orders for userId: {}", orders.size(), userId);
        return orders;
    }

    public RequestData updateOrder(Long id, RequestData data ,String token) {
    	Order updatedOrder =data.toOrder();
        logger.info("Updating order with ID: {}", id);
        Optional<Order> existingOrderOpt = repository.findByIdWithItems(id);
        if (existingOrderOpt.isEmpty()) {
            throw new RuntimeException("Order not found with ID: " + id);
        }
        System.out.println("begin validation");
        // Valider l'utilisateur
        validateUser(updatedOrder.getUserId(),token);
        System.out.println("ok validation true");
        Order existingOrder = existingOrderOpt.get();

        // Restaurer le stock des anciens items
        for (OrderItem item : existingOrder.getItems()) {
            Product product = restTemplate.getForObject(
                    PRODUCT_SERVICE_URL + "/" + item.getProductId(),
                    Product.class
            );
            if (product == null) {
                throw new RuntimeException("Product not found: " + item.getProductId());
            }
            Product updatedProduct = new Product();
            updatedProduct.setId(product.getId());
            updatedProduct.setName(product.getName());
            updatedProduct.setDescription(product.getDescription());
            updatedProduct.setPrice(product.getPrice());
            updatedProduct.setStock(product.getStock() + item.getQuantity());
            restTemplate.put(PRODUCT_SERVICE_URL + "/" + item.getProductId(), updatedProduct);
        }

        // Mettre à jour les champs de la commande
        existingOrder.setUserId(updatedOrder.getUserId());
        existingOrder.setStatus(updatedOrder.getStatus());
        existingOrder.getItems().clear();
        for (OrderItem newItem : updatedOrder.getItems()) {
            Product product = restTemplate.getForObject(
                    PRODUCT_SERVICE_URL + "/" + newItem.getProductId(),
                    Product.class
            );
            if (product == null) {
                throw new RuntimeException("Product not found: " + newItem.getProductId());
            }
            if (product.getStock() < newItem.getQuantity()) {
                throw new RuntimeException("Insufficient stock for product: " + product.getName());
            }
            newItem.setPriceAtOrder(product.getPrice());
            newItem.setOrder(existingOrder);
            existingOrder.getItems().add(newItem);

            Product updatedProduct = new Product();
            updatedProduct.setId(product.getId());
            updatedProduct.setName(product.getName());
            updatedProduct.setDescription(product.getDescription());
            updatedProduct.setPrice(product.getPrice());
            updatedProduct.setStock(product.getStock() - newItem.getQuantity());
            restTemplate.put(PRODUCT_SERVICE_URL + "/" + newItem.getProductId(), updatedProduct);
        }

        double total = existingOrder.getItems().stream()
                .mapToDouble(item -> item.getPriceAtOrder() * item.getQuantity())
                .sum();
        existingOrder.setTotal(total);

        Order savedOrder = repository.save(existingOrder);
        logger.info("Order updated with ID: {}", savedOrder.getId());
        return new RequestData(savedOrder);
    }

    public void deleteOrder(Long id) {
        logger.info("Deleting order with ID: {}", id);
        Optional<Order> orderOpt = repository.findByIdWithItems(id);
        if (orderOpt.isEmpty()) {
            throw new RuntimeException("Order not found with ID: " + id);
        }

        Order order = orderOpt.get();

        for (OrderItem item : order.getItems()) {
            Product product = restTemplate.getForObject(
                    PRODUCT_SERVICE_URL + "/" + item.getProductId(),
                    Product.class
            );
            if (product == null) {
                throw new RuntimeException("Product not found: " + item.getProductId());
            }
            Product updatedProduct = new Product();
            updatedProduct.setId(product.getId());
            updatedProduct.setName(product.getName());
            updatedProduct.setDescription(product.getDescription());
            updatedProduct.setPrice(product.getPrice());
            updatedProduct.setStock(product.getStock() + item.getQuantity());
            restTemplate.put(PRODUCT_SERVICE_URL + "/" + item.getProductId(), updatedProduct);
        }

        repository.deleteById(id);
        logger.info("Order deleted with ID: {}", id);
    }
}