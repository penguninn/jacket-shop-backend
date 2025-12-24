package com.threadcity.jacketshopbackend.entity.chatbot;

import lombok.Getter;
import lombok.Setter;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
@Scope("session")
@Getter
@Setter
public class ChatbotContext {

    public enum ChatIntent {
        INTRO,
        MORE_PRODUCTS,
        STYLE_SELECTION,
        BRAND_SELECTION,
        FREE_CHAT
    }

    private ChatIntent lastIntent = ChatIntent.FREE_CHAT;
    private Set<Long> shownProductIds = new HashSet<>();
    private String lastKeyword = ""; // lưu style/brand đang chọn
}
