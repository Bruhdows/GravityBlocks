package com.bruhdows.gravityblocks;

import com.bruhdows.gravityblocks.command.GravityBlockCommand;
import com.bruhdows.gravityblocks.listener.GravityStickListener;
import com.bruhdows.gravityblocks.manager.GravityBlockManager;
import com.bruhdows.gravityblocks.manager.GravityStickManager;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

@Getter
public class GravityBlocksPlugin extends JavaPlugin {

    private GravityBlockManager gravityBlockManager;
    private GravityStickManager gravityStickManager;

    @Override
    public void onEnable() {
        gravityBlockManager = new GravityBlockManager();
        gravityStickManager = new GravityStickManager(this);

        Objects.requireNonNull(getCommand("gravityblock")).setExecutor(new GravityBlockCommand(this));

        getServer().getPluginManager().registerEvents(new GravityStickListener(this), this);
        getServer().getScheduler().runTaskTimer(this, () -> gravityBlockManager.tick(), 0L, 1L);
    }

    @Override
    public void onDisable() {
        gravityBlockManager.cleanupAll();
    }

}