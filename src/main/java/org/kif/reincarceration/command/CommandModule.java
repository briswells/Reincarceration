package org.kif.reincarceration.command;

import org.bukkit.command.PluginCommand;
import org.kif.reincarceration.Reincarceration;
import org.kif.reincarceration.config.ConfigManager;
import org.kif.reincarceration.core.Module;
import org.kif.reincarceration.cycle.CycleModule;
import org.kif.reincarceration.data.DataModule;
import org.kif.reincarceration.economy.EconomyModule;
import org.kif.reincarceration.modifier.core.ModifierModule;
import org.kif.reincarceration.rank.RankModule;
import org.kif.reincarceration.gui.GUIModule;
import org.kif.reincarceration.util.ConsoleUtil;

import java.sql.SQLException;

public class CommandModule implements Module {
    private final Reincarceration plugin;
    private final ConfigManager configManager;

    public CommandModule(Reincarceration plugin) {
        this.plugin = plugin;
        this.configManager = plugin.getModuleManager().getConfigManager();
    }

    @Override
    public void onEnable() {
        registerCommands();
        ConsoleUtil.sendSuccess("Command Module enabled");
    }

    @Override
    public void onDisable() {
        ConsoleUtil.sendSuccess("Command Module disabled");
    }

    private void registerCommands() {
        CycleModule cycleModule = plugin.getModuleManager().getModule(CycleModule.class);
        DataModule dataModule = plugin.getModuleManager().getModule(DataModule.class);
        EconomyModule economyModule = plugin.getModuleManager().getModule(EconomyModule.class);
        RankModule rankModule = plugin.getModuleManager().getModule(RankModule.class);
        ModifierModule modifierModule = plugin.getModuleManager().getModule(ModifierModule.class);
        GUIModule guiModule = plugin.getModuleManager().getModule(GUIModule.class);

        registerCommand("rc", new GUICommand(this, configManager, cycleModule, dataModule, economyModule, guiModule));
        registerCommand("flagitem", new FlagItemCommand(plugin));
        registerCommand("inspectitem", new InspectItemCommand(plugin));
        registerCommand("inspectinventory", new InspectInventoryCommand(plugin));
        registerCommand("viewplayerdata", new ViewPlayerDataCommand(plugin));
        registerCommand("completeCycle", new CompleteCycleCommand(cycleModule));
        registerCommand("rcreloadtags", new ReloadTagsCommand(plugin));
        registerCommand("forcequit", new ForceQuitCommand(cycleModule));
    }

    private void registerCommand(String name, org.bukkit.command.CommandExecutor executor) {
        PluginCommand command = plugin.getCommand(name);
        if (command != null) {
            command.setExecutor(executor);
        } else {
            ConsoleUtil.sendError("Failed to register command: " + name);
        }
    }

    public Reincarceration getPlugin() {
        return plugin;
    }
}