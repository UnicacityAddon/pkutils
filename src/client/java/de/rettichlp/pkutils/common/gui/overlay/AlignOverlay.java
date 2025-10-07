package de.rettichlp.pkutils.common.gui.overlay;

import java.util.ArrayList;
import java.util.List;

public abstract class AlignOverlay<T> extends OverlayEntry {

    protected final List<OverlayEntry> overlayEntries = new ArrayList<>();

    public abstract void add(T entry);
}
