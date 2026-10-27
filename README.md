# 💎 SlotMatch (BD-J Blu-ray Game)

A simple and fun 1080p tile-matching puzzle game built for the Blu-ray platform to demonstrate its gaming capabilities.

Co-developed by **aefirestone** and **Gemini** as an open-source experimental homebrew project.

<p align="center">
  <a href=""><img src= "https://raw.githubusercontent.com/aefirestone/SlotMatch/refs/heads/main/SlotMatch_screenshot.jpg" alt="Play in browser"></a>
</p>

## 🕹 Features

1. **Simple Gameplay:** Nothing too fancy, similar to the original Bejewled, match the slot symbols to earn points and reach your highest score.
2. **1080p Graphics:** Nice 2D graphics in full HD
3. **LPCM audio:** Listen to Eric Matyas' background music during gameplay
4. **Colored Buttons** Using your remote, press 🟩 to reveal a hint, and press 🟨 to turn off music anytime.

---

---

## 📊 Hardware Compatibility & Status*

| Platform / Player | Memory / Heap Profile | Status | Notes |
| :--- | :--- | :--- | :--- |
| **CyberLink PowerDVD** | `2 / 2 MB RAM` \| `32 MB Max Heap` | ✅ **Perfect** | Runs perfectly with full rendering, audio, and menu navigation. |
| **PS4** | `6 / 16 MB RAM` \| `23 MB Max Heap` | 🟡 **Needs Polish** | Game runs but missing slot symbol graphics (SLOTS.png). |
| **Philips BDP1502/F7A** | `1-2 / 2 MB RAM` \| `16 MB Max Heap` | 🟡 **Needs Polish** | Severe input lag when navigating with the remote control. |
| **Sony BDP-S6700** | `1-2 / 2 MB RAM` \| `16 MB Max Heap` | 🟡 **Needs Polish** | Severe input lag when navigating with the remote control. |
| **Sony BDP-S390** | *Hardware Specs N/A* | ❌ **Known Issue** | Black screen with audio |
| **Seikou¹** | *Hardware Specs N/A* | ❌ **Known Issue** | Black screen with audio |
| **VLC¹** | *Hardware Specs N/A* | ❌ **Known Issue** | Cannot launch game due to being stuck in a broken/cropped menu screen / missing "background.jpg" |

---
*All of these players' firmware has been up to date. 
¹VLC and Seikou are community made Blu-ray emulators that uses libbluray, may not be 100% accurate with BD specs

## 🛠 Known Issues & Areas for Contribution

1. **Black Screen (BDP-S390/Seikou):** Some players experience a blank screen while audio continues to stream, pointing to a graphics plane compositing mismatch.
2. **Audio Playlist Fallback:** Could not make multi-track audio playlists to work `00000`–`00004`, strict standalone hardware players occasionally default to stream `00000` if secondary `.mpls` index tables aren't explicitly verified by the authoring tool.
3. **Memory Garbage Collection:** Extended play sessions on lower-spec hardware may require tighter tracking of temporary image buffers during cascade matches.
4. **Optimization:** The game was intended to play on the weakest Blu-ray player new and old, and be BD compliant. Optimizing the game to the lowest common denominator will help make the game run in most players.

---

## 🤝 Contributing

Contributions, bug fixes, and hardware test reports are more than welcome! Whether you're an experienced embedded Java developer, a homebrew enthusiast, or someone with a stack of physical Blu-ray players to test on, feel free to open a Pull Request or Issue.

---

## 📄 License

This project is open-source under the **MIT License**. See the [LICENSE](LICENSE) file for details.

Borrowed additional class files (all located in the 'engine' directory) from BluBird BD-J game. Copyright 2018 - 2021 Disc Quest

Music by Eric Matyas
[www.soundimage.org](https://soundimage.org/puzzle-music/)
