package com.example.bankcards.mapper;

import com.example.bankcards.dto.request.CreateCardRequest;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.UUID;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CardMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", source = "userId")
    @Mapping(target = "numberHash", ignore = true)
    @Mapping(target = "lastFour", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "balance", ignore = true)
    @Mapping(target = "version", ignore = true)
    Card toEntity(CreateCardRequest request);

    default User map(UUID userId) {
        if (userId == null) return null;
        var user = new User();
        user.setId(userId);
        return user;
    }
}
