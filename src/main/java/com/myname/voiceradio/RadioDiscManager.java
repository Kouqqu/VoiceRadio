package com.myname.voiceradio;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class RadioDiscManager {

    private final NamespacedKey KEY_IS_RADIO;
    private final NamespacedKey KEY_STATION_NAME;
    private final NamespacedKey KEY_STATION_URL;
    private final NamespacedKey KEY_DISTANCE;

    public RadioDiscManager(Main plugin) {
        this.KEY_IS_RADIO = new NamespacedKey(plugin, "is_radio_disc");
        this.KEY_STATION_NAME = new NamespacedKey(plugin, "station_name");
        this.KEY_STATION_URL = new NamespacedKey(plugin, "station_url");
        this.KEY_DISTANCE = new NamespacedKey(plugin, "distance");
    }

    public ItemStack createRadioDisc() {
        ItemStack disc = new ItemStack(Material.MUSIC_DISC_OTHERSIDE);
        ItemMeta meta = disc.getItemMeta();
        if (meta != null) {
            PersistentDataContainer pdc = meta.getPersistentDataContainer();
            pdc.set(KEY_IS_RADIO, PersistentDataType.BYTE, (byte) 1);
            pdc.set(KEY_STATION_NAME, PersistentDataType.STRING, "Не настроено");
            pdc.set(KEY_STATION_URL, PersistentDataType.STRING, "");
            
            int defaultDist = Main.getInstance().getConfig().getInt("3d-radio.default-distance", 15);
            pdc.set(KEY_DISTANCE, PersistentDataType.INTEGER, defaultDist);
            
            disc.setItemMeta(meta);
            updateLore(disc);
        }
        return disc;
    }

    public boolean isRadioDisc(ItemStack item) {
        if (item == null || item.getType() == Material.AIR || !item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        return pdc.has(KEY_IS_RADIO, PersistentDataType.BYTE);
    }

    public String getStationName(ItemStack item) {
        if (!isRadioDisc(item)) return "Не настроено";
        ItemMeta meta = item.getItemMeta();
        return meta.getPersistentDataContainer().getOrDefault(KEY_STATION_NAME, PersistentDataType.STRING, "Не настроено");
    }

    public String getStationUrl(ItemStack item) {
        if (!isRadioDisc(item)) return "";
        ItemMeta meta = item.getItemMeta();
        return meta.getPersistentDataContainer().getOrDefault(KEY_STATION_URL, PersistentDataType.STRING, "");
    }

    public int getDistance(ItemStack item) {
        int defaultDist = Main.getInstance().getConfig().getInt("3d-radio.default-distance", 15);
        if (!isRadioDisc(item)) return defaultDist;
        ItemMeta meta = item.getItemMeta();
        return meta.getPersistentDataContainer().getOrDefault(KEY_DISTANCE, PersistentDataType.INTEGER, defaultDist);
    }

    public void setStation(ItemStack item, String name, String url) {
        if (!isRadioDisc(item)) return;
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        pdc.set(KEY_STATION_NAME, PersistentDataType.STRING, name);
        pdc.set(KEY_STATION_URL, PersistentDataType.STRING, url);
        item.setItemMeta(meta);
        updateLore(item);
    }

    public void setDistance(ItemStack item, int distance) {
        if (!isRadioDisc(item)) return;
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        pdc.set(KEY_DISTANCE, PersistentDataType.INTEGER, distance);
        item.setItemMeta(meta);
        updateLore(item);
    }

    public void updateLore(ItemStack item) {
        if (!isRadioDisc(item)) return;
        ItemMeta meta = item.getItemMeta();

        String stationName = getStationName(item);
        int distance = getDistance(item);

        meta.displayName(Component.text("📻 Радио-кассета")
                .color(NamedTextColor.GOLD)
                .decorate(TextDecoration.BOLD));

        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("Текущая волна: ").color(NamedTextColor.GRAY)
                .append(Component.text(stationName).color(NamedTextColor.YELLOW).decorate(TextDecoration.BOLD)));
        lore.add(Component.text("Дальность 3D-звука: ").color(NamedTextColor.GRAY)
                .append(Component.text(distance + " блоков").color(NamedTextColor.GREEN)));
        lore.add(Component.empty());
        lore.add(Component.text("[ПКМ в руке — настроить волну]").color(NamedTextColor.DARK_GRAY));

        meta.lore(lore);
        item.setItemMeta(meta);
    }
}
