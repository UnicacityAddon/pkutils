package de.rettichlp.pkutils.common.registry;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface PKUtilsCommand {

    String label();

    String[] aliases() default {};
}
