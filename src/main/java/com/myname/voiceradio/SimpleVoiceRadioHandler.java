package com.myname.voiceradio;

import de.maxhenkel.voicechat.api.VoicechatApi;
import de.maxhenkel.voicechat.api.VoicechatPlugin;
import de.maxhenkel.voicechat.api.VoicechatServerApi;
import de.maxhenkel.voicechat.api.VoicechatConnection;
import de.maxhenkel.voicechat.api.ServerLevel;
import de.maxhenkel.voicechat.api.Position;
import de.maxhenkel.voicechat.api.audiochannel.StaticAudioChannel;
import de.maxhenkel.voicechat.api.audiochannel.LocationalAudioChannel;
import de.maxhenkel.voicechat.api.opus.OpusEncoder;
import de.maxhenkel.voicechat.api.BukkitVoicechatService;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import su.plo.voice.lavaplayer.libs.com.sedmelluq.discord.lavaplayer.format.StandardAudioDataFormats;
import su.plo.voice.lavaplayer.libs.com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import su.plo.voice.lavaplayer.libs.com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import su.plo.voice.lavaplayer.libs.com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import su.plo.voice.lavaplayer.libs.com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import su.plo.voice.lavaplayer.libs.com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import su.plo.voice.lavaplayer.libs.com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import su.plo.voice.lavaplayer.libs.com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import su.plo.voice.lavaplayer.libs.com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import su.plo.voice.lavaplayer.libs.com.sedmelluq.discord.lavaplayer.track.playback.AudioFrame;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

public class SimpleVoiceRadioHandler implements VoicechatPlugin {

    private static VoicechatServerApi voicechatApi;
    private static SimpleVoiceRadioHandler instance;
    private final AudioPlayerManager playerManager;

    public SimpleVoiceRadioHandler() {
        instance = this;
        playerManager = new DefaultAudioPlayerManager();
        playerManager.getConfiguration().setOutputFormat(StandardAudioDataFormats.DISCORD_PCM_S16_BE);
        AudioSourceManagers.registerRemoteSources(playerManager);
        AudioSourceManagers.registerLocalSource(playerManager);
    }

    public static void register() {
        try {
            BukkitVoicechatService service = Bukkit.getServicesManager().load(BukkitVoicechatService.class);
            if (service != null) {
                SimpleVoiceRadioHandler handler = new SimpleVoiceRadioHandler();
                service.registerPlugin(handler);
                Main.getInstance().getLogger().info("Simple Voice Chat успешно подключен к VoiceRadio!");
            }
        } catch (Throwable t) {
            Main.getInstance().getLogger().warning("Ошибка подключения Simple Voice Chat в VoiceRadio: " + t.getMessage());
        }
    }

    @Override
    public String getPluginId() {
        return "voiceradio";
    }

    @Override
    public void initialize(VoicechatApi api) {
        if (api instanceof VoicechatServerApi serverApi) {
            voicechatApi = serverApi;
        }
    }

    public static boolean isConnected(Player player) {
        if (voicechatApi == null) return false;
        VoicechatConnection conn = voicechatApi.getConnectionOf(player.getUniqueId());
        return conn != null && conn.isConnected();
    }

    public static boolean isAvailable() {
        return voicechatApi != null;
    }

