package com.threadcity.jacketshopbackend.service.chatbot;

import com.threadcity.jacketshopbackend.entity.Product;
import com.threadcity.jacketshopbackend.repository.ProductRepository;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.spring.AiService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.Random;

@Service
public class ChatbotService {

    @AiService
    interface Assistant {
        String chat(@UserMessage String message);
    }

    private final Assistant assistant;
    private final ProductRepository productRepository;
    private final Random random = new Random();

    public ChatbotService(ProductRepository productRepository) {
        this.productRepository = productRepository;

        OpenAiChatModel model = OpenAiChatModel.builder()
                .apiKey("")
                .modelName("gpt-4o-mini")
                .temperature(0.3)
                .build();

        this.assistant = AiServices.create(Assistant.class, model);
    }

    public String chat(String message) {
        String msg = message.toLowerCase(Locale.ROOT);

        // 1️⃣ Xin chào
        if (msg.contains("xin chào") || msg.contains("hello")) {
            return "Chào bạn 👋 Mình có thể giúp bạn tìm áo khoác phù hợp. "
                    + "Bạn đang tìm kiểu dáng nào?";
        }

        // 2️⃣ Gợi ý ví dụ → 3 sản phẩm ID cũ nhất
        if (msg.contains("gợi ý") || msg.contains("ví dụ")) {
            List<Product> products = productRepository.findTop3ByOrderByIdAsc();
            return buildProductResponse(
                    "Dưới đây là một vài sản phẩm bạn có thể tham khảo:",
                    products
            );
        }

        // 3️⃣ Có sản phẩm khác không → random 1 sản phẩm
        if (msg.contains("khác") || msg.contains("thêm")) {
            List<Product> all = productRepository.findAll();
            if (all.isEmpty()) return "Hiện tại chưa có thêm sản phẩm.";

            Product p = all.get(random.nextInt(all.size()));
            return buildSingleProductResponse(p);
        }

        // 4️⃣ Câu hỏi tự do → AI xử lý
        return assistant.chat(message);
    }

    // ===== Helper =====

    private String buildProductResponse(String title, List<Product> products) {
        StringBuilder sb = new StringBuilder(title).append("\n\n");
        for (Product p : products) {
            sb.append("• **").append(p.getName()).append("**: ")
                    .append(p.getDescription()).append("\n");
        }
        sb.append("\nBạn muốn xem thêm sản phẩm khác không?");
        return sb.toString();
    }

    private String buildSingleProductResponse(Product p) {
        return """
                Bạn có thể tham khảo sản phẩm này:
                
                • **%s**
                %s
                
                Bạn muốn xem thêm mẫu khác hay lọc theo nhu cầu cụ thể?
                """.formatted(p.getName(), p.getDescription());
    }
}