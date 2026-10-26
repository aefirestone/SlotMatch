package org.homebrew;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import javax.tv.xlet.*;
import org.havi.ui.*;
import javax.media.Manager;
import javax.media.Player;
import javax.media.ControllerListener;
import javax.media.ControllerEvent;
import javax.media.EndOfMediaEvent;
import javax.media.Time;
import org.bluray.net.BDLocator;
import org.davic.media.MediaLocator;
import org.dvb.event.EventManager;
import org.dvb.event.UserEvent;
import org.dvb.event.UserEventListener;
import org.dvb.event.UserEventRepository;
import org.dvb.ui.DVBBufferedImage;

public class MyXlet extends java.awt.Container implements javax.tv.xlet.Xlet, java.awt.event.KeyListener, UserEventListener {

    private HScene scene;
    private HGraphicsDevice graphicsDevice;
    private Image bgImage;
    private Image slotsImage;
    private Image[] tileImages;
    private Image backbuffer;

    private int[][] board;
    private final int GRID_SIZE = 8;
    
    // Game States: 0 = Menu, 1 = Playing, 2 = Game Over
    private final int STATE_MENU = 0;
    private final int STATE_PLAYING = 1;
    private final int STATE_GAMEOVER = 2;
    private int gameState = STATE_MENU;

    // Menu Selections: 0 = Normal Game, 1 = Debug Stats Mode
    private int menuIndex = 0;
    private boolean debugMode = false;

    private int level = 1;
    private int cursorRow = 0;
    private int cursorCol = 0;   // -1 represents the left sidebar menu
    private int sidebarIndex = 0; // 0 = Hint Button, 1 = Music Button
    private int selectedRow = -1;
    private int selectedCol = -1;

    private int score = 0;
    
    // Hint System
    private int hintRow1 = -1, hintCol1 = -1, hintRow2 = -1, hintCol2 = -1;
    private int hintTimer = 0;

    // Randomized Multi-Track Audio Control (Playlists 00000 - 00004)
    private Player videoplayer;
    private int currentTrack = 0;
    private boolean musicPlaying = true;

    // Arcade Name Entry Variables
    private char[] nameEntry = {'P', 'L', 'Y'};
    private int nameEntryIndex = 0;

    private Font font;
    private Font boldFont;
    private Font smallFont;
    private Font tinyFont;

    // Full 1080p Layout Coordinates (Centered 880x880 Board)
    private final int BOARD_X = 520;
    private final int BOARD_Y = 100;
    private final int TILE_SIZE = 110; // 880 / 8 = 110px per tile

    // High Score Data Structures
    private String[] hsNames = new String[10];
    private int[] hsScores = new int[10];

    public void initXlet(javax.tv.xlet.XletContext context) {
        setSize(1920, 1080);
        setBounds(0, 0, 1920, 1080);

        try {
            scene = org.havi.ui.HSceneFactory.getInstance().getDefaultHScene();
            scene.setBounds(0, 0, 1920, 1080);
            scene.add(this);
            scene.validate();
        } catch (Exception e) {}

        setFocusable(true);

        // Register DVB User Events for Low-Latency Hardware Input (including Remote Color Buttons)
        try {
            UserEventRepository repo = new UserEventRepository("SlotMatchGameInput");
            repo.addAllArrowKeys();
            repo.addAllNumericKeys();
            repo.addKey(10);  // ENTER / OK
            repo.addKey(461); // BACK
            repo.addKey(403); // Red Button
            repo.addKey(404); // Green Button (Music Toggle)
            repo.addKey(405); // Yellow Button (Hint)
            repo.addKey(406); // Blue Button
            EventManager.getInstance().addUserEventListener(this, repo);
        } catch (Exception e) {}

        initRandomAudioPlayer();
        initHighScores();
        loadScores();
        loadAssets();
        initBoard();
    }

    private void initRandomAudioPlayer() {
        int randomTrack = (int)(Math.random() * 5); // Robustly picks 0, 1, 2, 3, or 4
        initAudioPlayer(randomTrack);
    }

