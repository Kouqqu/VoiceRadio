package com.myname.voiceradio;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import su.plo.voice.api.addon.AddonInitializer;
import su.plo.voice.api.addon.AddonLoaderScope;
import su.plo.voice.api.addon.InjectPlasmoVoice;
import su.plo.voice.api.addon.annotation.Addon;
import su.plo.voice.api.encryption.Encryption;
import su.plo.voice.api.server.PlasmoVoiceServer;
import su.plo.voice.api.server.audio.line.ServerSourceLine;
import su.plo.voice.api.server.audio.provider.AudioFrameProvider;
import su.plo.voice.api.server.audio.provider.AudioFrameResult;
import su.plo.voice.api.server.audio.source.AudioSender;
import su.plo.voice.api.server.audio.source.ServerDirectSource;
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

import java.util.UUID;

@Addon(id = "voiceradio", scope = AddonLoaderScope.SERVER, version = "1.0", authors = {"MyName"})
public class RadioAddon implements AddonInitializer {

    private static final byte[] OPUS_SILENCE = new byte[] { (byte) 0xFC, (byte) 0xFF, (byte) 0xFE };

    @InjectPlasmoVoice
    private PlasmoVoiceServer voiceServer;

    private AudioPlayerManager playerManager;
    private ServerSourceLine sourceLine;

    @Override
    public void onAddonInitialize() {
        voiceServer.getEventBus().register(this, this);

        playerManager = new DefaultAudioPlayerManager();
        playerManager.getConfiguration().setOutputFormat(StandardAudioDataFormats.DISCORD_OPUS);

        // Включаем поддержку удаленных интернет-потоков (HTTP, HTTPS, Icecast, Shoutcast и т.д.)
        AudioSourceManagers.registerRemoteSources(playerManager);
        AudioSourceManagers.registerLocalSource(playerManager);

        // Настраиваем таймаут подключения (10 сек), чтобы медленные радиосерверы не вызывали SocketTimeoutException
        su.plo.voice.lavaplayer.libs.com.sedmelluq.discord.lavaplayer.source.http.HttpAudioSourceManager httpSource =
                playerManager.source(su.plo.voice.lavaplayer.libs.com.sedmelluq.discord.lavaplayer.source.http.HttpAudioSourceManager.class);
        if (httpSource != null) {
            httpSource.configureRequests(config ->
                    su.plo.voice.lavaplayer.libs.org.apache.http.client.config.RequestConfig.copy(config)
                            .setConnectTimeout(10000)
                            .setSocketTimeout(10000)
                            .setConnectionRequestTimeout(10000)
                            .build()
            );
        }

        sourceLine = voiceServer.getSourceLineManager().getLineByName("proximity")
                .orElseThrow(() -> new IllegalStateException("Линия proximity не найдена"));
    }

    public void playRadio(Player player, String streamUrl, String stationDisplayName) {
        UUID playerId = player.getUniqueId();

        // Останавливаем предыдущую трансляцию, если она была
        Main.getInstance().stopRadio(player);

        if (org.bukkit.Bukkit.getPluginManager().isPluginEnabled("Voicechat") && SimpleVoiceRadioHandler.isConnected(player)) {
            SimpleVoiceRadioHandler.getInstance().playRadio(player, streamUrl, stationDisplayName);
            return;
        }

        if (voiceServer == null || !voiceServer.getPlayerManager().getPlayerById(playerId).isPresent()) {
            player.sendMessage("§cОшибка: Голосовой чат (Plasmo Voice / Simple Voice Chat) не подключен или еще не инициализировался.");
            return;
        }

        voiceServer.getPlayerManager().getPlayerById(playerId).ifPresent(voicePlayer -> {
            player.sendMessage("§e📻 Подключение к радиопотоку... Пожалуйста, подождите.");

            ServerDirectSource source = sourceLine.createDirectSource(voicePlayer, true);
            AudioPlayer audioPlayer = playerManager.createPlayer();

            int defaultVolume = Main.getInstance().getConfig().getInt("default-volume", 20);
            audioPlayer.setVolume(defaultVolume);

            playerManager.loadItem(streamUrl, new AudioLoadResultHandler() {
                @Override
                public void trackLoaded(AudioTrack track) {
                    startPlayback(player, track, audioPlayer, source, streamUrl, stationDisplayName);
                }

                @Override
                public void playlistLoaded(AudioPlaylist playlist) {
                    if (!playlist.getTracks().isEmpty()) {
                        startPlayback(player, playlist.getTracks().get(0), audioPlayer, source, streamUrl, stationDisplayName);
                    } else {
                        player.sendMessage("§cНе удалось загрузить плейлист по данной ссылке.");
                        source.remove();
                    }
                }

                @Override
                public void noMatches() {
                    player.sendMessage("§cНе удалось найти аудиопоток по указанной ссылке.");
                    source.remove();
                }

                @Override
                public void loadFailed(FriendlyException exception) {
                    player.sendMessage("§cОшибка при подключении к радиопотоку: " + exception.getMessage());
                    source.remove();
                }
            });
        });
    }

