package de.rettichlp.pkutils.common.gui.widgets.alignment;

import de.rettichlp.pkutils.common.gui.widgets.base.AbstractPKUtilsWidget;

import java.util.ArrayList;
import java.util.List;

public abstract class AlignWidget<T> extends AbstractPKUtilsWidget {

    protected final List<AbstractPKUtilsWidget> pkUtilsWidgets = new ArrayList<>();
    protected boolean disableMargin = false;

    public abstract void add(T entry);

    public void clear() {
        this.pkUtilsWidgets.clear();
    }

    public AlignWidget<T> disableMargin() {
        this.disableMargin = true;
        return this;
    }
}