	private void initAudioPlayer(int trackIndex) {
        try {
            if (videoplayer != null) {
                videoplayer.stop();
                videoplayer.deallocate();
                videoplayer.close();
                videoplayer = null;
            }
            
            // Explicitly format to 5-digit Blu-ray playlist format (00000 - 00004)
            String trackStr = (trackIndex < 10) ? "0000" + trackIndex : "000" + trackIndex;
            String playlistStr = "bd://PLAYLIST:" + trackStr;
            
            BDLocator bdLoc = new BDLocator(playlistStr);
            MediaLocator locator = new MediaLocator(bdLoc);
            
            videoplayer = Manager.createPlayer(locator);
            videoplayer.addControllerListener(new ControllerListener() {
                public void controllerUpdate(ControllerEvent event) {
                    if (event instanceof EndOfMediaEvent) {
                        if (musicPlaying && videoplayer != null) {
                            initRandomAudioPlayer();
                        }
                    }
                }
            });
            videoplayer.realize();
            videoplayer.start();
            currentTrack = trackIndex;
            musicPlaying = true;
            System.out.println("Successfully playing playlist: " + playlistStr);
        } catch (Exception e) {
            System.err.println("Couldn't load track " + trackIndex + " (" + e.toString() + "). Make sure " + trackIndex + ".mpls exists in BDMV/PLAYLIST/!");
        }
    }

    private void toggleMusic() {
        try {
            if (musicPlaying) {
                if (videoplayer != null) {
                    videoplayer.stop();
                }
                musicPlaying = false;
            } else {
                initRandomAudioPlayer();
            }
        } catch (Exception e) {
            System.err.println("Error toggling music: " + e.toString());
        }
    }

    public void startXlet() {
        setVisible(true);
        if (scene != null) scene.setVisible(true);
        requestFocus();
        repaint();
    }

    public void pauseXlet() {
        setVisible(false);
    }

    public void destroyXlet(boolean unconditional) {
        if (videoplayer != null) {
            try { videoplayer.stop(); videoplayer.deallocate(); videoplayer.close(); } catch (Exception e) {}
            videoplayer = null;
        }
        try {
            EventManager.getInstance().removeUserEventListener(this);
        } catch (Exception e) {}
        if (scene != null) scene.remove(this);
        scene = null;
    }

    private File getScoreFile() {
        String root = null;
        try {
            root = System.getProperty("bluray.bindingunit.root");
        } catch (Exception e) {}
        
        if (root == null || root.length() == 0) {
            root = System.getProperty("user.dir");
        }
        return new File(root, "scores.txt");
    }

    private void initHighScores() {
        String[] defaultNames = {"SEVEN", "DIAMOND", "MELON", "HEART", "BELL", "BAR", "CHERRY", "LEMON", "HORSESHOE", "PLAYER"};
        int[] defaultScores = {1000, 900, 800, 700, 600, 500, 400, 300, 200, 100};
        for (int i = 0; i < 10; i++) {
            hsNames[i] = defaultNames[i];
            hsScores[i] = defaultScores[i];
        }
    }