    private void startPlayback(Player player, AudioTrack track, AudioPlayer audioPlayer, ServerDirectSource source, String streamUrl, String stationDisplayName) {
        UUID playerId = player.getUniqueId();

        audioPlayer.playTrack(track);

        Encryption encryption = voiceServer.getDefaultEncryption();
        LavaplayerProvider provider = new LavaplayerProvider(audioPlayer, encryption, OPUS_SILENCE);

        AudioSender audioSender = source.createAudioSender(provider);
        audioSender.start();
        audioSender.onStop(() -> {
            source.remove();
            Main.getInstance().activeSessions.remove(playerId);
        });

        RadioSession session = new RadioSession(audioPlayer, source, audioSender, streamUrl, stationDisplayName);
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

    public void play3DRadio(org.bukkit.Location location, String streamUrl, String stationDisplayName, int distance, org.bukkit.inventory.ItemStack discItem) {
        Main.getInstance().stop3DRadio(location);

        if (org.bukkit.Bukkit.getPluginManager().isPluginEnabled("Voicechat") && SimpleVoiceRadioHandler.isAvailable()) {
            SimpleVoiceRadioHandler.getInstance().play3DRadio(location, streamUrl, stationDisplayName, distance);
        }

        if (voiceServer == null) {
            return;
        }

        AudioPlayer audioPlayer = playerManager.createPlayer();
        int volume3D = Main.getInstance().getConfig().getInt("3d-radio.volume", 100);
        audioPlayer.setVolume(volume3D);

        su.plo.slib.api.server.world.McServerWorld mcWorld = voiceServer.getMinecraftServer().getWorld(location.getWorld());
        su.plo.slib.api.server.position.ServerPos3d pos = new su.plo.slib.api.server.position.ServerPos3d(
                mcWorld,
                location.getX() + 0.5,
                location.getY() + 0.5,
                location.getZ() + 0.5
        );

        su.plo.voice.api.server.audio.source.ServerStaticSource source = sourceLine.createStaticSource(pos, false);

        playerManager.loadItem(streamUrl, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                audioPlayer.playTrack(track);

                Encryption encryption = voiceServer.getDefaultEncryption();
                LavaplayerProvider provider = new LavaplayerProvider(audioPlayer, encryption, OPUS_SILENCE);

                AudioSender audioSender = source.createAudioSender(provider, (short) distance);
                audioSender.start();
                audioSender.onStop(() -> {
                    source.remove();
                    Main.getInstance().active3DSessions.remove(location);
                });

                Radio3DSession session = new Radio3DSession(location, audioPlayer, source, audioSender, streamUrl, stationDisplayName, distance, discItem);
                Main.getInstance().active3DSessions.put(location, session);
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                if (!playlist.getTracks().isEmpty()) {
                    trackLoaded(playlist.getTracks().get(0));
                } else {
                    source.remove();
                }
            }

            @Override
            public void noMatches() {
                source.remove();
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                source.remove();
            }
        });
    }

    public static class LavaplayerProvider implements AudioFrameProvider {
        private final AudioPlayer player;
        private final Encryption encryption;
        private final byte[] opusSilence;

        public LavaplayerProvider(AudioPlayer player, Encryption encryption, byte[] opusSilence) {
            this.player = player;
            this.encryption = encryption;
            this.opusSilence = opusSilence;
        }

        @Override
        public AudioFrameResult provide20ms() {
            AudioFrame frame = player.provide();
            if (frame == null) {
                if (player.getPlayingTrack() == null) return AudioFrameResult.Finished.INSTANCE;
                try {
                    return new AudioFrameResult.Provided(encryption.encrypt(opusSilence));
                } catch (Exception e) { return AudioFrameResult.Finished.INSTANCE; }
            }
            try {
                return new AudioFrameResult.Provided(encryption.encrypt(frame.getData()));
            } catch (Exception e) { return AudioFrameResult.Finished.INSTANCE; }
        }
    }
}
