package com.threadcity.jacketshopbackend.dto.request.common;

import com.threadcity.jacketshopbackend.common.Enums;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class UpdateStatusRequest implements Serializable {

    private Enums.Status status;
}
