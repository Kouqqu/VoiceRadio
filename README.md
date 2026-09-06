<div align="center">

# 📻 VoiceRadio

**[🇬🇧 English](#english) • [🇷🇺 Русский](#russian)**

</div>

---

<a id="english"></a>
## 🇬🇧 English

**VoiceRadio** is a powerful Minecraft server plugin that allows players to stream online radio directly in-game through proximity voice chat (**Plasmo Voice** or **Simple Voice Chat**), as well as create radio cassettes for regular jukeboxes and listen to audio in 3D.

### ✨ Features

- 🎧 **Personal Radio**: Listen to your favorite radio stations directly in your headphones without third-party apps or browser tabs.
- 🔊 **3D Radio Cassettes (Boombox / Jukebox)**: Convert music discs into custom radio cassettes, insert them into jukeboxes, and throw parties with realistic 3D spatial audio for all nearby players!
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
| `/radio disc` / `/radio get` | Get a custom configurable Radio Cassette | *Everyone* |
| `/radio setwave <url\|preset>` | Tune the radio station onto the cassette in hand | *Everyone* |
| `/radio setdist <distance>` | Set 3D sound broadcast distance for cassette in hand | *Everyone* |
| `/radio reload` or `/radioreload` | Reload plugin configuration | `voiceradio.admin` |

### ⚙️ Compatibility & Requirements

- **Minecraft Version**: `26.1` and higher
- **Server Software**: `Paper`, `Purpur`, `Spigot`, `Bukkit`
- **Java Runtime**: `Java 25`
- **Voice Chat Plugin (either one)**:
  - [Plasmo Voice](https://modrinth.com/plugin/plasmo-voice) + Plasmo LavaPlayer Lib
  - [Simple Voice Chat](https://modrinth.com/plugin/simple-voice-chat)

### 🚀 Installation

1. Place the `.jar` file into your server's `plugins` folder.
2. Make sure **Plasmo Voice** or **Simple Voice Chat** is installed on the server.
3. Restart the server.
4. Configure the radio stations list in `plugins/VoiceRadio/config.yml`.

---

<a id="russian"></a>
## 🇷🇺 Русский

**VoiceRadio** — это мощный плагин для серверов Minecraft, позволяющий игрокам слушать онлайн-радиопотоки прямо в игре через голосовой чат (**Plasmo Voice** или **Simple Voice Chat**), а также создавать радио-кассеты для обычных проигрывателей и слушать аудио в 3D.

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

1. Поместите `.jar` файл в папку `plugins` вашего сервера.
2. Убедитесь, что на сервере установлен **Plasmo Voice** или **Simple Voice Chat**.
3. Перезапустите сервер.
4. Настройте список радиостанций в файле `plugins/VoiceRadio/config.yml`.

---

## 📄 Лицензия / License

Проект распространяется под лицензией [MIT](LICENSE).
