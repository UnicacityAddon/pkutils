package de.rettichlp.pkutils.listener.impl.job;

import de.rettichlp.pkutils.common.models.Job;
import de.rettichlp.pkutils.common.registry.PKUtilsBase;
import de.rettichlp.pkutils.common.registry.PKUtilsListener;
import de.rettichlp.pkutils.listener.ICommandSendListener;
import de.rettichlp.pkutils.listener.IMessageReceiveListener;
import de.rettichlp.pkutils.listener.IMoveListener;
import de.rettichlp.pkutils.listener.INaviSpotReachedListener;
import de.rettichlp.pkutils.listener.ITickListener;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.Color;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static de.rettichlp.pkutils.PKUtilsClient.configService;
import static de.rettichlp.pkutils.PKUtilsClient.notificationService;
import static de.rettichlp.pkutils.PKUtilsClient.player;
import static de.rettichlp.pkutils.common.models.Job.LUMBERJACK;
import static de.rettichlp.pkutils.common.models.Job.PIZZA_DELIVERY;
import static de.rettichlp.pkutils.common.models.Job.TOBACCO_PLANTATION;
import static de.rettichlp.pkutils.common.models.Job.URANIUM_TRANSPORT;
import static java.time.Duration.between;
import static java.time.LocalDateTime.now;
import static java.util.Arrays.stream;
import static java.util.Objects.isNull;
import static java.util.concurrent.TimeUnit.SECONDS;
import static java.util.regex.Pattern.compile;
import static net.minecraft.text.Text.empty;
import static net.minecraft.text.Text.of;
import static net.minecraft.util.Formatting.DARK_GRAY;
import static net.minecraft.util.Formatting.GRAY;

@PKUtilsListener
public class JobListener extends PKUtilsBase
        implements ICommandSendListener, IMessageReceiveListener, IMoveListener, INaviSpotReachedListener, ITickListener {

    private static final Pattern TRANSPORT_DELIVER_PATTERN = compile("^\\[Transport] Du hast (eine Kiste|eine Waffenkiste|ein Weizen Paket|eine Schwarzpulverkiste) abgeliefert\\.$");
    private static final Pattern DRINK_TRANSPORT_DELIVER_PATTERN = compile("^\\[Bar] Du hast eine Flasche abgegeben!$");
    private static final Pattern PIZZA_JOB_TRANSPORT_GET_PIZZA_PATTERN = compile("^\\[Pizzalieferant] Sobald du 10 Pizzen dabei hast, wird dir deine erste Route angezeigt\\.$");

    @Nullable
    private Job currentJob;

    @Override
    public boolean onCommandSend(@NotNull String command) {
        if (command.equals("sägewerk")) {
            delayedAction(() -> sendCommand("findtree"), 1000);
        }

        return true;
    }

    @Override
    public boolean onMessageReceive(Text text, String message) {
        Matcher transportDeliverMatcher = TRANSPORT_DELIVER_PATTERN.matcher(message);
        if (transportDeliverMatcher.find()) {
            delayedAction(() -> sendCommand("droptransport"), SECONDS.toMillis(10));
            return true;
        }

        Matcher drinkTransportDeliverMatcher = DRINK_TRANSPORT_DELIVER_PATTERN.matcher(message);
        if (drinkTransportDeliverMatcher.find()) {
            delayedAction(() -> sendCommand("dropdrink"), 2500);
            return true;
        }

        Matcher pizzaJobTransportGetPizzaMatcher = PIZZA_JOB_TRANSPORT_GET_PIZZA_PATTERN.matcher(message);
        if (pizzaJobTransportGetPizzaMatcher.find()) {
            delayedAction(() -> sendCommand("getpizza"), 2500);
            return true;
        }

        // refresh job cooldowns
        Optional<Job> optionalJob = stream(Job.values())
                .filter(job -> job.getJobStartPattern().matcher(message).find())
                .findFirst();

        if (optionalJob.isPresent()) {
            Job job = optionalJob.get();
            this.currentJob = job;

            Duration cooldown = job.getCooldown();
            LocalDateTime jobCooldownEntTime = now().plus(cooldown);

            configService.edit(mainConfig -> mainConfig.getJobCooldownEndTimes().put(job, jobCooldownEntTime));

            notificationService.sendNotification(() -> {
                long remainingMillis = between(now(), jobCooldownEntTime).toMillis();
                return empty()
                        .append(of(job.getDisplayName()).copy().formatted(GRAY))
                        .append(of(":").copy().formatted(DARK_GRAY)).append(" ")
                        .append(of(millisToFriendlyString(remainingMillis)));
            }, Color.WHITE, cooldown.toMillis());

            if (job == LUMBERJACK) {
                delayedAction(() -> sendCommand("findtree"), 1000);
            }

            return true;
        }

        return true;
    }

    @Override
    public void onMove(BlockPos blockPos) {
        if (isNull(this.currentJob)) {
            return;
        }

        if (this.currentJob == URANIUM_TRANSPORT && player.getBlockPos().isWithinDistance(new BlockPos(1132, 68, 396), 1.8)) {
            sendCommand("dropuran");
        }
    }

    @Override
    public void onNaviSpotReached() {
        if (isNull(this.currentJob)) {
            return;
        }

        if (this.currentJob == PIZZA_DELIVERY && player.getBlockPos().isWithinDistance(new BlockPos(266, 69, 54), 2)) {
            sendCommand("getpizza");
            return;
        }

        if (this.currentJob == TOBACCO_PLANTATION && player.getBlockPos().isWithinDistance(new BlockPos(-133, 69, -78), 3)) {
            sendCommand("droptabak");
        }
    }

    @Override
    public void onTick() {
        List<Job> expiredJobCooldowns = configService.load().getJobCooldownEndTimes().entrySet().stream()
                .filter(entry -> entry.getValue().isBefore(now()))
                .map(Map.Entry::getKey)
                .toList();

        expiredJobCooldowns.forEach(job -> {
            configService.edit(mainConfig -> mainConfig.getJobCooldownEndTimes().remove(job));
            notificationService.sendSuccessNotification("Cooldown für '" + job.getDisplayName() + "' abgelaufen");
        });
    }
}
