package de.rettichlp.pkutils.listener.impl.job;

import de.rettichlp.pkutils.common.registry.PKUtilsBase;
import de.rettichlp.pkutils.common.registry.PKUtilsListener;
import de.rettichlp.pkutils.listener.IMessageReceiveListener;
import de.rettichlp.pkutils.listener.INaviSpotReachedListener;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static de.rettichlp.pkutils.PKUtilsClient.networkHandler;
import static de.rettichlp.pkutils.PKUtilsClient.player;
import static java.util.concurrent.TimeUnit.SECONDS;
import static java.util.regex.Pattern.compile;

@PKUtilsListener
public class TransportListener extends PKUtilsBase implements IMessageReceiveListener, INaviSpotReachedListener {

    private static final Pattern TRANSPORT_DELIVER_PATTERN = compile("^\\[Transport] Du hast eine (Kiste|Waffenkiste) abgeliefert\\.$" +
            "|^\\[Transport] Du hast ein Weizen Paket abgeliefert\\.$" +
            "|^\\[Transport] Du hast eine Schwarzpulverkiste abgeliefert\\.$");
    private static final Pattern DRINK_TRANSPORT_DELIVER_PATTERN = compile("^\\[Bar] Du hast eine Flasche abgegeben!$");
    private static final Pattern TABAK_JOB_TRANSPORT_START_PATTERN = compile("^\\[Tabakplantage] Bringe es nun zur Shishabar und gib es mit /droptabak ab\\.$");
    private static final Pattern PIZZA_JOB_TRANSPORT_START_PATTERN = compile("^\\[Pizzalieferant] Hier kannst du die frischen Pizzen mit /getpizza abholen\\.$");
    private static final Pattern PIZZA_JOB_TRANSPORT_GET_PIZZA_PATTERN = compile("^\\[Pizzalieferant] Sobald du 10 Pizzen dabei hast, wird dir deine erste Route angezeigt\\.$");
    private boolean isTabakJobTransportActive = false;
    private boolean isPizzaJobTransportActive = false;

    @Override
    public boolean onMessageReceive(Text text, String message) {

        Matcher transportDeliverMatcher = TRANSPORT_DELIVER_PATTERN.matcher(message);
        if (transportDeliverMatcher.find()) {
            delayedAction(() -> networkHandler.sendChatCommand("droptransport"), SECONDS.toMillis(10));
            return true;
        }

        Matcher drinkTransportDeliverMatcher = DRINK_TRANSPORT_DELIVER_PATTERN.matcher(message);
        if (drinkTransportDeliverMatcher.find()) {
            delayedAction(() -> networkHandler.sendChatCommand("dropdrink"), 2500);
            return true;
        }

        Matcher tabakJobTransportStartMatcher = TABAK_JOB_TRANSPORT_START_PATTERN.matcher(message);
        if (tabakJobTransportStartMatcher.find()) {
            this.isTabakJobTransportActive = true;
            return true;
        }

        Matcher pizzaJobTransportStartMatcher = PIZZA_JOB_TRANSPORT_START_PATTERN.matcher(message);
        if (pizzaJobTransportStartMatcher.find()) {
            this.isPizzaJobTransportActive = true;
            return true;
        }
        Matcher pizzaJobTransportGetPizzaMatcher = PIZZA_JOB_TRANSPORT_GET_PIZZA_PATTERN.matcher(message);
        if (pizzaJobTransportGetPizzaMatcher.find()) {
            delayedAction(() -> networkHandler.sendChatCommand("getpizza"), SECONDS.toMillis(3));
            return true;
        }

        return true;
    }

    @Override
    public void onNaviSpotReached() {
        if (this.isTabakJobTransportActive && player.getBlockPos().isWithinDistance(new BlockPos(-133, 69, -78), 3)) {
            networkHandler.sendChatCommand("droptabak");
            this.isTabakJobTransportActive = false;
        }

        if (this.isPizzaJobTransportActive && player.getBlockPos().isWithinDistance(new BlockPos(266, 69, 54), 2)) {
            networkHandler.sendChatCommand("getpizza");
            this.isPizzaJobTransportActive = false;
        }
    }
}
