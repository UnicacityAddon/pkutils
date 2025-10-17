package de.rettichlp.pkutils.common.gui.widgets.base;

import org.atteo.classindex.IndexAnnotated;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@IndexAnnotated
public @interface PKUtilsWidget {

    String registryName();
}
