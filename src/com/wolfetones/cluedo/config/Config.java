/*
 * Copyright (c) 2018
 *
 * The Wolfe Tones
 * -------------------
 * Nebojsa Cvetkovic - 16376551
 * Hugh Ormond - 16312941
 *
 * This file is a part of Cluedo
 *
 * Cluedo is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Cluedo is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Cluedo.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.wolfetones.cluedo.config;


import java.awt.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Game configuration class.
 */
public class Config {
    public static final String TITLE = "Cluedo";
    public static final String AUTHOR =
            "___________.__              __      __      .__   _____        ___________                          \n" +
            "\\__    ___/|  |__   ____   /  \\    /  \\____ |  |_/ ____\\____   \\__    ___/___   ____   ____   ______\n" +
            "  |    |   |  |  \\_/ __ \\  \\   \\/\\/   /  _ \\|  |\\   __\\/ __ \\    |    | /  _ \\ /    \\_/ __ \\ /  ___/\n" +
            "  |    |   |   Y  \\  ___/   \\        (  <_> )  |_|  | \\  ___/    |    |(  <_> )   |  \\  ___/ \\___ \\ \n" +
            "  |____|   |___|  /\\___  >   \\__/\\  / \\____/|____/__|  \\___  >   |____| \\____/|___|  /\\___  >____  >\n" +
            "                \\/     \\/         \\/                       \\/                      \\/     \\/     \\/ ";

    public static Font FONT;

    static {
        try {
            FONT = Font.createFont(Font.TRUETYPE_FONT, Config.class.getClassLoader().getResourceAsStream("capone-cg-light.otf"));
        } catch (FontFormatException | IOException e) {
            new Exception("Error loading font", e).printStackTrace();

            // Load default font instead
            FONT = new Font(Font.SANS_SERIF, Font.PLAIN, Config.screenRelativeSize(12));
        }
    }

    public static class Board {
        public static final int WIDTH = 28;
        public static final int HEIGHT = 28;

        public static final int STRING_WIDTH = WIDTH * 2 + 1;
        public static final int STRING_HEIGHT = HEIGHT * 2 + 1;

        public static final String BOARD_STRING =
                "                                                         " +
                "                                                         " +
                "                      ███       ███                      " +
                "                      █@█       █@█                      " +
                "    ███▒▒▒█▒▒▒███ █████=█▒▒▒█▒▒▒█=█████ █▒▒▒▒▒█▒▒▒▒▒█    " +
                "    █1 1 1 1 1PA█ █=====█2 2 2 2█=====█ █3 3 3 3 3 3▒    " +
                "    █         PP███=█████       █████=███           ▒    " +
                "    ▒1 1 1 1 1 1█===█2 2 2 2 2 2 2 2█===█3 3 3 3 3 3▒    " +
                "    ▒           █===█               █===█           █    " +
                "    ▒1 1 1 1 1 1█===█2 2 2 2 2 2 2 2█===█3 3 3 3 3 3▒    " +
                "    █           █===█               █===█           ▒    " +
                "    ▒1 1 1 1 1 1█===█2 2 2 2 2 2 2 2█===█3 3 3 3 3 3▒    " +
                "    ▒           █===█               █===█▲█     PP███    " +
                "    ▒1 1 1 1 1 1█===▲2 2 2 2 2 2 2 2▲=====█3 3 3PC█      " +
                "    ███         █===█               █=====███████████    " +
                "      █1 1 1 1 1█===█2 2 2 2 2 2 2 2█==============@█    " +
                "    █████████▲███===█               █=============███    " +
                "    █===============█2 2 2 2 2 2 2 2█=============█      " +
                "    ███=============███▲█████████▲███===█████████████    " +
                "      █=================================█5 5 5 5 5 5▒    " +
                "    ███████████=========================█           █    " +
                "    ▒4 4 4 4 4█=========================▲5 5 5 5 5 5▒    " +
                "    ▒         ███████===▓▓▓▓▓▓▓▓▓▓▓=====█           █    " +
                "    ▒4 4 4 4 4 4 4 4█===▓0 0 0 0 0▓=====█5 5 5 5 5 5▒    " +
                "    █               █===▓         ▓=====█           █    " +
                "    ▒4 4 4 4 4 4 4 4█===▓0 0 0 0 0▓=====█5 5 5 5 5 5▒    " +
                "    ▒               █===▓         ▓=====█           █    " +
                "    ▒4 4 4 4 4 4 4 4▲===▓0 0 0 0 0▓=====█5 5 5 5 5 5▒    " +
                "    ▒               █===▓         ▓=====█████████▲███    " +
                "    ▒4 4 4 4 4 4 4 4█===▓0 0 0 0 0▓===============█      " +
                "    █               █===▓         ▓=====█████▲█████      " +
                "    ▒4 4 4 4 4 4 4 4█===▓0 0 0 0 0▓=====█6 6 6 6 6█      " +
                "    ▒               █===▓         ▓===███         █▒█    " +
                "    ▒4 4 4 4 4 4 4 4█===▓0 0 0 0 0▓===█6 6 6 6 6 6 6▒    " +
                "    █████████████▲███===▓         ▓===█             █    " +
                "      █=================▓0 0 0 0 0▓===▲6 6 6 6 6 6 6▒    " +
                "    ███=================▓▓▓▓▓▲▓▓▓▓▓===█             █    " +
                "    █@================================█6 6 6 6 6 6 6▒    " +
                "    ███===============█████▲▲▲█████===███         █▒█    " +
                "      █===============█8 8 8 8 8 8█=====█6 6 6 6 6█      " +
                "    █████████████▲█===█           █=====█████████████    " +
                "    █DP7 7 7 7 7 7█===█8 8 8 8 8 8█================@█    " +
                "    █PP           █===█           █===============███    " +
                "    ▒7 7 7 7 7 7 7█===█8 8 8 8 8 8▲===============█      " +
                "    ▒             █===█           █===█▲█████████████    " +
                "    ▒7 7 7 7 7 7 7█===█8 8 8 8 8 8█===█9 9 9 9 9 9PB█    " +
                "    █             █===█           █===█           PP█    " +
                "    ▒7 7 7 7 7 7 7█===█8 8 8 8 8 8█===█9 9 9 9 9 9 9▒    " +
                "    ▒             █===█           █===█             █    " +
                "    ▒7 7 7 7 7 7 7█===█8 8 8 8 8 8█===█9 9 9 9 9 9 9▒    " +
                "    █           ███=███           ███=███           ▒    " +
                "    █7 7 7 7 7 7█ █@█ █8 8 8 8 8 8█ █=█ █9 9 9 9 9 9▒    " +
                "    █▒▒▒█▒█▒█▒▒▒█ ███ █████████████ ███ ███▒▒▒█▒█▒▒▒█    " +
                "                                                         " +
                "                                                         " +
                "                                                         " +
                "                                                         ";

