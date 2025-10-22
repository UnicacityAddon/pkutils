package de.rettichlp.pkutils.common.gui.options;

import de.rettichlp.pkutils.common.gui.OptionsScreen;
import de.rettichlp.pkutils.common.models.InventoryItem;
import de.rettichlp.pkutils.common.models.PersonalUseEntry;
import de.rettichlp.pkutils.common.models.Purity;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.DirectionalLayoutWidget;
import net.minecraft.client.gui.widget.EmptyWidget;
import net.minecraft.client.gui.widget.Positioner;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static de.rettichlp.pkutils.PKUtils.LOGGER;
import static de.rettichlp.pkutils.PKUtils.configuration;
import static de.rettichlp.pkutils.PKUtils.renderService;
import static java.lang.Integer.parseInt;
import static java.lang.String.valueOf;
import static java.util.Arrays.stream;
import static net.minecraft.client.gui.widget.DirectionalLayoutWidget.horizontal;
import static net.minecraft.client.gui.widget.DirectionalLayoutWidget.vertical;
import static net.minecraft.text.Text.empty;
import static net.minecraft.text.Text.of;
import static net.minecraft.text.Text.translatable;

public class PersonalUseOptionsScreen extends OptionsScreen {

    private static final Text TEXT_PERSONAL_USE = translatable("pkutils.options.text.personal_use");
    private static final Text TEXT_PURITY = translatable("pkutils.options.text.purity");
    private static final Text TEXT_AMOUNT = translatable("pkutils.options.text.amount");

    public PersonalUseOptionsScreen(Screen parent) {
        super(parent, TEXT_PERSONAL_USE);
    }

    @Override
    public void initBody() {
        DirectionalLayoutWidget directionalLayoutWidget = this.layout.addBody(vertical().spacing(4));

        DirectionalLayoutWidget directionalLayoutWidget1 = directionalLayoutWidget.add(horizontal().spacing(8));

        EmptyWidget widget = new EmptyWidget(100, 0);
        directionalLayoutWidget1.add(widget);

        TextWidget widget1 = new TextWidget(TEXT_PURITY, this.textRenderer);
        widget1.setWidth(96);
        directionalLayoutWidget1.add(widget1, positioner -> positioner.alignVerticalCenter().alignHorizontalCenter());

        TextWidget widget2 = new TextWidget(TEXT_AMOUNT, this.textRenderer);
        widget2.setWidth(96);
        directionalLayoutWidget1.add(widget2, positioner -> positioner.alignVerticalCenter().alignHorizontalCenter());

        stream(InventoryItem.values())
                .filter(InventoryItem::isDrugBankItem)
                .forEach(inventoryItem -> directionalLayoutWidget.add(personalUseRow(inventoryItem)));

        directionalLayoutWidget.forEachChild(this::addDrawableChild);
    }

    private @NotNull DirectionalLayoutWidget personalUseRow(@NotNull InventoryItem inventoryItem) {
        PersonalUseEntry personalUseEntry = configuration.getOptions().personalUse().stream()
                .filter(oue -> oue.getInventoryItem() == inventoryItem)
                .findFirst()
                .orElseGet(() -> new PersonalUseEntry(inventoryItem));

        DirectionalLayoutWidget directionalLayoutWidget = horizontal().spacing(8);

        // text
        TextWidget widget = new TextWidget(translatable("pkutils.inventory." + inventoryItem.name().toLowerCase()), this.textRenderer);
        widget.setWidth(100);
        widget.alignLeft();
        directionalLayoutWidget.add(widget, Positioner::alignVerticalCenter);

        // purity input
        renderService.addCyclingButton(directionalLayoutWidget, TEXT_PURITY, Purity.values(), Purity::getDisplayName, (options, value) -> updatePersonalUseEntry(personalUseEntry, value), options -> personalUseEntry.getPurity(), 96);

        // amount input
        DirectionalLayoutWidget widget2 = personalUseInput(personalUseEntry);
        directionalLayoutWidget.add(widget2);

        return directionalLayoutWidget;
    }

    private @NotNull DirectionalLayoutWidget personalUseInput(@NotNull PersonalUseEntry personalUseEntry) {
        DirectionalLayoutWidget directionalLayoutWidget = horizontal();

        TextFieldWidget widget = new TextFieldWidget(this.textRenderer, 40, 20, empty());
        widget.setWidth(80);
        widget.setChangedListener(value -> updatePersonalUseEntry(personalUseEntry, value));
        widget.setEditable(true);
        widget.setPlaceholder(of(valueOf(personalUseEntry.getAmount())));

        TextWidget widget1 = new TextWidget(of(" g"), this.textRenderer);
        widget1.setWidth(16);
        widget1.alignLeft();

        directionalLayoutWidget.add(widget);
        directionalLayoutWidget.add(widget1, Positioner::alignVerticalCenter);

        return directionalLayoutWidget;
    }

    private void updatePersonalUseEntry(@NotNull PersonalUseEntry personalUseEntry, Purity newPurity) {
        personalUseEntry.setPurity(newPurity);
        List<PersonalUseEntry> personalUseEntries = configuration.getOptions().personalUse();
        personalUseEntries.removeIf(pue -> pue.getInventoryItem() == personalUseEntry.getInventoryItem());
        personalUseEntries.add(personalUseEntry);
    }

    private void updatePersonalUseEntry(@NotNull PersonalUseEntry personalUseEntry, String newAmount) {
        int parsedInt;

        try {
            parsedInt = parseInt(newAmount);
        } catch (NumberFormatException e) {
            LOGGER.warn("Could not parse personal use amount: {}", newAmount);
            parsedInt = -1;
        }

        // skip config edit if the amount did not change or is negative
        if (parsedInt == personalUseEntry.getAmount() || parsedInt < 0) {
            LOGGER.info("Skipping personal use config edit, amount did not change or is negative (newAmount: {}, currentAmount: {})", parsedInt, personalUseEntry.getAmount());
            return;
        }

        personalUseEntry.setAmount(parsedInt);
        List<PersonalUseEntry> personalUseEntries = configuration.getOptions().personalUse();
        personalUseEntries.removeIf(pue -> pue.getInventoryItem() == personalUseEntry.getInventoryItem());
        personalUseEntries.add(personalUseEntry);
    }
}
