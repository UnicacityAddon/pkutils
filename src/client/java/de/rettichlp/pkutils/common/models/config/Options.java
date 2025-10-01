package de.rettichlp.pkutils.common.models.config;

import lombok.Getter;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
public class Options {

    private final NameTagOptions nameTag = new NameTagOptions();
}
