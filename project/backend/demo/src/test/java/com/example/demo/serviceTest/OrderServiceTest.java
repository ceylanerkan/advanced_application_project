package com.example.demo.serviceTest;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ecommerce.model.Order;
import com.ecommerce.repository.OrderRepository;
import com.ecommerce.service.OrderService;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderService orderService;

    private Order testOrder;

    @BeforeEach
    void setUp() {
        testOrder = new Order();
        testOrder.setId(1L);
        testOrder.setStatus("Completed");
        testOrder.setGrandTotal(150.50);
    }

    @Test
    void getAllOrders_ShouldReturnList() {
        when(orderRepository.findAll()).thenReturn(Arrays.asList(testOrder));
        List<Order> orders = orderService.getAllOrders();
        assertEquals(1, orders.size());
    }

    @Test
    void getOrderById_ShouldReturnOrder() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        Order found = orderService.getOrderById(1L);
        assertEquals("Completed", found.getStatus());
    }
}