package com.threadcity.jacketshopbackend.dto.request;

import com.threadcity.jacketshopbackend.common.Enums;
import lombok.Data;

import java.io.Serializable;

@Data
public class CategoryStatusRequest implements Serializable {
    private Enums.Status status;
}