        public static class Tiles {
            public static final char EMPTY = ' ';
            public static final char WALL = '█';
            public static final char GUESS_WALL = '▓';
            public static final char WINDOW = '▒';
            public static final char DOOR = '▲';
            public static final char START = '@';
            public static final char CORRIDOR = '=';
            public static final char PASSAGE = 'P';

            public static final int[] STARTS = new int[] {
                    2,
                    3,
                    4,
                    1,
                    5,
                    0
            };

            public static final Map<Character, int[]> PASSAGES = new HashMap<>() {{
                put('A', new int[]{1, 9});
                put('B', new int[]{9, 1});
                put('C', new int[]{3, 7});
                put('D', new int[]{7, 3});
            }};
        }
    }

    public static class Cards {
        public static final Room[] ROOMS = {
            new Room("Guess Room", null, null, true),
            new Room("Kitchen", new String[] {"kitchen"}, "kitchen"),
            new Room("Living Room", new String[] {"livingroom", "living"}, "living-room"),
            new Room("Conservatory", new String[] {"conservatory"}, "conservatory"),
            new Room("Library", new String[] {"library"}, "library"),
            new Room("Billiard Room", new String[] {"billiardroom", "billiard"}, "billiard-room"),
            new Room("Trophy Room", new String[] {"trophyroom", "trophy"}, "trophy-room"),
            new Room("Bedroom", new String[] {"bedroom"}, "bedroom"),
            new Room("Hall", new String[] {"hall"}, "hall"),
            new Room("Studio", new String[] {"studio"}, "studio")
        };

        public static final Suspect[] SUSPECTS = {
                new Suspect("Miss Scarlett", new String[] {"missscarlett", "scarlett", "red"}, "miss-scarlett", Color.RED),
                new Suspect("Colonel Mustard", new String[] {"colonelmustard", "mustard", "yellow"}, "colonel-mustard", Color.YELLOW),
                new Suspect("Mrs. White", new String[] {"mrswhite", "white"}, "mrs-white", Color.WHITE),
                new Suspect("Reverend Green", new String[] {"reverendgreen", "green"}, "reverend-green", Color.GREEN),
                new Suspect("Mrs. Peacock", new String[] {"mrspeacock", "peacock", "blue"}, "mrs-peacock", Color.BLUE),
                new Suspect("Professor Plum", new String[] {"professorplum", "plum", "purple"}, "professor-plum", Color.MAGENTA)
        };

        public static final Weapon[] WEAPONS = {
                new Weapon("Candlestick", new String[] {"candlestick"}, "candlestick"),
                new Weapon("Fire Poker", new String[] {"firepoker"}, "fire-poker"),
                new Weapon("Garden Shears", new String[] {"gardenshears", "shears"}, "garden-shears"),
                new Weapon("Poison", new String[] {"poison"}, "poison"),
                new Weapon("Ice Pick", new String[] {"ice-pick"}, "ice-pick"),
                new Weapon("Revolver", new String[] {"revolver", "pistol"}, "revolver")
        };

        public static class Room extends Card {
            public final boolean guess;

            Room(String n, String[] s, String i) {
                this(n, s, i, false);
            }

            Room(String n, String[] s, String r, boolean g) {
                super(n, s, r);
                guess = g;
            }
        }

        public static class Suspect extends Card {
            public final Color color;

            Suspect(String n, String[] s, String r, Color c) {
                super(n, s, r);
                color = c;
            }
        }

        public static class Weapon extends Card {
            Weapon(String n, String[] s, String r) {
                super(n, s, r);
            }
        }

        public abstract static class Card {
            public final String name;
            public final String[] searchNames;
            public final String resource;

            Card(String n, String[] s, String r) {
                name = n;
                searchNames = s;
                resource = r;
            }
        }
    }

    private static final int DEFAULT_SCREEN_HEIGHT = 1080;
    private static Dimension sScreenSize = Toolkit.getDefaultToolkit().getScreenSize();
    private static double sScreenRelativeSize = (double) sScreenSize.height / DEFAULT_SCREEN_HEIGHT;

    public static int screenRelativeSize(int size) {
        return (int) Math.ceil(size * sScreenRelativeSize);
    }

    public static double screenRelativeSize(double size) {
        return size * sScreenRelativeSize;
    }

    public static int screenWidthPercentage(float percentage) {
        return (int) (sScreenSize.width * percentage);
    }

    public static int screenHeightPercentage(float percentage) {
        return (int) (sScreenSize.height * percentage);
    }
}
