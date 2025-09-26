package de.rettichlp.pkutils.common.api.request;

import de.rettichlp.pkutils.common.api.IRequest;
import de.rettichlp.pkutils.common.models.EquipLog;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.net.URI;

import static de.rettichlp.pkutils.PKUtilsClient.api;
import static java.net.URI.create;

public record EquipLogAddRequest(EquipLog.Type equipLogType) implements IRequest {

    @Contract(" -> new")
    @Override
    public @NotNull URI getUrl() {
        return create(api.getBaseUrl() + "/equiplog/add");
    }
}
