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
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Config {

    public static final String TITLE = "Cluedo";
    public static final String AUTHOR =
            "___________.__              __      __      .__   _____        ___________                          \n" +
            "\\__    ___/|  |__   ____   /  \\    /  \\____ |  |_/ ____\\____   \\__    ___/___   ____   ____   ______\n" +
            "  |    |   |  |  \\_/ __ \\  \\   \\/\\/   /  _ \\|  |\\   __\\/ __ \\    |    | /  _ \\ /    \\_/ __ \\ /  ___/\n" +
            "  |    |   |   Y  \\  ___/   \\        (  <_> )  |_|  | \\  ___/    |    |(  <_> )   |  \\  ___/ \\___ \\ \n" +
            "  |____|   |___|  /\\___  >   \\__/\\  / \\____/|____/__|  \\___  >   |____| \\____/|___|  /\\___  >____  >\n" +
            "                \\/     \\/         \\/                       \\/                      \\/     \\/     \\/ ";

    public static Font FONT = null;

    static {
        try {
            FONT = Font.createFont(Font.TRUETYPE_FONT, Config.class.getClassLoader().getResourceAsStream("capone-cg-light.otf"));
        } catch (FontFormatException | IOException e) {
            e.printStackTrace();
        }
    }

    public static class Board {
        public static final int WIDTH = 26;
        public static final int HEIGHT = 27;

        public static final int STRING_WIDTH = WIDTH * 2 + 1;
        public static final int STRING_HEIGHT = HEIGHT * 2 + 1;

        public static final String BOARD_STRING =
                "                                                     " +
                "                                                     " +
                "                    ███       ███                    " +
                "                    █@█       █@█                    " +
                "  ███░░░█░░░███ █████=█░░░█░░░█=█████ █░░░░░█░░░░░█  " +
                "  █1 1 1 1 1PA█ █=====█2 2 2 2█=====█ █3 3 3 3 3 3░  " +
                "  █         PP███=█████       █████=███           ░  " +
                "  ░1 1 1 1 1 1█===█2 2 2 2 2 2 2 2█===█3 3 3 3 3 3░  " +
                "  ░           █===█               █===█           █  " +
                "  ░1 1 1 1 1 1█===█2 2 2 2 2 2 2 2█===█3 3 3 3 3 3░  " +
                "  █           █===█               █===█           ░  " +
                "  ░1 1 1 1 1 1█===█2 2 2 2 2 2 2 2█===█3 3 3 3 3 3░  " +
                "  ░           █===█               █===█▲█     PP███  " +
                "  ░1 1 1 1 1 1█===▲2 2 2 2 2 2 2 2▲=====█3 3 3PC█    " +
                "  ███         █===█               █=====███████████  " +
                "    █1 1 1 1 1█===█2 2 2 2 2 2 2 2█==============@█  " +
                "  █████████▲███===█               █=============███  " +
                "  █===============█2 2 2 2 2 2 2 2█=============█    " +
                "  ███=============███▲█████████▲███===█████████████  " +
                "    █=================================█5 5 5 5 5 5░  " +
                "  ███████████=========================█           █  " +
                "  ░4 4 4 4 4█=========================▲5 5 5 5 5 5░  " +
                "  ░         ███████===███████████=====█           █  " +
                "  ░4 4 4 4 4 4 4 4█===█0 0 0 0 0█=====█5 5 5 5 5 5░  " +
                "  █               █===█         █=====█           █  " +
                "  ░4 4 4 4 4 4 4 4█===█0 0 0 0 0█=====█5 5 5 5 5 5░  " +
                "  ░               █===█         █=====█           █  " +
                "  ░4 4 4 4 4 4 4 4▲===█0 0 0 0 0█=====█5 5 5 5 5 5░  " +
                "  ░               █===█         █=====█████████▲███  " +
                "  ░4 4 4 4 4 4 4 4█===█0 0 0 0 0█===============█    " +
                "  █               █===█         █=====█████▲█████    " +
                "  ░4 4 4 4 4 4 4 4█===█0 0 0 0 0█=====█6 6 6 6 6█    " +
                "  ░               █===█         █===███         █░█  " +
                "  ░4 4 4 4 4 4 4 4█===█0 0 0 0 0█===█6 6 6 6 6 6 6░  " +
                "  █████████████▲███===█         █===█             █  " +
                "    █=================█0 0 0 0 0█===▲6 6 6 6 6 6 6░  " +
                "  ███=================█████▲█████===█             █  " +
                "  █@================================█6 6 6 6 6 6 6░  " +
                "  ███===============█████▲▲▲█████===███         █░█  " +
                "    █===============█8 8 8 8 8 8█=====█6 6 6 6 6█    " +
                "  █████████████▲█===█           █=====█████████████  " +
                "  █DP7 7 7 7 7 7█===█8 8 8 8 8 8█================@█  " +
                "  █PP           █===█           █===============███  " +
                "  ░7 7 7 7 7 7 7█===█8 8 8 8 8 8▲===============█    " +
                "  ░             █===█           █===█▲█████████████  " +
                "  ░7 7 7 7 7 7 7█===█8 8 8 8 8 8█===█9 9 9 9 9 9PB█  " +
                "  █             █===█           █===█           PP█  " +
                "  ░7 7 7 7 7 7 7█===█8 8 8 8 8 8█===█9 9 9 9 9 9 9░  " +
                "  ░             █===█           █===█             █  " +
                "  ░7 7 7 7 7 7 7█===█8 8 8 8 8 8█===█9 9 9 9 9 9 9░  " +
                "  █           ███=███           ███=███           ░  " +
                "  █7 7 7 7 7 7█ █@█ █8 8 8 8 8 8█ █=█ █9 9 9 9 9 9░  " +
                "  █░░░█░█░█░░░█ ███ █████████████ ███ ███░░░█░█░░░█  " +
                "                                                     " +
                "                                                     ";

        public static class Tiles {
            public static final char EMPTY = ' ';
            public static final char WALL = '█';
            public static final char WINDOW = '░';
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

            public static final Map<Character, int[]> PASSAGES = new HashMap<Character, int[]>() {{
                put('A', new int[]{1, 9});
                put('B', new int[]{9, 1});
                put('C', new int[]{3, 7});
                put('D', new int[]{7, 3});
            }};
        }
    }

    public static class Cards {
        public static final Room[] ROOMS = {
            new Room("Guess Room", null, true),
            new Room("Kitchen", "kitchen"),
            new Room("Living Room", "living-room"),
            new Room("Conservatory", "conservatory"),
            new Room("Library", "library"),
            new Room("Billiard Room", "billiard-room"),
            new Room("Trophy Room", "trophy-room"),
            new Room("Bedroom", "bedroom"),
            new Room("Hall", "hall"),
            new Room("Studio", "studio")
        };

        public static final Suspect[] SUSPECTS = {
                new Suspect("Miss Scarlett", "miss-scarlett", Color.RED),
                new Suspect("Colonel Mustard", "colonel-mustard", Color.YELLOW),
                new Suspect("Mrs. White", "mrs-white", Color.WHITE),
                new Suspect("Reverend Green", "reverend-green", Color.GREEN),
                new Suspect("Mrs. Peacock", "mrs-peacock", Color.BLUE),
                new Suspect("Professor Plum", "professor-plum", Color.MAGENTA)
        };

        public static final Weapon[] WEAPONS = {
                new Weapon("Candlestick", "candlestick"),
                new Weapon("Fire Poker", "fire-poker"),
                new Weapon("Garden Shears", "garden-shears"),
                new Weapon("Poison", "poison"),
                new Weapon("Ice Pick", "ice-pick"),
                new Weapon("Revolver", "revolver")
        };

        public static class Room extends Card {
            public final boolean guess;

            Room(String n, String i) {
                this(n, i, false);
            }

            Room(String n, String r, boolean g) {
                super(n, r);
                guess = g;
            }
        }

        public static class Suspect extends Card {
            public final Color color;

            Suspect(String n, String r, Color c) {
                super(n, r);
                color = c;
            }
        }

        public static class Weapon extends Card {
            Weapon(String n, String r) {
                super(n, r);
            }
        }

        public abstract static class Card {
            public final String name;
            public final String resource;

            Card(String n, String r) {
                name = n;
                resource = r;
            }
        }
    }

    private static final int DEFAULT_SCREEN_WIDTH = 1920;
    private static final int DEFAULT_SCREEN_HEIGHT = 1080;

    private static int sScreenWidth = -1;
    private static int sScreenHeight = -1;

    private static void loadScreenSize() {
        if (sScreenWidth == -1 || sScreenHeight == -1) {
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            sScreenWidth = screenSize.width;
            sScreenHeight = screenSize.height;
        }
    }

    public static int screenRelativeSize(int size) {
        loadScreenSize();

        return (int) Math.ceil(size * sScreenWidth / (double) DEFAULT_SCREEN_WIDTH);
    }

    public static int screenWidthPercentage(float percentage) {
        loadScreenSize();

        return (int) (sScreenWidth * percentage);
    }

    public static int screenHeightPercentage(float percentage) {
        loadScreenSize();

        return (int) (sScreenHeight * percentage);
    }
}
