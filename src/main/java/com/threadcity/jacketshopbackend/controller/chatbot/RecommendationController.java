package com.threadcity.jacketshopbackend.controller.chatbot;

import com.threadcity.jacketshopbackend.entity.Product;
import com.threadcity.jacketshopbackend.service.chatbot.RecommendationService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recommend")
public class RecommendationController {

    private final RecommendationService recService;

    public RecommendationController(RecommendationService recService){
        this.recService = recService;
    }

    @GetMapping("/related")
    public List<Product> related(@RequestParam Long productId, @RequestParam(required = false, defaultValue = "6") int k){
        return recService.recommendRelated(productId, k);
    }

    @GetMapping("/homepage")
    public List<Product> homepage(@RequestParam Long userId, @RequestParam(required = false, defaultValue = "10") int k){
        return recService.recommendForHomepage(userId, k);
    }

    @PostMapping("/cart")
    public List<Product> cart(@RequestBody List<Long> cartItemIds, @RequestParam(required = false, defaultValue = "5") int k){
        return recService.recommendForCart(cartItemIds, k);
    }
}
