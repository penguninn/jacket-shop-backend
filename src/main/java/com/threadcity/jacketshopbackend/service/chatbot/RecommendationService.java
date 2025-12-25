package com.threadcity.jacketshopbackend.service.chatbot;

import com.threadcity.jacketshopbackend.entity.Product;
import com.threadcity.jacketshopbackend.entity.chatbot.InteractionEntity;
import com.threadcity.jacketshopbackend.repository.ProductRepository;
import com.threadcity.jacketshopbackend.repository.chatbot.InteractionRepository;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RecommendationService {

    private final ProductRepository productRepo;
    private final InteractionRepository interactionRepo;

    // weights (tuneable)
    private final double W_CONTENT = 0.6;
    private final double W_BEHAVIOR = 0.4;

    public RecommendationService(ProductRepository productRepo, InteractionRepository interactionRepo){
        this.productRepo = productRepo;
        this.interactionRepo = interactionRepo;
    }

    // ---------- util: parse tags ----------
    private Set<String> tagsOf(Product product){
        if (product.getDescription() == null || product.getDescription().isBlank()) return Set.of();
        return Arrays.stream(product.getDescription().split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(String::toLowerCase)
                .collect(Collectors.toSet());
    }

    // Jaccard similarity
    private double contentSimilarity(Product a, Product b){
        Set<String> A = tagsOf(a);
        Set<String> B = tagsOf(b);
        if (A.isEmpty() && B.isEmpty()) return 0.0;
        Set<String> inter = new HashSet<>(A); inter.retainAll(B);
        Set<String> uni = new HashSet<>(A); uni.addAll(B);
        return (double) inter.size() / (double) uni.size();
    }

    // build product-user map: productId -> Map<userId, score>
    private Map<Long, Map<Long, Double>> buildProductUserMap(){
        Map<Long, Map<Long, Double>> map = new HashMap<>();
        List<InteractionEntity> all = interactionRepo.findAll();
        for (InteractionEntity it : all){
            map.computeIfAbsent(it.getProductId(), k -> new HashMap<>())
                    .merge(it.getUserId(), it.getActionScore().doubleValue(), Double::sum);
        }
        return map;
    }

    // cosine similarity between two product vectors (maps user->score)
    private double behaviorSimilarity(Map<Long, Double> v1, Map<Long, Double> v2){
        if (v1 == null || v2 == null) return 0.0;
        // dot product
        double dot = 0.0;
        for (Map.Entry<Long, Double> e : v1.entrySet()){
            Double other = v2.get(e.getKey());
            if (other != null) dot += e.getValue() * other;
        }
        double norm1 = Math.sqrt(v1.values().stream().mapToDouble(d -> d*d).sum());
        double norm2 = Math.sqrt(v2.values().stream().mapToDouble(d -> d*d).sum());
        if (norm1 == 0 || norm2 == 0) return 0.0;
        return dot / (norm1 * norm2);
    }

    // ---------- Public methods ----------

    // 1) Related products for a given productId
    public List<Product> recommendRelated(Long productId, int k){
        var target = productRepo.findById(productId);
        if (target.isEmpty()) return List.of();

        List<Product> allProducts = productRepo.findAll();
        Map<Long, Map<Long, Double>> productUser = buildProductUserMap();

        Product base = target.get();
        Map<Long, Double> baseVec = productUser.getOrDefault(productId, Map.of());

        // compute scores
        List<ScoredProduct> scored = new ArrayList<>();
        for (Product it : allProducts){
            if (it.getId().equals(productId)) continue;
            double cSim = contentSimilarity(base, it);
            double bSim = behaviorSimilarity(baseVec, productUser.getOrDefault(it.getId(), Map.of()));
            double score = W_CONTENT * cSim + W_BEHAVIOR * bSim;
            scored.add(new ScoredProduct(it, score));
        }
        return scored.stream()
                .sorted(Comparator.comparingDouble(ScoredProduct::getScore).reversed())
                .limit(k)
                .map(ScoredProduct::getProduct)
                .collect(Collectors.toList());
    }

    // 2) Homepage recommendations for a user (hybrid)
    public List<Product> recommendForHomepage(Long userId, int k){
        // if user has no history -> return top popularity
        var interactions = interactionRepo.findByUserId(userId);
        // build profile vector: aggregate product vectors of products the user interacted with
        Map<Long, Map<Long, Double>> productUser = buildProductUserMap();
        // user profile in product-space: we'll score candidate products by similarity to products user liked
        // For simplicity: for each candidate product, compute average similarity to user's interacted products (hybrid)
        Set<Long> userProductIds = interactions.stream().map(InteractionEntity::getProductId).collect(Collectors.toSet());
        List<Product> allProducts = productRepo.findAll();

        List<ScoredProduct> scored = new ArrayList<>();
        for (Product candidate : allProducts){
            if (userProductIds.contains(candidate.getId())) continue; // skip products already interacted
            double avgScore = 0.0;
            int cnt = 0;
            for (Long ui : userProductIds){
                Product userProduct = productRepo.findById(ui).orElse(null);
                if (userProduct == null) continue;
                double cSim = contentSimilarity(candidate, userProduct);
                double bSim = behaviorSimilarity(productUser.getOrDefault(candidate.getId(), Map.of()),
                        productUser.getOrDefault(ui, Map.of()));
                double sim = W_CONTENT * cSim + W_BEHAVIOR * bSim;
                avgScore += sim; cnt++;
            }
            if (cnt > 0) avgScore = avgScore / cnt;
            scored.add(new ScoredProduct(candidate, avgScore));
        }

        return scored.stream().sorted(Comparator.comparingDouble(ScoredProduct::getScore).reversed())
                .limit(k)
                .map(ScoredProduct::getProduct).collect(Collectors.toList());
    }

    // 3) Recommend complements for cart products (co-occurrence)
    public List<Product> recommendForCart(List<Long> cartProductIds, int k){
        Map<Long, Integer> coCount = new HashMap<>();
        // naive co-occurrence: find users who interacted with cart products, then count other products they interacted with
        List<InteractionEntity> all = interactionRepo.findAll();
        Set<Long> users = all.stream()
                .filter(it -> cartProductIds.contains(it.getProductId()))
                .map(InteractionEntity::getUserId)
                .collect(Collectors.toSet());

        for (InteractionEntity it : all){
            if (users.contains(it.getUserId()) && !cartProductIds.contains(it.getProductId())){
                coCount.merge(it.getProductId(), 1, Integer::sum);
            }
        }
        // rank by coCount then popularity
        List<Long> sortedProductIds = coCount.entrySet().stream()
                .sorted(Map.Entry.<Long,Integer>comparingByValue().reversed())
                .map(Map.Entry::getKey).collect(Collectors.toList());
        List<Product> out = new ArrayList<>();
        for (Long id : sortedProductIds){
            productRepo.findById(id).ifPresent(out::add);
            if (out.size() >= k) break;
        }
        return out;
    }

    // helper holder
    private static class ScoredProduct {
        private final Product product;
        private final double score;
        public ScoredProduct(Product product, double score){ this.product = product; this.score = score;}
        public Product getProduct(){ return product; }
        public double getScore(){ return score; }
    }
}
