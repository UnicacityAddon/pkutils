package de.rettichlp.pkutils.common.api.response;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class GetUserInfoResponse {

    private final GetUserInfoMinecraft getUserInfoMinecraft;
    private final List<String> roles;
    private final String version;

    @Data
    public static class GetUserInfoMinecraft {

        public UUID uuid;
        public String name;
    }
}
