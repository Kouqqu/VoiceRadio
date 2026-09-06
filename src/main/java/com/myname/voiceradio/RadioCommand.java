package com.myname.voiceradio;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class RadioCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (label.equalsIgnoreCase("radioreload")) {
            return handleReload(sender);
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage("§cЭту команду может использовать только игрок.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            sendHelp(player);
            return true;
        }

        String subCommand = args[0].toLowerCase();
        switch (subCommand) {
            case "play":
                handlePlay(player, args);
                break;
            case "stop":
                handleStop(player);
                break;
            case "volume":
                handleVolume(player, args);
                break;
            case "list":
                handleList(player);
                break;
            case "disc":
            case "get":
                handleGetDisc(player);
                break;
            case "setwave":
                handleSetWave(player, args);
                break;
            case "setdist":
                handleSetDist(player, args);
                break;
            case "reload":
                handleReload(sender);
                break;
            default:
                sendHelp(player);
                break;
        }

        return true;
    }

    private void handlePlay(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§cИспользование: /radio play <ссылка_или_пресет>");
            return;
        }

        // Объединяем все аргументы после play в одну строку (для названий с пробелами или URL)
        String target = String.join(" ", Arrays.copyOfRange(args, 1, args.length)).trim();
        if ((target.startsWith("\"") && target.endsWith("\"")) || (target.startsWith("'") && target.endsWith("'"))) {
            target = target.substring(1, target.length() - 1).trim();
        }

        StationInfo station = findStation(target);

        if (station == null || station.url == null || station.url.isEmpty()) {
            player.sendMessage("§cУказанный пресет не найден, или ссылка должна начинаться с http:// или https://");
            return;
        }

        String displayName = station.name;
        if (station.genre != null && !station.genre.isEmpty()) {
            displayName += " [" + station.genre + "]";
        }

        Main.getInstance().getRadioAddon().playRadio(player, station.url, displayName);
    }

    private static class StationInfo {
        final String name;
        final String genre;
        final String url;

        StationInfo(String name, String genre, String url) {
            this.name = name;
            this.genre = genre;
            this.url = url;
        }
    }

    private StationInfo findStation(String target) {
        Main plugin = Main.getInstance();
        ConfigurationSection stationsSection = plugin.getConfig().getConfigurationSection("stations");

        if (stationsSection != null) {
            // 1. Прямое совпадение по ключу (без учета регистра)
            for (String key : stationsSection.getKeys(false)) {
                if (key.equalsIgnoreCase(target)) {
                    return extractStationInfo(stationsSection, key);
                }
            }

            // 2. Проверка поля 'name' внутри секции пресета
            for (String key : stationsSection.getKeys(false)) {
                if (stationsSection.isConfigurationSection(key)) {
                    ConfigurationSection sub = stationsSection.getConfigurationSection(key);
                    if (sub != null && sub.contains("name")) {
                        String name = sub.getString("name");
                        if (name != null && name.equalsIgnoreCase(target)) {
                            return extractStationInfo(stationsSection, key);
                        }
                    }
                }
            }
        }

        // 3. Если передан прямой URL
        if (target.startsWith("http://") || target.startsWith("https://")) {
            return new StationInfo("Онлайн Поток", "", target);
        }

        return null;
    }

    private StationInfo extractStationInfo(ConfigurationSection parent, String key) {
        if (parent.isConfigurationSection(key)) {
            ConfigurationSection sub = parent.getConfigurationSection(key);
            String name = sub.getString("name", key);
            String genre = sub.getString("genre", "");
            String url = sub.getString("url", "");
            return new StationInfo(name, genre, url);
        } else {
            String url = parent.getString(key, "");
            return new StationInfo(key.toUpperCase(), "", url);
        }
    }

    private void handleStop(Player player) {
        Main plugin = Main.getInstance();
        RadioSession session = plugin.activeSessions.get(player.getUniqueId());

        if (session != null) {
            plugin.stopRadio(player);
            player.sendMessage("§c📻 Радио остановлено.");
        } else {
            player.sendMessage("§cСейчас радио у вас не играет.");
        }
    }

    private void handleVolume(Player player, String[] args) {
        if (args.length < 2) {
            RadioSession session = Main.getInstance().activeSessions.get(player.getUniqueId());
            int currentVol = session != null ? session.getVolume() : Main.getInstance().getConfig().getInt("default-volume", 20);
            player.sendMessage("§eТекущая громкость: §f" + currentVol + "%");
            player.sendMessage("§7Используйте: /radio volume <0-100>");
            return;
        }

        try {
            int newVol = Integer.parseInt(args[1]);
            int maxVol = Main.getInstance().getConfig().getInt("max-volume", 100);

            if (newVol < 0 || newVol > maxVol) {
                player.sendMessage("§cГромкость должна быть от 0 до " + maxVol + "%");
                return;
            }

            RadioSession session = Main.getInstance().activeSessions.get(player.getUniqueId());
            if (session != null) {
                session.setVolume(newVol);
                player.sendMessage("§a📻 Громкость радио изменена на §f" + newVol + "%");
            } else {
                player.sendMessage("§cРадио сейчас не играет, но настройка сохранена.");
            }
        } catch (NumberFormatException e) {
            player.sendMessage("§cПожалуйста, укажите числовое значение громкости.");
        }
    }

    private void handleList(Player player) {
        ConfigurationSection stationsSection = Main.getInstance().getConfig().getConfigurationSection("stations");
        if (stationsSection == null || stationsSection.getKeys(false).isEmpty()) {
            player.sendMessage("§eНет сохраненных пресетов радиостанций. Вы можете включить радио по любой URL ссылке: §f/radio play <url>");
            return;
        }

        player.sendMessage("§e📻 Доступные радиостанции:");
        for (String key : stationsSection.getKeys(false)) {
            StationInfo info = extractStationInfo(stationsSection, key);

            Component stationComponent = Component.text("  • ")
                    .color(NamedTextColor.GRAY)
                    .append(Component.text(info.name)
                            .color(NamedTextColor.GOLD)
                            .decorate(TextDecoration.BOLD));

            if (info.genre != null && !info.genre.isEmpty()) {
                stationComponent = stationComponent
                        .append(Component.text(" ["))
                        .color(NamedTextColor.DARK_GRAY)
                        .append(Component.text(info.genre)
                                .color(NamedTextColor.AQUA))
                        .append(Component.text("]"))
                        .color(NamedTextColor.DARK_GRAY);
            }

            stationComponent = stationComponent
                    .append(Component.text(" "))
                    .append(Component.text("[ВКЛЮЧИТЬ]")
                            .color(NamedTextColor.GREEN)
                            .clickEvent(ClickEvent.runCommand("/radio play " + key)));

            player.sendMessage(stationComponent);
        }
        player.sendMessage("§7Также вы можете запустить любой прямой URL: /radio play <https://...>");
    }

    private void handleGetDisc(Player player) {
        org.bukkit.inventory.ItemStack disc = Main.getInstance().getDiscManager().createRadioDisc();
        player.getInventory().addItem(disc);
        player.sendMessage("§a📻 Вы получили Радио-кассету! Зажмите ПКМ удерживая её в руке для настройки.");
    }

    private void handleSetWave(Player player, String[] args) {
        if (args.length < 2) return;
        org.bukkit.inventory.ItemStack held = player.getInventory().getItemInMainHand();
        RadioDiscManager discMgr = Main.getInstance().getDiscManager();

        if (!discMgr.isRadioDisc(held)) {
            player.sendMessage("§cДля настройки вы должны держать Радио-кассету в основной руке!");
            return;
        }

        String target = String.join(" ", Arrays.copyOfRange(args, 1, args.length)).trim();
        StationInfo station = findStation(target);

        if (station == null || station.url == null || station.url.isEmpty()) {
            player.sendMessage("§cНе удалось найти эту радиостанцию.");
            return;
        }

        discMgr.setStation(held, station.name, station.url);
        player.sendMessage("§a📻 Волна кассеты успешно установлена: §f" + station.name);
    }

    private void handleSetDist(Player player, String[] args) {
        if (args.length < 2) return;
        org.bukkit.inventory.ItemStack held = player.getInventory().getItemInMainHand();
        RadioDiscManager discMgr = Main.getInstance().getDiscManager();

        if (!discMgr.isRadioDisc(held)) {
            player.sendMessage("§cДля настройки вы должны держать Радио-кассету в основной руке!");
            return;
        }

        try {
            int dist = Integer.parseInt(args[1]);
            discMgr.setDistance(held, dist);
            player.sendMessage("§a📻 3D-Дальность кассеты успешно установлена: §e" + dist + " блоков");
        } catch (NumberFormatException ignored) {}
    }

    private boolean handleReload(CommandSender sender) {
        if (!sender.hasPermission("voiceradio.admin")) {
            sender.sendMessage("§cУ вас нет прав на использование этой команды.");
            return true;
        }

        Main.getInstance().reloadConfig();
        sender.sendMessage("§a📻 Конфигурация VoiceRadio успешно перезагружена!");
        return true;
    }

    private void sendHelp(Player player) {
        player.sendMessage("§e=== 📻 Управление VoiceRadio ===");
        player.sendMessage("§f/radio disc §7— Получить настраиваемую Радио-кассету");
        player.sendMessage("§f/radio play <url|пресет> §7— Включить радио персонально");
        player.sendMessage("§f/radio stop §7— Выключить персональное радио");
        player.sendMessage("§f/radio volume <0-100> §7— Настроить персональную громкость");
        player.sendMessage("§f/radio list §7— Посмотреть список готовых радиостанций");
        if (player.hasPermission("voiceradio.admin")) {
            player.sendMessage("§f/radio reload §7— Перезагрузить конфигурацию");
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            List<String> subCommands = Arrays.asList("play", "stop", "volume", "list", "disc");
            return subCommands.stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length >= 2 && args[0].equalsIgnoreCase("play")) {
            String currentArg = String.join(" ", Arrays.copyOfRange(args, 1, args.length)).toLowerCase();
            ConfigurationSection stationsSection = Main.getInstance().getConfig().getConfigurationSection("stations");
            if (stationsSection != null) {
                for (String key : stationsSection.getKeys(false)) {
                    if (key.toLowerCase().startsWith(currentArg)) {
                        completions.add(key);
                    }
                }
            }
            if ("https://".startsWith(currentArg)) {
                completions.add("https://");
            }
            return completions;
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("volume")) {
            return Arrays.asList("10", "20", "50", "80", "100").stream()
                    .filter(s -> s.startsWith(args[1]))
                    .collect(Collectors.toList());
        }

        return completions;
    }
}
