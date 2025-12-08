package com.threadcity.jacketshopbackend.dto.request;

import com.threadcity.jacketshopbackend.common.Enums.Status;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
public class UserStatusRequest implements Serializable {

    private Status status;
}
