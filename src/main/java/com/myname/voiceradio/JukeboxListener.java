package com.myname.voiceradio;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Jukebox;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class JukeboxListener implements Listener {

    private final Main plugin;

    public JukeboxListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;

        Player player = event.getPlayer();
        ItemStack heldItem = player.getInventory().getItemInMainHand();
        Block block = event.getClickedBlock();
        Action action = event.getAction();

        boolean holdsRadioDisc = plugin.getDiscManager().isRadioDisc(heldItem);

        // 1. Клике с кассетой в руке (в воздух или с зажатым Shift по блоку) -> Открываем настройки в чате
        if (holdsRadioDisc && (action == Action.RIGHT_CLICK_AIR || (action == Action.RIGHT_CLICK_BLOCK && player.isSneaking()))) {
            event.setCancelled(true);
            openDiscSettingsChat(player, heldItem);
            return;
        }

        // 2. Взаимодействие с Проигрывателем (Jukebox)
        if (block != null && block.getType() == Material.JUKEBOX && action == Action.RIGHT_CLICK_BLOCK) {
            Location loc = block.getLocation();

            // Если в проигрывателе уже играет 3D-радио (Plasmo или SimpleVoice)
            if (plugin.active3DSessions.containsKey(loc) || plugin.active3DStopActions.containsKey(loc)) {
                event.setCancelled(true);
                Radio3DSession session = plugin.stop3DRadio(loc);

                if (session != null && session.getDiscItem() != null) {
                    loc.getWorld().dropItemNaturally(loc.clone().add(0.5, 1.0, 0.5), session.getDiscItem());
                }

                player.sendMessage("§c📻 3D-Радио извлечено из проигрывателя.");
                return;
            }

            // Если радио не играет и игрок кликает по проигрывателю радио-кассетой
            if (holdsRadioDisc && !player.isSneaking()) {
                event.setCancelled(true); // КРИТИЧЕСКИ ВАЖНО: Всегда отменяем ванильный клик!

                String streamUrl = plugin.getDiscManager().getStationUrl(heldItem);
                String stationName = plugin.getDiscManager().getStationName(heldItem);
                int distance = plugin.getDiscManager().getDistance(heldItem);

                if (streamUrl == null || streamUrl.isEmpty()) {
                    player.sendMessage("§cЭта радио-кассета еще не настроена!");
                    player.sendMessage("§7Нажмите ПКМ кассетой в руке, чтобы выбрать радиостанцию.");
                    return;
                }

                ItemStack discToInsert = heldItem.clone();
                discToInsert.setAmount(1);

                if (player.getGameMode() != org.bukkit.GameMode.CREATIVE) {
                    heldItem.setAmount(heldItem.getAmount() - 1);
                }

                // Запускаем 3D-радио на блоке
                plugin.getRadioAddon().play3DRadio(loc, streamUrl, stationName, distance, discToInsert);

                player.sendMessage("§a📻 Радио-кассета вставлена в проигрыватель!");
                player.sendMessage("§aСейчас играет: §f" + stationName + " §a(Дальность: §e" + distance + " блоков§a)");
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        Location loc = block.getLocation();
        if (plugin.active3DSessions.containsKey(loc) || plugin.active3DStopActions.containsKey(loc)) {
            Radio3DSession session = plugin.stop3DRadio(loc);
            if (session != null && session.getDiscItem() != null) {
                loc.getWorld().dropItemNaturally(loc.clone().add(0.5, 0.5, 0.5), session.getDiscItem());
            }
        }
    }

    public static void openDiscSettingsChat(Player player, ItemStack disc) {
        Main plugin = Main.getInstance();
        String currentStation = plugin.getDiscManager().getStationName(disc);
        int currentDist = plugin.getDiscManager().getDistance(disc);

        player.sendMessage("§e=== 📻 Настройка Радио-кассеты ===");
        player.sendMessage("§7Текущая станция: §f" + currentStation + " §7| Радиус: §a" + currentDist + " блоков");
        player.sendMessage("§eВыберите радиостанцию:");

        ConfigurationSection stationsSection = plugin.getConfig().getConfigurationSection("stations");
        if (stationsSection != null) {
            for (String key : stationsSection.getKeys(false)) {
                String name = key.toUpperCase();
                String genre = "";
                if (stationsSection.isConfigurationSection(key)) {
                    ConfigurationSection sub = stationsSection.getConfigurationSection(key);
                    name = sub.getString("name", key);
                    genre = sub.getString("genre", "");
                }

                Component comp = Component.text("  • ").color(NamedTextColor.GRAY)
                        .append(Component.text(name).color(NamedTextColor.GOLD).decorate(TextDecoration.BOLD));

                if (!genre.isEmpty()) {
                    comp = comp.append(Component.text(" [" + genre + "]").color(NamedTextColor.AQUA));
                }

                comp = comp.append(Component.text(" "))
                        .append(Component.text("[ВЫБРАТЬ]")
                                .color(NamedTextColor.GREEN)
                                .clickEvent(ClickEvent.runCommand("/radio setwave " + key)));

                player.sendMessage(comp);
            }
        }

        // Кнопки радиуса 3D-звука (чистые цифры)
        Component distComp = Component.text("Дальность 3D-звука: ").color(NamedTextColor.YELLOW);
        List<Integer> allowedDists = plugin.getConfig().getIntegerList("3d-radio.allowed-distances");
        if (allowedDists.isEmpty()) {
            allowedDists = List.of(5, 10, 15, 20, 25, 30);
        }

        for (int dist : allowedDists) {
            NamedTextColor btnColor = (dist == currentDist) ? NamedTextColor.GREEN : NamedTextColor.GRAY;
            distComp = distComp.append(Component.text("[" + dist + "] ")
                    .color(btnColor)
                    .clickEvent(ClickEvent.runCommand("/radio setdist " + dist)));
        }

        player.sendMessage(distComp);
    }
}
