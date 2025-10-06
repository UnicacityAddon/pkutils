package de.rettichlp.pkutils.listener.impl.job;

import de.rettichlp.pkutils.common.models.Job;
import de.rettichlp.pkutils.common.registry.PKUtilsBase;
import de.rettichlp.pkutils.common.registry.PKUtilsListener;
import de.rettichlp.pkutils.listener.IMessageReceiveListener;
import de.rettichlp.pkutils.listener.ITickListener;
import net.minecraft.text.Text;

import java.awt.Color;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static de.rettichlp.pkutils.PKUtilsClient.configService;
import static de.rettichlp.pkutils.PKUtilsClient.notificationService;
import static java.time.Duration.between;
import static java.time.LocalDateTime.now;
import static java.util.Arrays.stream;
import static net.minecraft.text.Text.empty;
import static net.minecraft.text.Text.of;
import static net.minecraft.util.Formatting.DARK_GRAY;
import static net.minecraft.util.Formatting.GRAY;

@PKUtilsListener
public class JobListener extends PKUtilsBase implements IMessageReceiveListener, ITickListener {

    @Override
    public boolean onMessageReceive(Text text, String message) {
        // refresh job cooldowns
        Optional<Job> optionalJob = stream(Job.values())
                .filter(job -> job.getJobStartPattern().matcher(message).find())
                .findFirst();

        if (optionalJob.isPresent()) {
            Job job = optionalJob.get();
            Duration cooldown = job.getCooldown();
            LocalDateTime jobCooldownEntTime = now().plus(cooldown);

            configService.edit(mainConfig -> mainConfig.getJobCooldownEndTimes().put(job, jobCooldownEntTime));

            notificationService.sendNotification(() -> {
                long remainingMillis = between(now(), jobCooldownEntTime).toMillis();
                return empty()
                        .append(of(job.getDisplayName()).copy().formatted(GRAY))
                        .append(of(":").copy().formatted(DARK_GRAY)).append(" ")
                        .append(of(millisToFriendlyString(remainingMillis)));
            }, Color.GRAY, cooldown.toMillis());

            return true;
        }

        return true;
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
