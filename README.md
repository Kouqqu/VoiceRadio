# 📻 VoiceRadio

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Minecraft](https://img.shields.io/badge/Minecraft-26.1+-brightgreen.svg)]()
[![Java](https://img.shields.io/badge/Java-25-blue.svg)]()

> *[English](#english) • [Русский](#russian)*

---

<a name="english"></a>
## 🇬🇧 English

**VoiceRadio** is a powerful Minecraft server plugin for **Paper / Purpur / Spigot / Bukkit** that allows players to stream online radio directly in-game through proximity voice chat (**Plasmo Voice** or **Simple Voice Chat**). It also turns regular music discs into **3D Radio Cassettes** and jukeboxes into world boomboxes!

### ✨ Features

- 🎧 **Personal Radio**: Listen to your favorite radio stations directly in your headphones without third-party apps or browser tabs.
- 🔊 **3D Radio Cassettes & Boomboxes (Jukebox)**: Convert music discs into custom radio cassettes, insert them into jukeboxes, and throw parties with realistic 3D spatial audio for all nearby players!
- 🎚️ **Customizable Broadcast Radius**: Set the 3D spatial sound range for your jukebox (5, 10, 15, 20, 25, 30 blocks).
- 📻 **Presets & Direct URLs**: Pre-configure favorite radio stations (MP3, AAC, Icecast, Shoutcast) in `config.yml` or play any direct audio stream URL on the fly.
- 🎙️ **Dual Voice Chat Support**: Seamlessly works with either **Plasmo Voice** or **Simple Voice Chat**.

### 📋 Commands & Permissions

| Command | Description | Permission |
| :--- | :--- | :--- |
| `/radio play <url\|preset>` | Play an online radio stream personally | *Everyone* |
| `/radio stop` | Stop your personal radio | *Everyone* |
| `/radio volume <0-100>` | Adjust personal volume | *Everyone* |
| `/radio list` | View list of available station presets | *Everyone* |
| `/radio disc` or `/radio get` | Get a custom configurable Radio Cassette | *Everyone* |
| `/radio setwave <url\|preset>` | Tune the radio station onto the cassette in hand | *Everyone* |
| `/radio setdist <distance>` | Set 3D sound broadcast distance for cassette in hand | *Everyone* |
| `/radio reload` or `/radioreload` | Reload plugin configuration | `voiceradio.admin` |

### ⚙️ Compatibility & Requirements

- **Minecraft Version**: `26.1` and higher
- **Server Software**: `Paper`, `Purpur`, `Spigot`, `Bukkit`
- **Java Runtime**: `Java 25`
- **Voice Chat Plugin (either one)**:
  - [Plasmo Voice](https://modrinth.com/plugin/plasmo-voice) (with Plasmo LavaPlayer Lib)
  - [Simple Voice Chat](https://modrinth.com/plugin/simple-voice-chat)

### 🚀 Installation

1. Download the latest `VoiceRadio-1.0.jar` from the [Releases](https://github.com/Kouqqu/VoiceRadio/releases) tab.
2. Drop it into your server's `plugins/` directory.
3. Make sure **Plasmo Voice** or **Simple Voice Chat** is installed.
4. Restart or start your server.
5. Configure radio station presets in `plugins/VoiceRadio/config.yml`.

---

<a name="russian"></a>
## 🇷🇺 Русский

**VoiceRadio** — мощный плагин для серверов Minecraft (**Paper / Purpur / Spigot / Bukkit**), позволяющий игрокам слушать онлайн-радиопотоки прямо в игре через голосовой чат (**Plasmo Voice** или **Simple Voice Chat**), а также создавать 3D-бумбоксы и радио-кассеты для обычных проигрывателей (Jukebox).

### ✨ Особенности

- 🎧 **Персональное радио**: Слушайте любимые радиостанции прямо в наушниках без сторонних сайтов и приложений.
- 🔊 **3D Радио-кассеты (Boombox / Jukebox)**: Превращайте обычные пластинки в настоящие радио-кассеты, вставляйте их в проигрыватель и устраивайте вечеринки с объемным пространственным звуком для всех окружающих игроков!
- 🎚️ **Гибкая настройка дистанции**: Настраивайте радиус вещания 3D-звука пластинки (5, 10, 15, 20, 25, 30 блоков).
- 📻 **Поддержка пресетов и прямых ссылок**: Добавляйте любые интернет-радиостанции (MP3, AAC, Icecast, Shoutcast и др.) в `config.yml` или воспроизводите потоки по прямой ссылке на лету.
- 🎙️ **Два голосовых чата**: Полная поддержка **Plasmo Voice** и **Simple Voice Chat**.

### 📋 Команды и права

| Команда | Описание | Право |
| :--- | :--- | :--- |
| `/radio play <ссылка\|пресет>` | Запустить радиостанцию персонально для себя | *Для всех* |
| `/radio stop` | Остановить персональное радио | *Для всех* |
| `/radio volume <0-100>` | Изменить персональную громкость | *Для всех* |
| `/radio list` | Список доступных пресетов станций | *Для всех* |
| `/radio disc` / `/radio get` | Получить радио-кассету для проигрывателя | *Для всех* |
| `/radio setwave <ссылка\|пресет>` | Настроить радиоволну на пластинке в руках | *Для всех* |
| `/radio setdist <дистанция>` | Настроить радиус вещания 3D-звука пластинки в руках | *Для всех* |
| `/radio reload` или `/radioreload` | Перезагрузить конфигурацию плагина | `voiceradio.admin` |

### ⚙️ Требования и совместимость

- **Версия Minecraft**: `26.1` и выше
- **Серверные ядра**: `Paper`, `Purpur`, `Spigot`, `Bukkit`
- **Java**: `Java 25`
- **Голосовой чат (один из двух на выбор)**:
  - [Plasmo Voice](https://modrinth.com/plugin/plasmo-voice) + Plasmo LavaPlayer Lib
  - [Simple Voice Chat](https://modrinth.com/plugin/simple-voice-chat)

### 🚀 Установка

1. Скачайте `VoiceRadio-1.0.jar` из раздела [Releases](https://github.com/Kouqqu/VoiceRadio/releases).
2. Поместите `.jar` файл в папку `plugins/` вашего сервера.
3. Убедитесь, что на сервере установлен **Plasmo Voice** или **Simple Voice Chat**.
4. Запустите или перезапустите сервер.
5. Настройте список радиостанций в файле `plugins/VoiceRadio/config.yml`.

---

## 📄 Лицензия

Проект распространяется под лицензией [MIT](LICENSE).
