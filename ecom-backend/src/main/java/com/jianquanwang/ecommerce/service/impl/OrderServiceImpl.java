package com.jianquanwang.ecommerce.service.impl;

import com.jianquanwang.ecommerce.exceptions.APIException;
import com.jianquanwang.ecommerce.exceptions.ResourceNotFoundException;
import com.jianquanwang.ecommerce.model.*;
import com.jianquanwang.ecommerce.payload.AddressResponse;
import com.jianquanwang.ecommerce.payload.OrderDTO;
import com.jianquanwang.ecommerce.payload.OrderItemDTO;
import com.jianquanwang.ecommerce.repositories.*;
import com.jianquanwang.ecommerce.service.CartService;
import com.jianquanwang.ecommerce.service.OrderService;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private CartService cartService;

    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private ProductRepository productRepository;

    @Override
    @Transactional
    public OrderDTO placeOrder(String email, Long addressId, String paymentMethod, String pgName, Long pgPaymentId, String pgStatus, String pgResponseMessage) {
        Cart cart = cartRepository.findCartByEmail(email);
        if (cart == null) {
            throw new ResourceNotFoundException("Cart", "Email", email);
        }

        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "AddressId", addressId));

        Order order = new Order();
        order.setEmail(email);
        order.setOrderDate(LocalDate.now());
        order.setTotalAmount(cart.getTotalPrice());
        order.setOrderStatus("Order Accepted !");
        order.setAddress(address);



        Payment payment = new Payment(paymentMethod, pgPaymentId, pgStatus, pgResponseMessage, pgName);
        payment.setOrder(order);
        payment = paymentRepository.save(payment);
        order.setPayment(payment);

        Order savedOrder = orderRepository.save(order);
        List<CartItem> cartItems = cart.getCartItems();
        if (cartItems.isEmpty()) {
            throw new APIException("Cart is empty");
        }

        List<OrderItem> orderItems = new ArrayList<>();
        for (CartItem cartItem : cartItems) {
            OrderItem orderItem = modelMapper.map(cartItem, OrderItem.class);
            orderItem.setOrderedProductPrice(cartItem.getProductPrice());
            orderItem.setOrder(savedOrder);
            orderItems.add(orderItem);
        }

        orderItems = orderItemRepository.saveAll(orderItems);

        cart.getCartItems().forEach(item->{
            int quantity = item.getQuantity();
            Product product = item.getProduct();
            product.setQuantity(product.getQuantity() - quantity);
            productRepository.save(product);
            cartService.deleteProductFromCart(item.getProduct().getProductId());
        });

        OrderDTO orderDTO = modelMapper.map(savedOrder, OrderDTO.class);
        orderItems.forEach(item ->{
            orderDTO.getOrderItems().add(modelMapper.map(item, OrderItemDTO.class));
        });

        orderDTO.setAddressId(addressId);
        return orderDTO;
    }
}
