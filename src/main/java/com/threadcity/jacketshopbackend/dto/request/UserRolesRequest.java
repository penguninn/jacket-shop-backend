package com.threadcity.jacketshopbackend.dto.request;

import java.io.Serializable;
import java.util.Set;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserRolesRequest implements Serializable {

    private Set<Long> roleIds;
}
