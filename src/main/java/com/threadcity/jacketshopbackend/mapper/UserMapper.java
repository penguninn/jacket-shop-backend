package com.threadcity.jacketshopbackend.mapper;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.threadcity.jacketshopbackend.common.Enums.Status;
import com.threadcity.jacketshopbackend.entity.Role;
import com.threadcity.jacketshopbackend.entity.User;
import com.threadcity.jacketshopbackend.service.auth.UserDetailsImpl;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "enabled", source = "status", qualifiedByName = "statusToEnabled")
    @Mapping(target = "authorities", source = "roles", qualifiedByName = "rolesToAuthorities")
    UserDetailsImpl toUserDetailsImpl(User user);

    @Named("statusToEnabled")
    public static boolean statusToEnabled(Status status) {
        return status == Status.ACTIVE;
    }

    @Named("rolesToAuthorities")
    public static List<GrantedAuthority> rolesToAuthorities(Set<Role> roles) {
        return roles == null ? List.of()
                : roles.stream()
                        .map(r -> new SimpleGrantedAuthority("ROLE_" + r.getName()))
                        .collect(Collectors.toList());
    }
}
