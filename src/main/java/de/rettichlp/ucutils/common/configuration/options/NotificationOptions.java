package de.rettichlp.ucutils.common.configuration.options;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(fluent = true)
public class NotificationOptions {

    private boolean joinQuit = true;
    private boolean aDuty = false;
    private boolean report = false;
    private boolean buildMode = false;
    private boolean jobCooldown = true;
}
