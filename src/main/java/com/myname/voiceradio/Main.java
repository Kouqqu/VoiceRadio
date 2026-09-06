package com.myname.voiceradio;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import su.plo.voice.api.server.PlasmoVoiceServer;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class Main extends JavaPlugin implements Listener {

    private static Main instance;
    public final ConcurrentHashMap<UUID, RadioSession> activeSessions = new ConcurrentHashMap<>();
    public final ConcurrentHashMap<org.bukkit.Location, Radio3DSession> active3DSessions = new ConcurrentHashMap<>();
    public final ConcurrentHashMap<org.bukkit.Location, Runnable> active3DStopActions = new ConcurrentHashMap<>();
    private final RadioAddon radioAddon = new RadioAddon();
    private RadioDiscManager discManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        this.discManager = new RadioDiscManager(this);

        if (getServer().getPluginManager().isPluginEnabled("PlasmoVoice")) {
            try {
                PlasmoVoiceServer.getAddonsLoader().load(radioAddon);
                getLogger().info("Plasmo Voice Addon (VoiceRadio) успешно загружен!");
            } catch (Exception e) {
                getLogger().severe("Ошибка при загрузке аддона VoiceRadio в Plasmo: " + e.getMessage());
            }
        } else {
            getLogger().info("Plasmo Voice не обнаружен. Пропускаем инициализацию Plasmo.");
        }

        if (getServer().getPluginManager().isPluginEnabled("Voicechat")) {
            SimpleVoiceRadioHandler.register();
        } else {
            getLogger().info("Simple Voice Chat не обнаружен. Пропускаем инициализацию SimpleVoice.");
        }

        RadioCommand commandExecutor = new RadioCommand();
        if (getCommand("radio") != null) {
            getCommand("radio").setExecutor(commandExecutor);
            getCommand("radio").setTabCompleter(commandExecutor);
        }
        if (getCommand("radioreload") != null) {
            getCommand("radioreload").setExecutor(commandExecutor);
        }

        getServer().getPluginManager().registerEvents(this, this);
        getServer().getPluginManager().registerEvents(new JukeboxListener(this), this);
        getLogger().info("Плагин VoiceRadio успешно запущен!");
    }

    @Override
    public void onDisable() {
        // Завершаем все активные радиосессии при выключении плагина
        activeSessions.forEach((uuid, session) -> session.stop());
        activeSessions.clear();

        active3DSessions.forEach((loc, session) -> session.stop());
        active3DSessions.clear();

        active3DStopActions.forEach((loc, action) -> {
            try { action.run(); } catch (Exception ignored) {}
        });
        active3DStopActions.clear();
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        stopRadio(event.getPlayer());
    }

    public void stopRadio(Player player) {
        RadioSession session = activeSessions.remove(player.getUniqueId());
        if (session != null) {
            session.stop();
        }
    }

    public Radio3DSession stop3DRadio(org.bukkit.Location location) {
        Runnable stopAction = active3DStopActions.remove(location);
        if (stopAction != null) {
            try { stopAction.run(); } catch (Exception ignored) {}
        }
        Radio3DSession session = active3DSessions.remove(location);
        if (session != null) {
            session.stop();
        }
        return session;
    }

    public static Main getInstance() {
        return instance;
    }

    public RadioAddon getRadioAddon() {
        return radioAddon;
    }

    public RadioDiscManager getDiscManager() {
        return discManager;
    }
}
