package de.rettichlp.pkutils.common.configuration.options;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(fluent = true)
public class CarOptions {

    private boolean fastFind = true;
    private boolean fastLock = true;
    private boolean highlight = true;
    private boolean automatedLock = true;
    private boolean automatedStart = true;
}
