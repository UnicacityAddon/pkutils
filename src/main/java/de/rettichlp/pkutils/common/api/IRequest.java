package de.rettichlp.pkutils.common.api;

import org.jetbrains.annotations.Nullable;

import java.net.URI;

import static de.rettichlp.pkutils.PKUtils.api;

public interface IRequest {

    URI getUrl();

    @Nullable
    default String bodyMapper() {
        return api.getGson().toJson(this);
    }
}
