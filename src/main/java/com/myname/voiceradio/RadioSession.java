package com.myname.voiceradio;

import su.plo.voice.api.server.audio.source.AudioSender;
import su.plo.voice.api.server.audio.source.ServerDirectSource;
import su.plo.voice.lavaplayer.libs.com.sedmelluq.discord.lavaplayer.player.AudioPlayer;

public class RadioSession {
    private final AudioPlayer player;
    private final ServerDirectSource source;
    private final AudioSender sender;
    private final String streamUrl;
    private final String stationName;
    private final Runnable stopAction;

    public RadioSession(AudioPlayer player, ServerDirectSource source, AudioSender sender, String streamUrl, String stationName, Runnable stopAction) {
        this.player = player;
        this.source = source;
        this.sender = sender;
        this.streamUrl = streamUrl;
        this.stationName = stationName;
        this.stopAction = stopAction;
    }

    public RadioSession(AudioPlayer player, ServerDirectSource source, AudioSender sender, String streamUrl, String stationName) {
        this(player, source, sender, streamUrl, stationName, null);
    }

    public RadioSession(AudioPlayer player, Runnable stopAction, String streamUrl, String stationName) {
        this(player, null, null, streamUrl, stationName, stopAction);
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

    public int getVolume() {
        return player != null ? player.getVolume() : 0;
    }

    public void setVolume(int volume) {
        if (player != null) {
            player.setVolume(volume);
        }
    }

    public void stop() {
        if (stopAction != null) {
            try {
                stopAction.run();
            } catch (Exception ignored) {}
        }
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
