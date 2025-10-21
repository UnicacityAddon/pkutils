package de.rettichlp.pkutils.listener;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;

public interface IEntityRenderListener extends IPKUtilsListener {

    void onEntityRender(WorldRenderContext context);
}
