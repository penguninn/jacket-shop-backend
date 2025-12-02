package com.threadcity.jacketshopbackend.dto.request;

import java.io.Serializable;

import com.threadcity.jacketshopbackend.common.Enums.Status;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserStatusRequest implements Serializable {

    private Status status;
}
