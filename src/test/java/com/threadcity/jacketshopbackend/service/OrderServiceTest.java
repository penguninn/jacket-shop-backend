package com.threadcity.jacketshopbackend.service;

import com.threadcity.jacketshopbackend.common.Enums.OrderStatus;
import com.threadcity.jacketshopbackend.common.Enums.OrderType;
import com.threadcity.jacketshopbackend.dto.request.OrderRequest;
import com.threadcity.jacketshopbackend.dto.response.OrderResponse;
import com.threadcity.jacketshopbackend.entity.*;
import com.threadcity.jacketshopbackend.exception.ResourceNotFoundException;
import com.threadcity.jacketshopbackend.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class OrderServiceTest {

    @Mock
    private OnlineOrderService onlineOrderService;

    @Mock
    private PosOrderService posOrderService;

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderService orderService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateOrder_Online() {
        OrderRequest request = new OrderRequest();
        request.setOrderType(OrderType.ONLINE);
        OrderResponse expectedResponse = new OrderResponse();

        when(onlineOrderService.createOrder(request)).thenReturn(expectedResponse);

        OrderResponse actualResponse = orderService.createOrder(request);

        assertEquals(expectedResponse, actualResponse);
        verify(onlineOrderService).createOrder(request);
        verifyNoInteractions(posOrderService);
    }

    @Test
    void testCreateOrder_Pos() {
        OrderRequest request = new OrderRequest();
        request.setOrderType(OrderType.POS_INSTORE);
        OrderResponse expectedResponse = new OrderResponse();

        when(posOrderService.createOrder(request)).thenReturn(expectedResponse);

        OrderResponse actualResponse = orderService.createOrder(request);

        assertEquals(expectedResponse, actualResponse);
        verify(posOrderService).createOrder(request);
        verifyNoInteractions(onlineOrderService);
    }

    @Test
    void testGetMyOrders() {
        List<OrderResponse> expected = Arrays.asList(new OrderResponse(), new OrderResponse());
        when(onlineOrderService.getMyOrders()).thenReturn(expected);

        List<OrderResponse> actual = orderService.getMyOrders();

        assertEquals(expected, actual);
        verify(onlineOrderService).getMyOrders();
    }

    @Test
    void testGetOrderById() {
        Long id = 1L;
        OrderResponse expected = new OrderResponse();
        when(onlineOrderService.getOrderById(id)).thenReturn(expected);

        OrderResponse actual = orderService.getOrderById(id);

        assertEquals(expected, actual);
        verify(onlineOrderService).getOrderById(id);
    }
}
