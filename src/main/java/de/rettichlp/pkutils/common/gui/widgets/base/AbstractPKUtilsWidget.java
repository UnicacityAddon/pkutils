package de.rettichlp.pkutils.common.gui.widgets.base;

import com.google.common.reflect.TypeToken;
import lombok.Getter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;

import static de.rettichlp.pkutils.PKUtils.LOGGER;
import static de.rettichlp.pkutils.PKUtils.api;
import static de.rettichlp.pkutils.PKUtils.configuration;
import static de.rettichlp.pkutils.PKUtils.notificationService;
import static de.rettichlp.pkutils.common.gui.widgets.base.AbstractPKUtilsWidget.Alignment.CENTER;
import static de.rettichlp.pkutils.common.gui.widgets.base.AbstractPKUtilsWidget.Alignment.LEFT;
import static de.rettichlp.pkutils.common.gui.widgets.base.AbstractPKUtilsWidget.Alignment.RIGHT;
import static java.util.Objects.isNull;
import static java.util.Optional.ofNullable;
import static net.minecraft.text.Text.translatable;

@Getter
public abstract class AbstractPKUtilsWidget<C extends PKUtilsWidgetConfiguration> {

    private static final Type MAP_TYPE = new TypeToken<Map<String, Object>>() {}.getType();

    private final TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;

    private C widgetConfiguration;

    public abstract Text getDisplayName();

    public abstract Text getTooltip();

    public abstract int getWidth();

    public abstract int getHeight();

    public abstract void draw(@NotNull DrawContext drawContext, int x, int y, Alignment alignment);

    public void init() {
        loadConfiguration();
    }

    public void draw(@NotNull DrawContext drawContext) {
        if (!isVisible() || !this.widgetConfiguration.isEnabled() || MinecraftClient.getInstance().options.hudHidden) {
            return;
        }

        int x = (int) this.widgetConfiguration.getX();
        int y = (int) this.widgetConfiguration.getY();
        draw(drawContext, x, y, getAlignment());
    }

    public boolean isMouseOver(double mouseX, double mouseY) {
        double x = this.widgetConfiguration.getX();
        double y = this.widgetConfiguration.getY();
        boolean mouseOverX = mouseX >= x && mouseX <= x + getWidth();
        boolean mouseOverY = mouseY >= y && mouseY <= y + getHeight();
        return mouseOverX && mouseOverY;
    }

    public boolean isVisible() {
        return true;
    }

    public String getRegistryName() {
        return ofNullable(this.getClass().getAnnotation(PKUtilsWidget.class))
                .map(PKUtilsWidget::registryName)
                .orElseThrow(() -> new IllegalStateException("Widget class " + this.getClass().getName() + " has no registry name"));
    }

    public void loadConfiguration() {
        String registryName = getRegistryName();

        if (isNull(registryName)) {
            LOGGER.warn("Widget {} is missing registry name and therefore has no configuration", this.getClass().getName());
            return;
        }

        Class<C> widgetConfigurationClass = getConfigurationClass();
        // load configuration from the configuration file - not from the cache
        Object widgetConfigurationObject = configuration.loadFromFile().getWidgets().get(registryName);

        if (isNull(widgetConfigurationObject)) {
            LOGGER.info("No configuration found for widget {}, using default configuration", registryName);

            try {
                this.widgetConfiguration = widgetConfigurationClass.getConstructor().newInstance();
                this.widgetConfiguration.setX(getDefaultX());
                this.widgetConfiguration.setY(getDefaultY());
            } catch (Exception e) {
                notificationService.sendErrorNotification("Konfiguration konnte nicht geladen werden");
                LOGGER.error("Could not load configuration for widget {}", registryName, e);
            }

            return;
        }

        String widgetConfigurationJson = api.getGson().toJson(widgetConfigurationObject);

        this.widgetConfiguration = api.getGson().fromJson(widgetConfigurationJson, widgetConfigurationClass);
    }

    public void saveConfiguration() {
        String registryName = getRegistryName();

        if (isNull(registryName)) {
            LOGGER.warn("Widget {} is missing registry name and therefore has no configuration", this.getClass().getName());
            return;
        }

        C widgetConfiguration = getWidgetConfiguration();
        widgetConfiguration.setX(roundToNearestHalf(widgetConfiguration.getX()));
        widgetConfiguration.setY(roundToNearestHalf(widgetConfiguration.getY()));

        String widgetConfigurationJson = api.getGson().toJson(widgetConfiguration);
        Map<String, Object> widgetConfigurationMap = api.getGson().fromJson(widgetConfigurationJson, MAP_TYPE);
        configuration.getWidgets().put(registryName, widgetConfigurationMap);
    }

    private Alignment getAlignment() {
        int scaledWidth = MinecraftClient.getInstance().getWindow().getScaledWidth();
        int widthSegment = scaledWidth / 3;

        Alignment alignment;

        double x = this.widgetConfiguration.getX();
        if (x <= widthSegment) {
            alignment = LEFT;
        } else if (x <= widthSegment * 2) {
            alignment = CENTER;
        } else {
            alignment = RIGHT;
        }

        return alignment;
    }

    private double getDefaultX() {
        return ofNullable(this.getClass().getAnnotation(PKUtilsWidget.class))
                .map(PKUtilsWidget::defaultX)
                .orElse(0.0);
    }

    private double getDefaultY() {
        return ofNullable(this.getClass().getAnnotation(PKUtilsWidget.class))
                .map(PKUtilsWidget::defaultY)
                .orElse(0.0);
    }

    @SuppressWarnings("unchecked")
    private Class<C> getConfigurationClass() {
        Type type = getClass().getGenericSuperclass();

        if (type instanceof ParameterizedType) {
            Type[] typeArgs = ((ParameterizedType) type).getActualTypeArguments();
            if (typeArgs.length > 0 && typeArgs[0] instanceof Class) {
                return (Class<C>) typeArgs[0];
            }
        }

        throw new IllegalStateException("Widget class must be generic: AbstractPKUtilsWidget<C>");
    }

    private double roundToNearestHalf(double value) {
        return Math.round(value * 2) / 2.0;
    }

    public enum Alignment {

        LEFT,
        CENTER,
        RIGHT
    }
}
