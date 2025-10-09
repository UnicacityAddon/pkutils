package de.rettichlp.pkutils.listener;

import net.minecraft.entity.Entity;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.world.World;

public interface IEntityRightClickListener {

    void onEntityRightClick(World world, Hand hand, Entity entity, EntityHitResult hitResult);
}
