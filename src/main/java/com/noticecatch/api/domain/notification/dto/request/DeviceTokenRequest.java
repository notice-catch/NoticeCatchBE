package com.noticecatch.api.domain.notification.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class DeviceTokenRequest {
    private String pushToken;
}