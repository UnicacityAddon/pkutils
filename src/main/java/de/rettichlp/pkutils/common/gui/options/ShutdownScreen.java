package de.rettichlp.pkutils.common.gui.options;

import de.rettichlp.pkutils.common.gui.PKUtilsScreen;
import de.rettichlp.pkutils.common.models.ShutdownReason;
import net.minecraft.client.gui.widget.DirectionalLayoutWidget;
import net.minecraft.client.gui.widget.TextWidget;

import static de.rettichlp.pkutils.PKUtils.renderService;
import static de.rettichlp.pkutils.PKUtils.storage;
import static net.minecraft.client.gui.widget.DirectionalLayoutWidget.vertical;
import static net.minecraft.text.Text.empty;
import static net.minecraft.text.Text.of;
import static net.minecraft.util.Formatting.GOLD;
import static net.minecraft.util.Formatting.GRAY;

public class ShutdownScreen extends PKUtilsScreen {

    private final ShutdownReason shutdownReason;

    public ShutdownScreen(ShutdownReason shutdownReason) {
        super(of("Automatisches Herunterfahren"), of(shutdownReason.getDisplayName()));
        this.shutdownReason = shutdownReason;
    }

    @Override
    public void initBody() {
        DirectionalLayoutWidget directionalLayoutWidget = this.layout.addBody(vertical().spacing(4));
        directionalLayoutWidget.getMainPositioner().alignHorizontalCenter();

        directionalLayoutWidget.add(new TextWidget(empty()
                .append(of("Das Spiel wird beendet und der PC heruntergefahren,").copy().formatted(GRAY)), this.textRenderer), positioner -> positioner.marginTop(16));

        directionalLayoutWidget.add(new TextWidget(empty()
                .append(of("wenn folgende Bedingung erfüllt ist:").copy().formatted(GRAY)), this.textRenderer), positioner -> positioner.marginBottom(16));

        directionalLayoutWidget.add(new TextWidget(of(this.shutdownReason.getConditionString()).copy().formatted(GOLD), this.textRenderer));

        directionalLayoutWidget.add(new TextWidget(empty()
                .append(of("Wenn du dieses Fenster schließt,").copy().formatted(GRAY)), this.textRenderer), positioner -> positioner.marginTop(16));

        directionalLayoutWidget.add(new TextWidget(empty()
                .append(of("wird das automatische Herunterfahren gestoppt.").copy().formatted(GRAY)), this.textRenderer), positioner -> positioner.marginBottom(16));

        renderService.addButton(directionalLayoutWidget, "pkutils.screen.button.shutdown.abort.name", button -> storage.getActiveShutdowns().clear(), 150);

        directionalLayoutWidget.forEachChild(this::addDrawableChild);
    }

    @Override
    public void doOnClose() {
        storage.getActiveShutdowns().removeIf(sr -> sr == this.shutdownReason);
    }
}