    public void playRadio(Player player, String streamUrl, String stationDisplayName) {
        if (voicechatApi == null) return;
        UUID playerId = player.getUniqueId();
        Main.getInstance().stopRadio(player);

        VoicechatConnection connection = voicechatApi.getConnectionOf(playerId);
        if (connection == null || !connection.isConnected()) {
            player.sendMessage("§cОшибка: Simple Voice Chat не подключен у вас в игре.");
            return;
        }

        player.sendMessage("§e📻 Подключение к радиопотоку... Пожалуйста, подождите.");

        AudioPlayer audioPlayer = playerManager.createPlayer();
        int defaultVolume = Main.getInstance().getConfig().getInt("default-volume", 20);
        audioPlayer.setVolume(defaultVolume);

        ServerLevel level = voicechatApi.fromServerLevel(player.getWorld());
        StaticAudioChannel channel = voicechatApi.createStaticAudioChannel(UUID.randomUUID(), level, connection);
        // Фильтр для персонального 2D радио
        channel.setFilter(serverPlayer -> serverPlayer.getUuid().equals(playerId));

        playerManager.loadItem(streamUrl, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                startPlayback(player, track, audioPlayer, channel, streamUrl, stationDisplayName, connection);
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                if (!playlist.getTracks().isEmpty()) {
                    startPlayback(player, playlist.getTracks().get(0), audioPlayer, channel, streamUrl, stationDisplayName, connection);
                } else {
                    player.sendMessage("§cНе удалось загрузить плейлист по данной ссылке.");
                }
            }

            @Override
            public void noMatches() {
                player.sendMessage("§cНе удалось найти аудиопоток по указанной ссылке.");
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                player.sendMessage("§cОшибка при подключении к радиопотоку: " + exception.getMessage());
            }
        });
    }

    private void startPlayback(Player player, AudioTrack track, AudioPlayer audioPlayer, StaticAudioChannel channel, String streamUrl, String stationDisplayName, VoicechatConnection connection) {
        UUID playerId = player.getUniqueId();
        audioPlayer.playTrack(track);

        AtomicBoolean stopped = new AtomicBoolean(false);
        OpusEncoder encoder = voicechatApi.createEncoder();

        de.maxhenkel.voicechat.api.audiochannel.AudioPlayer svAudioPlayer = voicechatApi.createAudioPlayer(channel, encoder, () -> {
            if (stopped.get() || !player.isOnline() || !connection.isConnected()) {
                return null;
            }

            AudioFrame frame = audioPlayer.provide();
            if (frame != null) {
                byte[] pcmData = frame.getData();
                short[] pcmShorts = new short[pcmData.length / 2];
                ByteBuffer.wrap(pcmData).order(ByteOrder.BIG_ENDIAN).asShortBuffer().get(pcmShorts);

                short[] monoShorts = new short[960];
                int samples = pcmShorts.length / 2;
                for (int i = 0; i < Math.min(960, samples); i++) {
                    monoShorts[i] = (short) ((pcmShorts[i * 2] + pcmShorts[i * 2 + 1]) / 2);
                }
                return monoShorts;
            } else if (audioPlayer.getPlayingTrack() == null) {
                stopped.set(true);
                Main.getInstance().activeSessions.remove(playerId);
                return null;
            } else {
                return new short[960];
            }
        });

        Runnable stopAction = () -> {
            stopped.set(true);
            try {
                svAudioPlayer.stopPlaying();
            } catch (Exception ignored) {}
        };

        svAudioPlayer.startPlaying();
        Main.getInstance().getLogger().info("[SimpleVoice] Воспроизведение радио (" + stationDisplayName + ") начато для " + player.getName());

        RadioSession session = new RadioSession(audioPlayer, stopAction, streamUrl, stationDisplayName);
        Main.getInstance().activeSessions.put(playerId, session);

        Component message = Component.text("📻 Сейчас играет: ")
                .color(NamedTextColor.GREEN)
                .append(Component.text(stationDisplayName).color(NamedTextColor.WHITE).decorate(TextDecoration.BOLD))
                .append(Component.text("  "))
                .append(Component.text("[ОСТАНОВИТЬ]")
                        .color(NamedTextColor.RED)
                        .decorate(TextDecoration.BOLD)
                        .clickEvent(ClickEvent.runCommand("/radio stop")));

        player.sendMessage(message);
    }

    public void play3DRadio(Location location, String streamUrl, String stationDisplayName, int distance) {
        if (voicechatApi == null) return;

        AudioPlayer audioPlayer = playerManager.createPlayer();
        int volume3D = Main.getInstance().getConfig().getInt("3d-radio.volume", 100);
        audioPlayer.setVolume(volume3D);

        ServerLevel level = voicechatApi.fromServerLevel(location.getWorld());
        Position pos = voicechatApi.createPosition(location.getX() + 0.5, location.getY() + 0.5, location.getZ() + 0.5);

        LocationalAudioChannel channel = voicechatApi.createLocationalAudioChannel(UUID.randomUUID(), level, pos);
        channel.setDistance(distance);

        playerManager.loadItem(streamUrl, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                audioPlayer.playTrack(track);
                AtomicBoolean stopped = new AtomicBoolean(false);
                OpusEncoder encoder = voicechatApi.createEncoder();

                de.maxhenkel.voicechat.api.audiochannel.AudioPlayer svAudioPlayer = voicechatApi.createAudioPlayer(channel, encoder, () -> {
                    if (stopped.get()) return null;

                    AudioFrame frame = audioPlayer.provide();
                    if (frame != null) {
                        byte[] pcmData = frame.getData();
                        short[] pcmShorts = new short[pcmData.length / 2];
                        ByteBuffer.wrap(pcmData).order(ByteOrder.BIG_ENDIAN).asShortBuffer().get(pcmShorts);

                        short[] monoShorts = new short[960];
                        int samples = pcmShorts.length / 2;
                        for (int i = 0; i < Math.min(960, samples); i++) {
                            monoShorts[i] = (short) ((pcmShorts[i * 2] + pcmShorts[i * 2 + 1]) / 2);
                        }
                        return monoShorts;
                    } else if (audioPlayer.getPlayingTrack() == null) {
                        stopped.set(true);
                        return null;
                    } else {
                        return new short[960];
                    }
                });

                Runnable stopAction = () -> {
                    stopped.set(true);
                    try {
                        svAudioPlayer.stopPlaying();
                    } catch (Exception ignored) {}
                };

                svAudioPlayer.startPlaying();
                Main.getInstance().getLogger().info("[SimpleVoice] 3D-радио (" + stationDisplayName + ") запущено в точке " + location);
                Main.getInstance().active3DStopActions.put(location, stopAction);
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                if (!playlist.getTracks().isEmpty()) {
                    trackLoaded(playlist.getTracks().get(0));
                }
            }

            @Override
            public void noMatches() {}

            @Override
            public void loadFailed(FriendlyException exception) {}
        });
    }

    public static SimpleVoiceRadioHandler getInstance() {
        return instance;
    }
}
