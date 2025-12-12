package com.threadcity.jacketshopbackend.dto.goship;

import lombok.Data;
import java.util.List;

@Data
public class GoshipResponse<T> {
    private int code;
    private String status;
    private List<T> data;
}
