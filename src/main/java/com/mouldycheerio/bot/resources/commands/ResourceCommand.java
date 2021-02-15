package com.mouldycheerio.bot.resources.commands;

import com.mouldycheerio.bot.resources.ResourceManager;
import com.mouldycheerio.dbot.commands.DetailedCommand;

public abstract class ResourceCommand extends DetailedCommand {
    private ResourceManager resourceManager;

    public ResourceCommand(ResourceManager resourceManager) {
        this.resourceManager = resourceManager;
    }

    public ResourceManager getResourceManager() {
        return resourceManager;
    }
}
