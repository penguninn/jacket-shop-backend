package com.threadcity.jacketshopbackend.service;

import com.threadcity.jacketshopbackend.common.Enums.PaymentStatus;
import com.threadcity.jacketshopbackend.common.Enums.OrderStatus;
import com.threadcity.jacketshopbackend.dto.request.OrderRequest;
import com.threadcity.jacketshopbackend.dto.response.OrderResponse;
import com.threadcity.jacketshopbackend.entity.*;
import com.threadcity.jacketshopbackend.exception.InvalidRequestException;
import com.threadcity.jacketshopbackend.exception.ResourceNotFoundException;
import com.threadcity.jacketshopbackend.mapper.OrderMapper;
import com.threadcity.jacketshopbackend.repository.*;
import com.threadcity.jacketshopbackend.service.auth.UserDetailsImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private ProductVariantRepository productVariantRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PaymentMethodRepository paymentMethodRepository;

    @Mock
    private ShippingMethodsRepository shippingMethodRepository;

    @Mock
    private CouponRepository couponRepository;

    @Mock
    private ProvinceRepository provinceRepository;

    @Mock
    private DistrictRepository districtRepository;

    @Mock
    private WardRepository wardRepository;

    @Mock
    private OrderMapper orderMapper;

    @InjectMocks
    private OrderService orderService;

    private User mockUser;
    private Cart mockCart;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Mock Security Context
        mockUser = new User();
        mockUser.setId(1L);

        UserDetailsImpl userDetails = mock(UserDetailsImpl.class);
        when(userDetails.getId()).thenReturn(mockUser.getId());

        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(userDetails);

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);

        // Mock Cart
        mockCart = new Cart();
        mockCart.setUser(mockUser);
        mockCart.setItems(Arrays.asList());
    }

    @Test
    void testCreateOrder_Success() {
        // Prepare cart with items
        ProductVariant variant = new ProductVariant();
        variant.setQuantity(10);
        variant.setSoldCount(0);
        variant.setPrice(new BigDecimal("100"));

        Product product = new Product();
        product.setName("Test Product");
        variant.setProduct(product);

        Size size = new Size();
        size.setName("M");
        variant.setSize(size);

        Color color = new Color();
        color.setName("Red");
        variant.setColor(color);

        CartItem item = new CartItem();
        item.setProductVariant(variant);
        item.setQuantity(2);

        mockCart.setItems(new ArrayList<>(List.of(item)));

        // Mock repositories
        when(userRepository.findById(mockUser.getId())).thenReturn(Optional.of(mockUser));
        when(cartRepository.findByUserId(mockUser.getId())).thenReturn(Optional.of(mockCart));

        PaymentMethod paymentMethod = new PaymentMethod();
        paymentMethod.setId(1L);
        paymentMethod.setName("COD");
        when(paymentMethodRepository.findById(1L)).thenReturn(Optional.of(paymentMethod));

        ShippingMethod shippingMethod = new ShippingMethod();
        shippingMethod.setId(1L);
        shippingMethod.setName("GHTK");
        shippingMethod.setFee(new BigDecimal("10"));
        when(shippingMethodRepository.findById(1L)).thenReturn(Optional.of(shippingMethod));

        Order order = new Order();
        order.setId(1L);
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(orderMapper.toDto(order)).thenReturn(new OrderResponse());

        // Prepare request
        OrderRequest request = new OrderRequest();
        request.setPaymentMethodId(1L);
        request.setShippingMethodId(1L);

        OrderResponse response = orderService.createOrder(request);

        assertNotNull(response);

        // Verify inventory updated
        assertEquals(8, variant.getQuantity());
        assertEquals(2, variant.getSoldCount());
    }

    @Test
    void testCreateOrder_CartEmpty() {
        mockCart.setItems(List.of());
        when(userRepository.findById(mockUser.getId())).thenReturn(Optional.of(mockUser));
        when(cartRepository.findByUserId(mockUser.getId())).thenReturn(Optional.of(mockCart));

        OrderRequest request = new OrderRequest();
        request.setPaymentMethodId(1L);
        request.setShippingMethodId(1L);

        assertThrows(InvalidRequestException.class, () -> orderService.createOrder(request));
    }

    @Test
    void testGetMyOrders() {
        Order order1 = new Order();
        order1.setId(1L);
        Order order2 = new Order();
        order2.setId(2L);

        when(orderRepository.findByUserId(mockUser.getId())).thenReturn(Arrays.asList(order1, order2));
        when(orderMapper.toDto(order1)).thenReturn(OrderResponse.builder().id(1L).build());
        when(orderMapper.toDto(order2)).thenReturn(OrderResponse.builder().id(2L).build());

        List<OrderResponse> result = orderService.getMyOrders();

        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals(2L, result.get(1).getId());
    }

    @Test
    void testGetOrderById_Found() {
        Order order = new Order();
        order.setId(1L);
        OrderResponse response = OrderResponse.builder().id(1L).build();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderMapper.toDto(order)).thenReturn(response);

        OrderResponse result = orderService.getOrderById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void testGetOrderById_NotFound() {
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> orderService.getOrderById(1L));
    }
}
