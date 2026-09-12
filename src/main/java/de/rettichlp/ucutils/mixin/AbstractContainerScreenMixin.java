package de.rettichlp.ucutils.mixin;

import de.rettichlp.ucutils.common.models.StockMarketEntry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;
import org.joml.Vector2i;
import org.joml.Vector2ic;
import org.jspecify.annotations.NonNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.Color;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.stream.Stream;

import static de.rettichlp.ucutils.UCUtils.LOGGER;
import static de.rettichlp.ucutils.UCUtils.commandService;
import static de.rettichlp.ucutils.UCUtils.configuration;
import static de.rettichlp.ucutils.UCUtils.player;
import static de.rettichlp.ucutils.UCUtils.storage;
import static de.rettichlp.ucutils.common.models.StockMarketEntry.fromItemStack;
import static java.awt.Color.BLUE;
import static java.awt.Color.MAGENTA;
import static java.lang.Integer.parseInt;
import static java.util.Optional.ofNullable;
import static java.util.regex.Pattern.compile;
import static net.minecraft.ChatFormatting.DARK_GRAY;
import static net.minecraft.ChatFormatting.DARK_GREEN;
import static net.minecraft.ChatFormatting.DARK_RED;
import static net.minecraft.ChatFormatting.GOLD;
import static net.minecraft.ChatFormatting.GRAY;
import static net.minecraft.ChatFormatting.GREEN;
import static net.minecraft.ChatFormatting.RED;
import static net.minecraft.ChatFormatting.YELLOW;
import static net.minecraft.core.component.DataComponents.LORE;
import static net.minecraft.network.chat.Component.empty;
import static net.minecraft.network.chat.Component.literal;
import static net.minecraft.network.chat.Component.translatable;
import static net.minecraft.world.inventory.ContainerInput.PICKUP;
import static net.minecraft.world.item.Items.PLAYER_HEAD;

