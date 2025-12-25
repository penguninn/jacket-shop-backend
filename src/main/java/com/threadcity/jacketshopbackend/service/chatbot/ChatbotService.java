package com.threadcity.jacketshopbackend.service.chatbot;

import com.threadcity.jacketshopbackend.entity.Product;
import com.threadcity.jacketshopbackend.entity.chatbot.ChatbotContext;
import com.threadcity.jacketshopbackend.repository.ProductRepository;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.spring.AiService;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ChatbotService {

    @AiService
    interface Assistant {
        String chat(@UserMessage String message);
    }
    private final ObjectProvider<ChatbotContext> contextProvider;
    private final Assistant assistant;
    private final ProductRepository productRepository;


    public ChatbotService(ProductRepository productRepository,ObjectProvider<ChatbotContext> contextProvider) {
        this.productRepository = productRepository;
        this.contextProvider  = contextProvider;

        OpenAiChatModel model = OpenAiChatModel.builder()
                .apiKey("")
                .modelName("gpt-4o-mini")
                .temperature(0.4)
                .build();

        this.assistant = AiServices.create(Assistant.class, model);
    }
    private ChatbotContext context() {
        return contextProvider.getIfAvailable();
    }

    /* ================= MAIN ================= */

    public String chat(String message) {
        String msg = normalize(message);

        if (isThanks(msg)) {
            return "Không có gì ạ! 😄 Mình luôn sẵn sàng tư vấn áo khoác cho bạn.";
        }

        // 0️⃣ greeting — CHỈ khi câu rất ngắn
        if (isGreeting(msg) && msg.length() <= 15) {
            return greetingResponse();
        }

        // 1️⃣ LIST intent – ƯU TIÊN TUYỆT ĐỐI
        if (isListStyleRequest(msg)) {
            return listAvailableStyles();
        }

        if (isListBrandRequest(msg)) {
            return listAvailableBrands();
        }

        if (context().getLastIntent() == ChatbotContext.ChatIntent.STYLE_SELECTION) {

            // user gõ lại "style" → hiểu là muốn xem danh sách
            if (isListStyleRequest(msg)) {
                return listAvailableStyles();
            }

            context().setLastIntent(ChatbotContext.ChatIntent.FREE_CHAT);

            if (isMeaninglessReply(msg)) {
                return "Bạn có thể chọn 1 style cụ thể như **biker**, **bomber**, **hoodie** giúp mình nhé 😊";
            }

            return filterByStyle(msg);
        }

        if (context().getLastIntent() == ChatbotContext.ChatIntent.BRAND_SELECTION) {

            if (isListBrandRequest(msg)) {
                return listAvailableBrands();
            }

            context().setLastIntent(ChatbotContext.ChatIntent.FREE_CHAT);

            if (isMeaninglessReply(msg)) {
                return "Bạn có thể cho mình biết **tên hãng** bạn quan tâm (ví dụ: Nike, Adidas) nhé 👌";
            }

            return filterByBrand(msg);
        }


        // 4️⃣ more products
        if (isMoreRequest(msg)) {
            return suggestAnotherProduct();
        }

        // 5️⃣ intro (sau list)
        if (isIntroRequest(msg)) {
            return recommendProducts();
        }

        // 6️⃣ filter rõ ràng
        if (isStyleRequest(msg)) {
            return filterByStyle(msg);
        }

        if (isBrandRequest(msg)) {
            return filterByBrand(msg);
        }

        // 7️⃣ OUT OF SCOPE
        return outOfScopeResponse();
    }
    private boolean isMeaninglessReply(String msg) {
        return msg.length() <= 2
                || List.of("ok", "ừ", "uh", "đi", "ờ", "yes", "no").contains(msg);
    }
    /* ================= INTENT ================= */

    private boolean isGreeting(String msg) {
        return containsAny(msg, "xin chào", "hello", "hi", "chào shop");
    }

    private boolean isIntroRequest(String msg) {
        return containsAny(msg,
                "gợi ý", "giới thiệu", "tham khảo", "tư vấn",
                "recommend", "suggest", "xem thử", "cho tôi xem");
    }

    private boolean isMoreRequest(String msg) {
        return containsAny(msg, "mẫu khác", "thêm mẫu", "còn không", "khác không", "xem ");
    }

    private boolean isStyleRequest(String msg) {
        if (isListStyleRequest(msg)) return false;
        return containsAny(msg,
                "style", "kiểu", "dáng",
                "biker", "bomber", "blazer", "hoodie");
    }

    private boolean isBrandRequest(String msg) {
        if (isListBrandRequest(msg)) return false;
        return containsAny(msg,
                "hãng", "brand", "hiệu",
                "nike", "adidas", "puma", "uniqlo");
    }

    /* ================= HANDLER ================= */

    private String greetingResponse() {
        return """
                Chào bạn 👋  
                Mình là tư vấn viên của ThreadCity.  
                Bạn đang tìm áo khoác theo **style**, **hãng** nào để mình hỗ trợ tốt nhất ạ?
                """;
    }

    private String recommendProducts() {
        context().getShownProductIds().clear();

        List<Product> products = productRepository.findTop3ByOrderByIdAsc();
        if (products.isEmpty()) return noProductResponse();

        products.forEach(p -> context().getShownProductIds().add(p.getId()));

        return buildProductListResponse(
                "Mình gợi ý cho bạn một vài mẫu áo khoác đang được nhiều khách hàng quan tâm:",
                products
        );
    }


    private String suggestAnotherProduct() {

        Set<Long> shownIds = context().getShownProductIds();

        List<Product> products = productRepository
                .findTop3ByIdNotInOrderByIdAsc(shownIds);

        if (products.isEmpty()) {
            return "Mình đã giới thiệu hết các mẫu phù hợp rồi 😊";
        }

        products.forEach(p -> shownIds.add(p.getId()));

        return buildProductListResponse(
                "Mình giới thiệu thêm cho bạn một số mẫu khác nhé:",
                products
        );
    }

    /* ================= BRAND & STYLE DETECTION ================= */

    private String filterByBrand(String msg) {
        final String keyword = detectBrandKeyword(msg);

        return filterProducts(
                p -> containsIgnoreCase(p.getBrand().getName(), keyword),
                "Một số mẫu áo khoác đến từ hãng bạn đang tìm:"
        );
    }

    private String filterByStyle(String msg) {
        final String keyword = detectStyleKeyword(msg);

        return filterProducts(
                p -> containsIgnoreCase(p.getStyle().getName(), keyword),
                "Các mẫu áo khoác theo style bạn đang quan tâm:"
        );
    }

    /* ================= KEYWORD DETECTION ================= */

    private String detectBrandKeyword(String msg) {
        msg = msg.replaceAll(
                "(xem|thử|cho|tôi|cái|đi|phát|check|loại|mẫu|anh|chị|mình|em|thì|sao)",
                "").trim();

        String k = extractKeyword(msg, "hãng", "brand", "hiệu");

        if (k.isEmpty()) k = normalize(msg);

        return k;
    }

    private String detectStyleKeyword(String msg) {
        msg = msg.replaceAll(
                "(xem|thử|cho|tôi|cái|đi|phát|check|loại|mẫu|anh|chị|mình|em|thì|sao)",
                "").trim();

        String k = extractKeyword(msg, "style", "kiểu", "dáng");

        if (k.isEmpty()) k = normalize(msg);

        return k;
    }
    /* ================= CORE FILTER ================= */

    private String filterProducts(ProductMatcher matcher, String title) {
        List<Product> products = productRepository.findAll().stream()
                .filter(p -> p.getBrand() != null && p.getStyle() != null)
                .filter(matcher::match)
                .limit(3)
                .collect(Collectors.toList());

        if (products.isEmpty()) {
            return "Mình chưa tìm thấy sản phẩm phù hợp 😥 Bạn muốn thử tiêu chí khác không?";
        }

        return buildProductListResponse(title, products);
    }

    /* ================= RESPONSE ================= */

    private String buildProductListResponse(String title, List<Product> products) {
        StringBuilder sb = new StringBuilder(title).append("\n\n");

        for (Product p : products) {
            sb.append("• **").append(p.getName()).append("**\n")
                    .append("  - Hãng: ").append(p.getBrand().getName()).append("\n")
                    .append("  - Style: ").append(p.getStyle().getName()).append("\n")
                    .append("  - Giá: ").append(formatPrice(p)).append("\n\n");
        }

        sb.append("👉 Bạn thích mẫu nào không? Mình có thể tìm **mẫu theo hãng khác** cho bạn nhé!");
        return sb.toString();
    }

    private String buildSingleProductResponse(Product p, String title) {
        return """
                %s
                
                • **%s**
                  - Hãng: %s
                  - Style: %s
                  - Giá: %s
                  - Mô tả: %s
                
                Bạn thấy mẫu này thế nào? Mình có thể gợi ý thêm mẫu tương tự cho bạn 👌
                """.formatted(
                title,
                p.getName(),
                p.getBrand().getName(),
                p.getStyle().getName(),
                formatPrice(p),
                p.getDescription()
        );
    }

    private String noProductResponse() {
        return "Hiện tại shop chưa có sản phẩm phù hợp để giới thiệu 😢";
    }

    /* ================= HELPER ================= */

    private String normalize(String text) {
        return text == null ? "" : text.trim().toLowerCase();
    }

    private boolean containsAny(String msg, String... keywords) {
        return Arrays.stream(keywords).anyMatch(msg::contains);
    }

    private String extractKeyword(String msg, String... triggers) {
        for (String trigger : triggers) {
            int index = msg.indexOf(trigger);
            if (index != -1) {
                return normalize(msg.substring(index + trigger.length()));
            }
        }
        return "";
    }

    private boolean containsIgnoreCase(String source, String keyword) {
        return source != null
                && keyword != null
                && source.toLowerCase().contains(keyword.toLowerCase());
    }

    private String formatPrice(Product p) {
        if (p.getMinPrice() == null || p.getMaxPrice() == null) return "Liên hệ";
        if (p.getMinPrice().equals(p.getMaxPrice())) {
            return p.getMinPrice() + " VND";
        }
        return p.getMinPrice() + " - " + p.getMaxPrice() + " VND";
    }

    /* ================= FUNCTIONAL ================= */

    @FunctionalInterface
    interface ProductMatcher {
        boolean match(Product p);
    }
    private boolean isListStyleRequest(String msg) {
        return msg.equals("style")
                || containsAny(msg,
                "có những style nào",
                "có những style gì",
                "có những loại nào",
                "có những loại gì",
                "có các loại gì",
                "có các loại nào",
                "có các style nào",
                "tìm áo khoác theo style",
                "tìm sản phẩm theo style",
                "các style",
                "style nào",
                "những style nào");
    }

    private boolean isListBrandRequest(String msg) {
        return msg.equals("hãng")
                || msg.equals("brand")
                || containsAny(msg,
                "có những hãng nào",
                "có những hãng gì",
                "có các hãng nào",
                "có các hãng gì",
                "tìm áo khoác theo hãng",
                "tìm áo khoác theo brand",
                "tìm sản phẩm theo hãng",
                "tìm sản phẩm theo brand",
                "hãng gì",
                "các hãng",
                "hãng nào",
                "những hãng gì",
                "những hãng nào");
    }
    private String listAvailableStyles() {
        List<String> styles = productRepository.findTopStyles(PageRequest.of(0, 3));
        if (styles.isEmpty()) return "Hiện tại shop chưa có nhiều style để lựa chọn 😥";

        context().setLastIntent(ChatbotContext.ChatIntent.STYLE_SELECTION); // gợi ý đang chờ chọn style

        return """
        Hiện tại shop có một số style được khách hàng yêu thích:
        %s
        
        👉 Bạn đang quan tâm style nào để mình tư vấn chi tiết hơn?
        """.formatted(formatList(styles));
    }


    private String listAvailableBrands() {
        List<String> brands = productRepository.findTopBrands(PageRequest.of(0, 3));
        if (brands.isEmpty()) return "Hiện tại shop chưa có nhiều hãng để giới thiệu 😥";

        context().setLastIntent(ChatbotContext.ChatIntent.BRAND_SELECTION);

        return """
        Shop hiện đang có các hãng áo khoác được ưa chuộng:
        %s
        
        👉 Bạn muốn xem sản phẩm của hãng nào không?
        """.formatted(formatList(brands));
    }

    private String formatList(List<String> items) {
        return items.stream()
                .map(i -> "• " + i)
                .collect(Collectors.joining("\n"));
    }
    private boolean isThanks(String msg) {
        return containsAny(msg,
                "cảm ơn", "thanks", "thank you", "tks", "thank");
    }
    private String outOfScopeResponse() {
        List<String> replies = List.of(
                "😄 Mình chuyên tư vấn áo khoác nên chưa thể trò chuyện về chủ đề này được.",
                "🙈 Chủ đề này hơi ngoài phạm vi của mình rồi.",
                "😊 Mình không rành về nội dung đó lắm."
        );

        String redirect = """
    
    👉 Nhưng mình có thể giúp bạn:
    • gợi ý áo khoác theo **style**
    • tìm sản phẩm theo **hãng**
    • hoặc giới thiệu **mẫu đang được yêu thích**
    
    Bạn muốn xem theo hướng nào ạ?
    """;

        String randomReply = replies.get(new Random().nextInt(replies.size()));
        return randomReply + redirect;
    }

}
