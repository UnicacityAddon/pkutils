package de.rettichlp.pkutils.common.api.response;

import lombok.Data;

import java.util.List;
import java.util.UUID;

public record GetUserInfoResponse(GetUserInfoMinecraft getUserInfoMinecraft, List<String> roles, String version) {

    @Data
    public static class GetUserInfoMinecraft {

        public UUID uuid;
        public String name;
    }
}
