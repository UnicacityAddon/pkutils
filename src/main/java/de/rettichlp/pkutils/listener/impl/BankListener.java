package de.rettichlp.pkutils.listener.impl;

import de.rettichlp.pkutils.PKUtils;
import de.rettichlp.pkutils.common.registry.PKUtilsListener;
import de.rettichlp.pkutils.listener.IBlockRightClickListener;
import de.rettichlp.pkutils.listener.IMessageReceiveListener;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import static java.util.regex.Pattern.compile;



@PKUtilsListener
public class BankListener implements IMessageReceiveListener, IBlockRightClickListener {
    private boolean clickedSign = false;

    private static final Pattern BANK_STATEMENT_PATTERN = compile("^Ihr Bankguthaben beträgt: \\+(?<amount>\\d+)\\$$");
    private static final Pattern FBANK_NO_PERMISSION_PATTERN = compile("^Fehler: Du musst mindestens Rang (?<rank>[0-9]) sein.$");
    private static final Pattern GROUP_ERROR_PATTERN = compile("^Fehler: Du bist in keiner Gruppierung.$");
    private static final Pattern DISTANCE_ERROR_PATTERN = compile("^Du befindest dich nicht in der Nähe eines Bankautomaten.$");

    @Override
    public void onBlockRightClick(World world, Hand hand, BlockHitResult hitResult) {
        BlockEntity blockEntity = world.getBlockEntity(hitResult.getBlockPos());
        this.clickedSign = blockEntity instanceof SignBlockEntity;
        PKUtils.LOGGER.info("Sign {}", this.clickedSign);
    }

    @Override
    public boolean onMessageReceive(Text text, String message) {
        if (this.clickedSign){
            Matcher bankStatementMatcher = BANK_STATEMENT_PATTERN.matcher(message);
            if (bankStatementMatcher.find()) {
                List<String> commands = new ArrayList<>();
                commands.add("fbank info");
                commands.add("gruppierungkasse");
                PKUtils.commandService.sendCommands(commands);
                return true;
            }

            Matcher fbankNoPermissionMatcher = FBANK_NO_PERMISSION_PATTERN.matcher(message);
            if (fbankNoPermissionMatcher.find()) {
                return false;
            }

            Matcher groupErrorMatcher = GROUP_ERROR_PATTERN.matcher(message);
            if (groupErrorMatcher.find()) {
                return false;
            }

            Matcher distanceErrorMatcher = DISTANCE_ERROR_PATTERN.matcher(message);
            if (distanceErrorMatcher.find()) {
                return false; //Show message or not?
            }
        }

        return true;
    }


}
