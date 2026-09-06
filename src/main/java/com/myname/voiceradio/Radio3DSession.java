package com.myname.voiceradio;

import org.bukkit.Location;
import su.plo.voice.api.server.audio.source.AudioSender;
import su.plo.voice.api.server.audio.source.ServerStaticSource;
import su.plo.voice.lavaplayer.libs.com.sedmelluq.discord.lavaplayer.player.AudioPlayer;

public class Radio3DSession {
    private final Location location;
    private final AudioPlayer player;
    private final ServerStaticSource source;
    private final AudioSender sender;
    private final String streamUrl;
    private final String stationName;
    private final int distance;
    private final org.bukkit.inventory.ItemStack discItem;

    public Radio3DSession(Location location, AudioPlayer player, ServerStaticSource source, AudioSender sender, String streamUrl, String stationName, int distance, org.bukkit.inventory.ItemStack discItem) {
        this.location = location;
        this.player = player;
        this.source = source;
        this.sender = sender;
        this.streamUrl = streamUrl;
        this.stationName = stationName;
        this.distance = distance;
        this.discItem = discItem;
    }

    public Location getLocation() {
        return location;
    }

    public AudioPlayer getPlayer() {
        return player;
    }

    public String getStreamUrl() {
        return streamUrl;
    }

    public String getStationName() {
        return stationName;
    }

    public int getDistance() {
        return distance;
    }

    public org.bukkit.inventory.ItemStack getDiscItem() {
        return discItem != null ? discItem.clone() : null;
    }

    public void stop() {
        if (sender != null) {
            try {
                sender.stop();
            } catch (Exception ignored) {}
        }
        if (source != null) {
            try {
                source.remove();
            } catch (Exception ignored) {}
        }
        if (player != null) {
            try {
                player.stopTrack();
            } catch (Exception ignored) {}
        }
    }
}
