# SlotMatch (BD-J Blu-ray Game)

A simple and fun 1080p tile-matching puzzle game built for the Blu-ray platform to demonstrate the system's gaming capabilities.

Co-developed by **aefirestone** and **Gemini** as an open-source experimental homebrew project.

---

---

## 📊 Hardware Compatibility & Status

| Platform / Player | Memory / Heap Profile | Status | Notes |
| :--- | :--- | :--- | :--- |
| **CyberLink PowerDVD** | `2 / 2 MB RAM` \| `32 MB Max Heap` | ✅ **Perfect** | Runs perfectly with full rendering, audio, and menu navigation. |
| **PS4 (Blu-ray App)** | `6 / 16 MB RAM` \| `23 MB Max Heap` | 🟡 **Needs Polish** | Game runs but missing slot symbol grapghics (SLOTS.png). |
| **Philips BDP1502/F7A** | `1-2 / 2 MB RAM` \| `16 MB Max Heap` | 🟡 **Needs Polish** | Severe input lag when navigating with the remote control. |
| **Sony BDP-S6700** | `1-2 / 2 MB RAM` \| `16 MB Max Heap` | 🟡 **Needs Polish** | Event handling functional; occasional remote contention with background system events. |
| **Sony BDP-S390** | *Hardware Specs Pending* | ❌ **Known Issue** | Suffers from black screen with audio |

---
All of these players' firmware has been up to date.

## 🛠 Known Issues & Areas for Contribution

We pushed the code as far as we could within the constraints of standard Java 1.3 / BD-J profiles, but a few hurdles remain open for the community to tackle:

1. **Black Screen (BDP-S390):** Older Sony firmware profiles experience a blank rendering surface while audio continues to stream, pointing to a graphics plane compositing mismatch. (A hunch)
2. **Audio Playlist Fallback:** While multi-track audio initialization is structured for playlists `00000`–`00004`, strict standalone hardware players occasionally default to stream `00000` if secondary `.mpls` index tables aren't explicitly verified by the authoring tool.
3. **Memory Garbage Collection:** Extended play sessions on lower-spec hardware may require tighter tracking of temporary image buffers during cascade matches.
4. **Optimization:** The game was intenteded to play on the weakest Blu-ray player new and old.

---

## 🤝 Contributing

Contributions, bug fixes, and hardware test reports are more than welcome! Whether you're an experienced embedded Java developer, a homebrew enthusiast, or someone with a stack of physical Blu-ray players to test on, feel free to open a Pull Request or Issue.

---

## 📄 License

This project is open-source under the **MIT License**. See the [LICENSE](LICENSE) file for details.

Borrowed additional class files (all located in the 'engine' directory) from BluBird BD-J game. Copyright 2018 - 2021 Disc Quest

Music by Eric Matyas
[www.soundimage.org](https://soundimage.org/puzzle-music/)