@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerScreenMixin<T extends AbstractContainerMenu> extends Screen implements MenuAccess<T> {

    @Shadow
    @Final
    protected int imageWidth;

    @Shadow
    protected int leftPos;

    @Shadow
    protected int topPos;

    protected AbstractContainerScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void ucutils$extractRenderStateTail(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a, CallbackInfo ci) {
        MultiPlayerGameMode gameMode = Minecraft.getInstance().gameMode;

        if (gameMode == null) {
            return;
        }

        String title = getTitle().getString();

        switch (getTitle().getString()) {
            case "ʟᴀɢᴇʀ" -> extractMedicStorageButton(graphics, mouseX, mouseY, a);
            case "ᴄᴀʀᴄᴏɴᴛʀᴏʟ" -> {
                if (configuration.getOptions().car().fastLock() && !storage.isPremium()) {
                    gameMode.handleContainerInput(getMenu().containerId, 0, 0, PICKUP, player);
                }
            }
            case "ᴀᴋᴛɪᴇɴᴍᴀʀᴋᴛ", "Handy-Aktien" -> {
                storage.setStockMarketCommandRunning(false);
                extractStockMarketHighlight(graphics, mouseX, mouseY, a);
                extractStockMarketLegend(graphics, mouseX, mouseY, a);
            }
            case "ʜᴀɴᴅʏ » üʙᴇʀsɪᴄʜᴛ" -> {
                if (storage.isStockMarketCommandRunning()) {
                    clickOnItemWithName("ᴀᴘᴘs", gameMode);
                }
            }
            case "ʜᴀɴᴅʏ » ᴀᴘᴘ-ᴍᴇɴü" -> {
                if (storage.isStockMarketCommandRunning()) {
                    clickOnItemWithName("Aktien", gameMode);
                }
            }
            case "Mülleimer" -> {
                Set<String> autoCollectFromTrashCan = configuration.getOptions().autoCollectFromTrashCan();

                for (int i = 0; i < 5; i++) {
                    ItemStack itemStack = getMenu().slots.get(i).getItem();
                    boolean shouldCollect = autoCollectFromTrashCan.contains(itemStack.getItem().toString());
                    if (shouldCollect) {
                        gameMode.handleContainerInput(getMenu().containerId, i, 0, PICKUP, player);
                    }
                }
            }
            case "Durchsuchung" -> extractTrunkHighlight(graphics, mouseX, mouseY, a);
            default -> {
                if (commandService.isSuperUser()) {
                    LOGGER.info("Screen opened: {}", title);
                }
            }
        }
    }

    @Unique
    private void extractMedicStorageButton(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        int ingredient1StoredAmount = getStoredAmount(this, 11);
        int ingredient2StoredAmount = getStoredAmount(this, 13);
        int ingredient3StoredAmount = getStoredAmount(this, 15);

        // render button right to the inventory
        Button button = new Button.Builder(literal("➤"), _ -> commandService.sendCommand("f " + ingredient1StoredAmount + "x Wirkstoff | " + ingredient2StoredAmount + "x Trägerstoff | " + ingredient3StoredAmount + "x Zusatzstoff"))
                .bounds(this.leftPos + this.imageWidth + 2, this.topPos, 20, 20)
                .build();

        if (ingredient1StoredAmount != 0 && ingredient2StoredAmount != 0 && ingredient3StoredAmount != 0) {
            button.extractRenderState(graphics, mouseX, mouseY, a);
            addRenderableWidget(button);
        }
    }

    @Unique
    private void extractStockMarketHighlight(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        for (Slot slot : player.containerMenu.slots) {
            ItemStack itemStack = slot.getItem();

            if (itemStack.isEmpty() || !itemStack.is(PLAYER_HEAD) || itemStack.getCustomName() == null) {
                continue;
            }

            Optional<Color> optionalColor = ofNullable(fromItemStack(itemStack))
                    .map(StockMarketEntry::getColor);

            if (optionalColor.isEmpty()) {
                continue;
            }

            int x = this.leftPos + slot.x;
            int y = this.topPos + slot.y;

            Color color = optionalColor.get();
            int argb = (0x80 << 24) | (color.getRGB() & 0x00FFFFFF);
            graphics.fill(x, y, x + 16, y + 16, argb);
        }
    }

    @Unique
    private void extractStockMarketLegend(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        Stream<Component> legendComponents = Stream.of(
                empty()
                        .append(translatable("ucutils.legend").withStyle(GRAY))
                        .append(literal(":").withStyle(DARK_GRAY)),
                empty()
                        .append(literal(" • ").withStyle(DARK_GRAY))
                        .append(translatable("ucutils.minimum_price").withColor(BLUE.getRGB())),
                empty()
                        .append(literal(" • ").withStyle(DARK_GRAY))
                        .append(translatable("ucutils.price_difference").withStyle(GRAY))
                        .append(literal(": ").withStyle(DARK_GRAY))
                        .append(literal("Δp ≥ 75$").withStyle(DARK_GREEN)),
                empty()
                        .append(literal(" • ").withStyle(DARK_GRAY))
                        .append(translatable("ucutils.price_difference").withStyle(GRAY))
                        .append(literal(": ").withStyle(DARK_GRAY))
                        .append(literal("Δp ≥ 60$").withStyle(GREEN)),
                empty()
                        .append(literal(" • ").withStyle(DARK_GRAY))
                        .append(translatable("ucutils.price_difference").withStyle(GRAY))
                        .append(literal(": ").withStyle(DARK_GRAY))
                        .append(literal("Δp ≥ 45$").withStyle(YELLOW)),
                empty()
                        .append(literal(" • ").withStyle(DARK_GRAY))
                        .append(translatable("ucutils.price_difference").withStyle(GRAY))
                        .append(literal(": ").withStyle(DARK_GRAY))
                        .append(literal("Δp ≥ 30$").withStyle(GOLD)),
                empty()
                        .append(literal(" • ").withStyle(DARK_GRAY))
                        .append(translatable("ucutils.price_difference").withStyle(GRAY))
                        .append(literal(": ").withStyle(DARK_GRAY))
                        .append(literal("Δp ≥ 15$").withStyle(RED)),
                empty()
                        .append(literal(" • ").withStyle(DARK_GRAY))
                        .append(translatable("ucutils.price_difference").withStyle(GRAY))
                        .append(literal(": ").withStyle(DARK_GRAY))
                        .append(literal("Δp < 15$").withStyle(DARK_RED)));

        List<ClientTooltipComponent> legendClientTooltipComponents = legendComponents
                .map(Component::getVisualOrderText)
                .map(ClientTooltipComponent::create)
                .toList();

        graphics.tooltip(this.minecraft.font, legendClientTooltipComponents, this.leftPos + this.imageWidth + 2, this.topPos, new CompanyShareTooltipPositioner(), null);
    }

    @Unique
    private void extractTrunkHighlight(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        for (Slot slot : player.containerMenu.slots) {
            ItemStack itemStack = slot.getItem();
            ItemLore itemLore = itemStack.get(LORE);

            if (itemStack.isEmpty() || itemStack.getCustomName() == null || itemLore == null) {
                continue;
            }

            List<Component> lines = itemLore.lines();
            boolean amountZeroOrNotMatching = lines.stream()
                    .map(Component::getString)
                    .anyMatch(loreLineString -> loreLineString.equals("0g") || !loreLineString.matches("^\\d+g$"));

            if (amountZeroOrNotMatching) {
                continue;
            }

            int x = this.leftPos + slot.x;
            int y = this.topPos + slot.y;

            int argb = (0x80 << 24) | (MAGENTA.getRGB() & 0x00FFFFFF);
            graphics.fill(x, y, x + 16, y + 16, argb);
        }
    }

    @Unique
    private int getStoredAmount(@NonNull MenuAccess<T> containerScreen, int slotId) {
        Slot slot = containerScreen.getMenu().getSlot(slotId);
        ItemLore itemLore = slot.getItem().get(LORE);

        if (itemLore == null) {
            return 0;
        }

        String amountString = itemLore.lines().get(1).getString();
        Matcher matcher = compile("\\d+").matcher(amountString);
        return matcher.find() ? parseInt(matcher.group()) : 0;
    }

    @Unique
    private void clickOnItemWithName(String itemName, MultiPlayerGameMode gameMode) {
        getMenu().slots.stream()
                .filter(slot -> slot.getItem().getHoverName().getString().equals(itemName))
                .findFirst()
                .ifPresent(slot -> gameMode.handleContainerInput(getMenu().containerId, slot.index, 0, PICKUP, player));
    }

    private static class CompanyShareTooltipPositioner implements ClientTooltipPositioner {

        @Override
        public @NonNull Vector2ic positionTooltip(int screenWidth,
                                                  int screenHeight,
                                                  int x,
                                                  int y,
                                                  int tooltipWidth,
                                                  int tooltipHeight) {
            return new Vector2i(x, y).add(12, 8);
        }
    }
}
