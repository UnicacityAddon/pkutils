package de.rettichlp.pkutils.common.gui.options;

import de.rettichlp.pkutils.common.models.Color;
import de.rettichlp.pkutils.common.models.Faction;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.gui.widget.DirectionalLayoutWidget;
import net.minecraft.client.gui.widget.GridWidget;
import net.minecraft.client.gui.widget.Positioner;
import net.minecraft.client.gui.widget.TextWidget;
import org.jetbrains.annotations.NotNull;

import static de.rettichlp.pkutils.PKUtilsClient.configService;
import static de.rettichlp.pkutils.common.models.Color.WHITE;
import static de.rettichlp.pkutils.common.models.Faction.NULL;
import static java.util.Arrays.stream;
import static net.minecraft.client.gui.widget.DirectionalLayoutWidget.horizontal;
import static net.minecraft.client.gui.widget.DirectionalLayoutWidget.vertical;
import static net.minecraft.text.Text.of;
import static net.minecraft.text.Text.translatable;

public class NameTagOptionsScreen extends OptionsScreen {

    public NameTagOptionsScreen(Screen parent) {
        super(parent, "pkutils.options.nametag.title");
    }

    @Override
    public void initBody() {
        DirectionalLayoutWidget directionalLayoutWidget = this.layout.addBody(vertical().spacing(4));

        directionalLayoutWidget.add(new TextWidget(translatable("pkutils.options.text.faction"), this.textRenderer), Positioner::alignHorizontalCenter);

        addToggleButton(directionalLayoutWidget, "pkutils.options.nametag.faction.information", (options, value) -> options.nameTag().factionInformation(value), options -> options.nameTag().factionInformation(), 308);

        directionalLayoutWidget.add(new TextWidget(translatable("pkutils.options.text.color"), this.textRenderer), positioner -> positioner.alignHorizontalCenter().marginTop(16));
        directionalLayoutWidget.add(getFactionColorOptions());

        directionalLayoutWidget.add(new TextWidget(translatable("pkutils.options.text.additional"), this.textRenderer), positioner -> positioner.alignHorizontalCenter().marginTop(16));

        DirectionalLayoutWidget directionalLayoutWidget3 = directionalLayoutWidget.add(horizontal().spacing(8));
        addToggleButton(directionalLayoutWidget3, "pkutils.options.nametag.additional.blacklist", (options, value) -> options.nameTag().additionalBlacklist(value), options -> options.nameTag().additionalBlacklist(), 150);
        addToggleButton(directionalLayoutWidget3, "pkutils.options.nametag.additional.contract", (options, value) -> options.nameTag().additionalContract(value), options -> options.nameTag().additionalContract(), 150);

        DirectionalLayoutWidget directionalLayoutWidget4 = directionalLayoutWidget.add(horizontal().spacing(8));
        addToggleButton(directionalLayoutWidget4, "pkutils.options.nametag.additional.houseban", (options, value) -> options.nameTag().additionalHouseban(value), options -> options.nameTag().additionalHouseban(), 150);
        addToggleButton(directionalLayoutWidget4, "pkutils.options.nametag.additional.wanted", (options, value) -> options.nameTag().additionalWanted(value), options -> options.nameTag().additionalWanted(), 150);

        directionalLayoutWidget.forEachChild(this::addDrawableChild);
    }

    private @NotNull GridWidget getFactionColorOptions() {
        GridWidget gridWidget = new GridWidget();
        gridWidget.getMainPositioner().marginX(4).marginBottom(4).alignHorizontalCenter();
        GridWidget.Adder adder = gridWidget.createAdder(2);

        stream(Faction.values())
                .filter(faction -> faction != NULL)
                .map(faction -> new CyclingButtonWidget.Builder<>(Color::getDisplayName)
                        .values(Color.values())
                        .initially(configService.load().getOptions().nameTag().highlightFactions().getOrDefault(faction, WHITE))
                        .build(of(faction.getDisplayName()), (button, value) -> configService.edit(mainConfig -> mainConfig.getOptions().nameTag().highlightFactions().put(faction, value))))
                .forEach(adder::add);

        return gridWidget;
    }
}
