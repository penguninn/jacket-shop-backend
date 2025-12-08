package com.threadcity.jacketshopbackend.dto.request;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.Set;

@Data
@Builder
public class UserUpdateRolesRequest implements Serializable {

    private Set<Long> roleIds;
}