    private void loadScores() {
        BufferedReader reader = null;
        try {
            File file = getScoreFile();
            if (!file.exists()) {
                saveScores();
                return;
            }
            reader = new BufferedReader(new FileReader(file));
            String line;
            int index = 0;
            while ((line = reader.readLine()) != null && index < 10) {
                int dashIdx = line.indexOf(" - ");
                if (dashIdx != -1) {
                    String name = line.substring(0, dashIdx).trim();
                    String scoreStr = line.substring(dashIdx + 3).trim();
                    hsNames[index] = name;
                    hsScores[index] = Integer.parseInt(scoreStr);
                    index++;
                }
            }
            sortScores();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try { reader.close(); } catch (Exception e) {}
            }
        }
    }

    private void saveScores() {
        BufferedWriter writer = null;
        try {
            File file = getScoreFile();
            writer = new BufferedWriter(new FileWriter(file));
            for (int i = 0; i < 10; i++) {
                writer.write(hsNames[i] + " - " + hsScores[i]);
                writer.newLine();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (writer != null) {
                try { writer.close(); } catch (Exception e) {}
            }
        }
    }

    private void sortScores() {
        for (int i = 0; i < 9; i++) {
            for (int j = i + 1; j < 10; j++) {
                if (hsScores[j] > hsScores[i]) {
                    int tempS = hsScores[i];
                    hsScores[i] = hsScores[j];
                    hsScores[j] = tempS;

                    String tempN = hsNames[i];
                    hsNames[i] = hsNames[j];
                    hsNames[j] = tempN;
                }
            }
        }
    }

    private void updateHighScoreCustom(String name, int currentScore) {
        if (currentScore <= 0) return;
        boolean placed = false;
        for (int i = 0; i < 10; i++) {
            if (hsNames[i].equals(name)) {
                if (currentScore > hsScores[i]) {
                    hsScores[i] = currentScore;
                }
                placed = true;
                break;
            }
        }
        if (!placed) {
            if (currentScore > hsScores[9]) {
                hsNames[9] = name;
                hsScores[9] = currentScore;
            }
        }
        sortScores();
        saveScores();
    }

    private void checkLevelProgression() {
        int targetLevel = (score / 1000) + 1;
        if (targetLevel > level) {
            level = targetLevel;
        }
    }

    private int getNumTileTypes() {
        return (level >= 3) ? 7 : 6;
    }

    private boolean hasPossibleMoves() {
        for (int r = 0; r < GRID_SIZE; r++) {
            for (int c = 0; c < GRID_SIZE; c++) {
                if (c + 1 < GRID_SIZE) {
                    if (testSwapSimulation(r, c, r, c + 1)) return true;
                }
                if (r + 1 < GRID_SIZE) {
                    if (testSwapSimulation(r, c, r + 1, c)) return true;
                }
            }
        }
        return false;
    }

    private boolean testSwapSimulation(int r1, int c1, int r2, int c2) {
        int temp = board[r1][c1];
        board[r1][c1] = board[r2][c2];
        board[r2][c2] = temp;

        boolean found = false;
        for (int r = 0; r < GRID_SIZE; r++) {
            for (int c = 0; c < GRID_SIZE - 2; c++) {
                int val = board[r][c];
                if (val != 0 && board[r][c+1] == val && board[r][c+2] == val) {
                    found = true;
                    break;
                }
            }
            if (found) break;
        }

        if (!found) {
            for (int c = 0; c < GRID_SIZE; c++) {
                for (int r = 0; r < GRID_SIZE - 2; r++) {
                    int val = board[r][c];
                    if (val != 0 && board[r+1][c] == val && board[r+2][c] == val) {
                        found = true;
                        break;
                    }
                }
                if (found) break;
            }
        }

        board[r2][c2] = board[r1][c1];
        board[r1][c1] = temp;

        return found;
    }

    private void findHint() {
        for (int r = 0; r < GRID_SIZE; r++) {
            for (int c = 0; c < GRID_SIZE; c++) {
                if (c + 1 < GRID_SIZE && testSwapSimulation(r, c, r, c + 1)) {
                    hintRow1 = r; hintCol1 = c; hintRow2 = r; hintCol2 = c + 1;
                    hintTimer = 50;
                    return;
                }
                if (r + 1 < GRID_SIZE && testSwapSimulation(r, c, r + 1, c)) {
                    hintRow1 = r; hintCol1 = c; hintRow2 = r + 1; hintCol2 = c;
                    hintTimer = 50;
                    return;
                }
            }
        }
    }

    private void loadAssets() {
        try {
            java.net.URL bgUrl = getClass().getResource("/Data/background.jpg");
            if (bgUrl != null) {
                bgImage = Toolkit.getDefaultToolkit().getImage(bgUrl);
            }

            java.net.URL slotsUrl = getClass().getResource("/Data/SLOTS.png");
            if (slotsUrl == null) {
                slotsUrl = getClass().getResource("/Data/slot.png");
            }
            if (slotsUrl != null) {
                slotsImage = Toolkit.getDefaultToolkit().getImage(slotsUrl);
            }

            MediaTracker tracker = new MediaTracker(this);
            if (bgImage != null) tracker.addImage(bgImage, 0);
            if (slotsImage != null) tracker.addImage(slotsImage, 1);
            tracker.waitForAll();

            int w = slotsImage != null ? slotsImage.getWidth(this) : 300;
            int h = slotsImage != null ? slotsImage.getHeight(this) : 300;
            if (w <= 0) w = 300;
            if (h <= 0) h = 300;
            
            int tw = w / 3;
            int th = h / 3;

            tileImages = new Image[9];
            for (int r = 0; r < 3; r++) {
                for (int c = 0; c < 3; c++) {
                    int index = r * 3 + c;
                    FilteredImageSource fis = new FilteredImageSource(
                        slotsImage.getSource(), 
                        new CropImageFilter(c * tw, r * th, tw, th)
                    );
                    tileImages[index] = createImage(fis);
                    tracker.addImage(tileImages[index], 2 + index);
                }
            }
            tracker.waitForAll();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initBoard() {
        java.util.Random rand = new java.util.Random();
        do {
            board = new int[GRID_SIZE][GRID_SIZE];
            for (int r = 0; r < GRID_SIZE; r++) {
                for (int c = 0; c < GRID_SIZE; c++) {
                    board[r][c] = 1 + rand.nextInt(getNumTileTypes());
                }
            }
        } while (resolveMatches() > 0 || !hasPossibleMoves());
    }

    private int resolveMatches() {
        boolean[][] horizMatched = new boolean[GRID_SIZE][GRID_SIZE];
        boolean[][] vertMatched = new boolean[GRID_SIZE][GRID_SIZE];

        for (int r = 0; r < GRID_SIZE; r++) {
            for (int c = 0; c < GRID_SIZE - 2; c++) {
                int val = board[r][c];
                if (val != 0 && board[r][c+1] == val && board[r][c+2] == val) {
                    int len = 3;
                    while (c + len < GRID_SIZE && board[r][c+len] == val) {
                        len++;
                    }
                    for (int i = 0; i < len; i++) {
                        horizMatched[r][c+i] = true;
                    }
                    c += len - 1;
                }
            }
        }

        for (int c = 0; c < GRID_SIZE; c++) {
            for (int r = 0; r < GRID_SIZE - 2; r++) {
                int val = board[r][c];
                if (val != 0 && board[r+1][c] == val && board[r+2][c] == val) {
                    int len = 3;
                    while (r + len < GRID_SIZE && board[r+len][c] == val) {
                        len++;
                    }
                    for (int i = 0; i < len; i++) {
                        vertMatched[r+i][c] = true;
                    }
                    r += len - 1;
                }
            }
        }

        boolean hasLT = false;
        int ltR = -1, ltC = -1;
        int matchCount = 0;

        for (int r = 0; r < GRID_SIZE; r++) {
            for (int c = 0; c < GRID_SIZE; c++) {
                if (horizMatched[r][c] || vertMatched[r][c]) {
                    matchCount++;
                    if (horizMatched[r][c] && vertMatched[r][c]) {
                        hasLT = true;
                        ltR = r;
                        ltC = c;
                    }
                }
            }
        }

        if (matchCount > 0) {
            if (hasLT && ltR != -1 && ltC != -1) {
                score += 250;
                checkLevelProgression();
                for (int r = 0; r < GRID_SIZE; r++) {
                    for (int c = 0; c < GRID_SIZE; c++) {
                        if (horizMatched[r][c] || vertMatched[r][c]) {
                            if (r == ltR && c == ltC) {
                                board[r][c] = 8;
                            } else {
                                board[r][c] = 0;
                            }
                        }
                    }
                }
            } else {
                for (int r = 0; r < GRID_SIZE; r++) {
                    for (int c = 0; c < GRID_SIZE; c++) {
                        if (horizMatched[r][c] || vertMatched[r][c]) {
                            if (board[r][c] == 8 || board[r][c] == 9) {
                                score += 500;
                            }
                            board[r][c] = 0;
                        }
                    }
                }
            }
        }

        return matchCount;
    }

    private void applyGravity() {
        java.util.Random rand = new java.util.Random();
        int maxTypes = getNumTileTypes();
        for (int c = 0; c < GRID_SIZE; c++) {
            int writeRow = GRID_SIZE - 1;
            for (int r = GRID_SIZE - 1; r >= 0; r--) {
                if (board[r][c] != 0) {
                    board[writeRow][c] = board[r][c];
                    if (writeRow != r) {
                        board[r][c] = 0;
                    }
                    writeRow--;
                }
            }
            for (int r = writeRow; r >= 0; r--) {
                board[r][c] = 1 + rand.nextInt(maxTypes);
            }
        }
    }

    private void handleSwap(int r1, int c1, int r2, int c2) {
        int temp = board[r1][c1];
        board[r1][c1] = board[r2][c2];
        board[r2][c2] = temp;

        int matches = resolveMatches();
        if (matches > 0) {
            score += matches * 10;
            checkLevelProgression();
            while (true) {
                applyGravity();
                int cascadeMatches = resolveMatches();
                if (cascadeMatches > 0) {
                    score += cascadeMatches * 15;
                    checkLevelProgression();
                } else {
                    break;
                }
            }
            hintTimer = 0;
            if (!hasPossibleMoves()) {
                gameState = STATE_GAMEOVER;
            }
        } else {
            board[r2][c2] = board[r1][c1];
            board[r1][c1] = temp;
        }
    }

    public void userEventReceived(UserEvent e) {
        if (e.getType() == 401) { // KEY_PRESSED
            int dvbCode = e.getCode();
            int mappedKey = dvbCode;
            switch (dvbCode) {
                case 37: mappedKey = KeyEvent.VK_LEFT; break;
                case 38: mappedKey = KeyEvent.VK_UP; break;
                case 39: mappedKey = KeyEvent.VK_RIGHT; break;
                case 40: mappedKey = KeyEvent.VK_DOWN; break;
                case 10: case 461: mappedKey = KeyEvent.VK_ENTER; break;
                case 404: mappedKey = KeyEvent.VK_M; break; // Green Button -> Music Toggle
                case 405: mappedKey = KeyEvent.VK_H; break; // Yellow Button -> Hint
            }
            keyPressed(new KeyEvent(this, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, mappedKey, (char)0));
        }
    }

    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();

        if (hintTimer > 0) hintTimer--;

        if (gameState == STATE_MENU) {
            if (keyCode == KeyEvent.VK_UP || keyCode == KeyEvent.VK_DOWN) {
                menuIndex = 1 - menuIndex;
            } else if (keyCode == KeyEvent.VK_ENTER || keyCode == KeyEvent.VK_ACCEPT) {
                debugMode = (menuIndex == 1);
                score = 0;
                level = 1;
                initBoard();
                gameState = STATE_PLAYING;
            }
            repaint();
            return;
        }

        if (gameState == STATE_GAMEOVER) {
            if (keyCode == KeyEvent.VK_LEFT) {
                nameEntryIndex = (nameEntryIndex > 0) ? nameEntryIndex - 1 : 2;
            } else if (keyCode == KeyEvent.VK_RIGHT) {
                nameEntryIndex = (nameEntryIndex < 2) ? nameEntryIndex + 1 : 0;
            } else if (keyCode == KeyEvent.VK_UP) {
                char c = nameEntry[nameEntryIndex];
                c = (c == 'Z') ? 'A' : (char)(c + 1);
                nameEntry[nameEntryIndex] = c;
            } else if (keyCode == KeyEvent.VK_DOWN) {
                char c = nameEntry[nameEntryIndex];
                c = (c == 'A') ? 'Z' : (char)(c - 1);
                nameEntry[nameEntryIndex] = c;
            } else if (keyCode == KeyEvent.VK_ENTER || keyCode == KeyEvent.VK_ACCEPT) {
                String playerName = new String(nameEntry);
                updateHighScoreCustom(playerName, score);
                
                score = 0;
                level = 1;
                nameEntry = new char[]{'P', 'L', 'Y'};
                nameEntryIndex = 0;
                gameState = STATE_MENU;
            }
            repaint();
            return;
        }

        if (gameState == STATE_PLAYING) {
            if (keyCode == KeyEvent.VK_H) {
                findHint();
                repaint();
                return;
            }
            if (keyCode == KeyEvent.VK_M) {
                toggleMusic();
                repaint();
                return;
            }

            if (keyCode == KeyEvent.VK_UP) {
                if (cursorCol == -1) {
                    if (sidebarIndex > 0) sidebarIndex--;
                } else {
                    if (cursorRow > 0) cursorRow--;
                }
            } else if (keyCode == KeyEvent.VK_DOWN) {
                if (cursorCol == -1) {
                    if (sidebarIndex < 1) sidebarIndex++;
                } else {
                    if (cursorRow < GRID_SIZE - 1) cursorRow++;
                }
            } else if (keyCode == KeyEvent.VK_LEFT) {
                if (cursorCol > 0) {
                    cursorCol--;
                } else if (cursorCol == 0) {
                    cursorCol = -1;
                    sidebarIndex = 0;
                    selectedRow = -1;
                    selectedCol = -1;
                }
            } else if (keyCode == KeyEvent.VK_RIGHT) {
                if (cursorCol == -1) {
                    cursorCol = 0;
                } else if (cursorCol < GRID_SIZE - 1) {
                    cursorCol++;
                }
            } else if (keyCode == KeyEvent.VK_ENTER || keyCode == KeyEvent.VK_ACCEPT) {
                if (cursorCol == -1) {
                    if (sidebarIndex == 0) {
                        findHint();
                    } else if (sidebarIndex == 1) {
                        toggleMusic();
                    }
                } else {
                    if (selectedRow == -1 && selectedCol == -1) {
                        selectedRow = cursorRow;
                        selectedCol = cursorCol;
                    } else {
                        int rowDiff = Math.abs(cursorRow - selectedRow);
                        int colDiff = Math.abs(cursorCol - selectedCol);
                        if ((rowDiff == 1 && colDiff == 0) || (rowDiff == 0 && colDiff == 1)) {
                            handleSwap(selectedRow, selectedCol, cursorRow, cursorCol);
                        }
                        selectedRow = -1;
                        selectedCol = -1;
                    }
                }
            }
        }
        repaint();
    }

    public void keyReleased(KeyEvent e) {}
    public void keyTyped(KeyEvent e) {}

    public void update(Graphics g) {
        paint(g);
    }

    public void paint(Graphics g) {
        Dimension d = getSize();
        if (backbuffer == null || backbuffer.getWidth(null) != d.width || backbuffer.getHeight(null) != d.height) {
            try {
                backbuffer = new DVBBufferedImage(d.width, d.height);
            } catch (Exception e) {
                backbuffer = createImage(d.width, d.height);
            }
        }

        if (backbuffer != null) {
            Graphics bufG = backbuffer.getGraphics();
            renderFrame(bufG);
            g.drawImage(backbuffer, 0, 0, this);
            Toolkit.getDefaultToolkit().sync();
        } else {
            renderFrame(g);
            Toolkit.getDefaultToolkit().sync();
        }
    }

    private void renderFrame(Graphics g) {
        if (bgImage != null) {
            g.drawImage(bgImage, 0, 0, getWidth(), getHeight(), this);
        } else {
            g.setColor(new Color(20, 20, 40));
            g.fillRect(0, 0, getWidth(), getHeight());
        }

        if (font == null) {
            font = new Font(null, Font.PLAIN, 22);
            boldFont = new Font(null, Font.BOLD, 32);
            smallFont = new Font(null, Font.PLAIN, 18);
            tinyFont = new Font(null, Font.BOLD, 14);
        }

        // --- STATE 0: MAIN MENU (1080p Centered) ---
        if (gameState == STATE_MENU) {
            g.setColor(new Color(0, 0, 0, 210));
            g.fillRoundRect(545, 240, 830, 600, 30, 30);
            g.setColor(new Color(255, 215, 0, 200));
            g.drawRoundRect(545, 240, 830, 600, 30, 30);

            g.setColor(Color.WHITE);
            g.setFont(boldFont);
            g.drawString("SLOTMATCH!", 790, 330);

            g.setFont(font);
            g.drawString("Select Game Mode:", 860, 410);

            String[] options = {"Start Game", "Start Game with Debug Stats"};
            for (int i = 0; i < options.length; i++) {
                int boxY = 470 + (i * 90);
                g.setColor(i == menuIndex ? new Color(70, 70, 140, 220) : new Color(40, 40, 60, 220));
                g.fillRoundRect(640, boxY, 640, 65, 15, 15);
                g.setColor(i == menuIndex ? Color.YELLOW : Color.WHITE);
                g.drawRect(640, boxY, 640, 65);

                g.drawString((i == menuIndex ? "> " : "   ") + options[i], 680, boxY + 41);
            }

            g.setFont(smallFont);
            g.setColor(Color.LIGHT_GRAY);
            g.drawString("Up/Down: Navigate | Enter: Select", 815, 780);
            return;
        }

        // --- LEFT SIDEBAR HUD & CONTROLS (1080p Scaled) ---
        g.setColor(new Color(0, 0, 0, 190));
        g.fillRoundRect(60, 100, 420, 880, 20, 20);
        g.setColor(new Color(255, 215, 0, 200));
        g.drawRoundRect(60, 100, 420, 880, 20, 20);

        g.setColor(Color.WHITE);
        g.setFont(boldFont);
        g.drawString("SLOTMATCH", 90, 155);

        g.setFont(font);
        g.drawString("Level: " + level, 90, 205);
        g.drawString("Score: " + score, 90, 245);

        g.setFont(smallFont);
        g.drawString("CONTROLS:", 90, 310);

        // Hint Button Box
        g.setColor(cursorCol == -1 && sidebarIndex == 0 ? Color.YELLOW : new Color(40, 40, 70, 220));
        g.fillRoundRect(90, 330, 360, 50, 12, 12);
        g.setColor(cursorCol == -1 && sidebarIndex == 0 ? Color.YELLOW : new Color(255, 215, 0, 180));
        g.drawRoundRect(90, 330, 360, 50, 12, 12);
        g.setColor(Color.WHITE);
        g.drawString("[YELLOW] HINT BUTTON", 115, 362);

        // Music Button Box (Shows active randomized track 0-4 or OFF)
        g.setColor(cursorCol == -1 && sidebarIndex == 1 ? Color.GREEN : new Color(40, 40, 70, 220));
        g.fillRoundRect(90, 395, 360, 50, 12, 12);
        g.setColor(cursorCol == -1 && sidebarIndex == 1 ? Color.GREEN : new Color(255, 215, 0, 180));
        g.drawRoundRect(90, 395, 360, 50, 12, 12);
        g.setColor(Color.WHITE);
        String musicLabel = musicPlaying ? ("[GREEN] TRACK: 0000" + currentTrack) : "[GREEN] MUSIC: OFF";
        g.drawString(musicLabel, 115, 427);

        g.setFont(tinyFont);
        g.setColor(new Color(200, 200, 200));
        g.drawString("Press LEFT from board for menu", 90, 470);

        // Top Scores Section
        g.setFont(font);
        g.setColor(Color.YELLOW);
        g.drawString("TOP SCORES", 90, 520);
        
        g.setFont(smallFont);
        g.setColor(Color.WHITE);
        for (int i = 0; i < 5; i++) {
            String entry = (i + 1) + ". " + hsNames[i] + " - " + hsScores[i];
            g.drawString(entry, 105, 560 + (i * 35));
        }

        // --- GAME BOARD FRAME & TILES (1080p 880x880 Centered) ---
        g.setColor(new Color(0, 0, 0, 190));
        g.fillRoundRect(BOARD_X - 12, BOARD_Y - 12, (GRID_SIZE * TILE_SIZE) + 24, (GRID_SIZE * TILE_SIZE) + 24, 20, 20);
        g.setColor(new Color(255, 215, 0, 220));
        g.drawRoundRect(BOARD_X - 12, BOARD_Y - 12, (GRID_SIZE * TILE_SIZE) + 24, (GRID_SIZE * TILE_SIZE) + 24, 20, 20);

        for (int r = 0; r < GRID_SIZE; r++) {
            for (int c = 0; c < GRID_SIZE; c++) {
                int x = BOARD_X + c * TILE_SIZE;
                int y = BOARD_Y + r * TILE_SIZE;

                if ((r + c) % 2 == 0) {
                    g.setColor(new Color(30, 30, 50, 200));
                } else {
                    g.setColor(new Color(50, 50, 80, 200));
                }
                g.fillRect(x, y, TILE_SIZE, TILE_SIZE);
                g.setColor(new Color(90, 90, 130, 150));
                g.drawRect(x, y, TILE_SIZE, TILE_SIZE);

                int tileType = board[r][c];
                Image img = null;
                if (tileImages != null) {
                    if (tileType >= 1 && tileType <= 7 && tileType - 1 < tileImages.length) {
                        img = tileImages[tileType - 1];
                    } else if (tileType == 8 && 6 < tileImages.length) {
                        img = tileImages[6];
                    } else if (tileType == 9 && 8 < tileImages.length) {
                        img = tileImages[8];
                    }
                }

                if (img != null) {
                    g.drawImage(img, x + 5, y + 5, TILE_SIZE - 10, TILE_SIZE - 10, this);
                }

                if (gameState == STATE_PLAYING && cursorCol >= 0) {
                    if (r == cursorRow && c == cursorCol) {
                        g.setColor(Color.YELLOW);
                        g.drawRect(x, y, TILE_SIZE, TILE_SIZE);
                        g.drawRect(x + 1, y + 1, TILE_SIZE - 2, TILE_SIZE - 2);
                    }

                    if (r == selectedRow && c == selectedCol) {
                        g.setColor(Color.RED);
                        g.drawRect(x, y, TILE_SIZE, TILE_SIZE);
                        g.drawRect(x + 1, y + 1, TILE_SIZE - 2, TILE_SIZE - 2);
                    }
                }
            }
        }

        // Draw Hint Highlights
        if (hintTimer > 0 && hintRow1 != -1) {
            g.setColor(Color.YELLOW);
            g.drawRect(BOARD_X + hintCol1 * TILE_SIZE, BOARD_Y + hintRow1 * TILE_SIZE, TILE_SIZE, TILE_SIZE);
            g.drawRect(BOARD_X + hintCol2 * TILE_SIZE, BOARD_Y + hintRow2 * TILE_SIZE, TILE_SIZE, TILE_SIZE);
        }

        // --- GAME OVER OVERLAY ---
        if (gameState == STATE_GAMEOVER) {
            g.setColor(new Color(0, 0, 0, 230));
            g.fillRoundRect(BOARD_X, BOARD_Y, GRID_SIZE * TILE_SIZE, GRID_SIZE * TILE_SIZE, 20, 20);
            g.setColor(new Color(255, 215, 0, 220));
            g.drawRoundRect(BOARD_X, BOARD_Y, GRID_SIZE * TILE_SIZE, GRID_SIZE * TILE_SIZE, 20, 20);

            g.setColor(Color.RED);
            g.setFont(boldFont);
            g.drawString("GAME OVER!", BOARD_X + 320, BOARD_Y + 160);

            g.setColor(Color.WHITE);
            g.setFont(font);
            g.drawString("No More Moves Left!", BOARD_X + 310, BOARD_Y + 230);
            g.drawString("Enter Your Initials:", BOARD_X + 310, BOARD_Y + 310);

            int boxX = BOARD_X + 310;
            int boxY = BOARD_Y + 350;
            for (int i = 0; i < 3; i++) {
                g.setColor(i == nameEntryIndex ? Color.YELLOW : new Color(100, 100, 140));
                g.drawRect(boxX + (i * 75), boxY, 65, 75);

                g.setColor(Color.WHITE);
                g.setFont(boldFont);
                g.drawString(String.valueOf(nameEntry[i]), boxX + (i * 75) + 18, boxY + 50);
            }

            g.setFont(smallFont);
            g.setColor(Color.LIGHT_GRAY);
            g.drawString("Up/Down: Change Letter", BOARD_X + 220, BOARD_Y + 500);
            g.drawString("Left/Right: Move Position", BOARD_X + 220, BOARD_Y + 535);
            g.drawString("Enter: Submit & Main Menu", BOARD_X + 220, BOARD_Y + 570);
        }

        // --- DEBUG STATS OVERLAY ---
        if (debugMode) {
            Runtime rt = Runtime.getRuntime();
            long totalMem = rt.totalMemory() / (1024 * 1024);
            long freeMem = rt.freeMemory() / (1024 * 1024);
            long usedMem = totalMem - freeMem;
            long maxMem = rt.maxMemory() / (1024 * 1024);

            g.setColor(new Color(0, 0, 0, 210));
            g.fillRoundRect(1450, 850, 420, 180, 15, 15);
            g.setColor(Color.GREEN);
            g.drawRect(1450, 850, 420, 180);

            g.setFont(tinyFont);
            g.drawString("=== DEBUG STATS ===", 1475, 885);
            g.drawString("Memory Used: " + usedMem + " MB", 1475, 920);
            g.drawString("Memory Total: " + totalMem + " MB", 1475, 950);
            g.drawString("Max Heap: " + maxMem + " MB", 1475, 980);
        }
    }
}
