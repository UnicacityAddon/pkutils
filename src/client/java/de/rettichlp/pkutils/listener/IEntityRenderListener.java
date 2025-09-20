package de.rettichlp.pkutils.listener;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;

public interface IEntityRenderListener {

    void onEntityRender(WorldRenderContext context);
}
