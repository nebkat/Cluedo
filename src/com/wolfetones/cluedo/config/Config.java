package com.wolfetones.cluedo.config;


import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
                "  █         PP███ █████       █████ ███           ░  " +
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
            new Room("Guess Room", true),
            new Room("Kitchen", 9),
            new Room("Ball Room"),
            new Room("Conservatory", 7),
            new Room("Dining Room"),
            new Room("Billiard Room"),
            new Room("Library"),
            new Room("Lounge", 3),
            new Room("Hall"),
            new Room("Study", 1)
        };

        public static final Suspect[] SUSPECTS = {
                new Suspect("Miss Scarlett", "red"),
                new Suspect("Colonel Mustard", "yellow"),
                new Suspect("Mrs. White", "white"),
                new Suspect("Reverend Green", "green"),
                new Suspect("Mrs. Peacock", "blue"),
                new Suspect("Professor Plum", "purple")
        };

        public static final Weapon[] WEAPONS = {
                new Weapon("Candlestick"),
                new Weapon("Dagger"),
                new Weapon("Lead Pipe"),
                new Weapon("Revolver"),
                new Weapon("Rope"),
                new Weapon("Spanner")
        };

        public static class Room {
            public final String name;
            public final boolean guess;
            public final int passage;

            public Room(String n) {
                this(n, false);
            }

            public Room(String n, int p) {
                this(n, false, p);
            }

            public Room(String n, boolean g) {
                this(n, g, -1);
            }

            public Room(String n, boolean g, int p) {
                name = n;
                guess = g;
                passage = p;
            }
        }

        public static class Suspect {
            public final String name;
            public final String color;

            public Suspect(String n, String c) {
                name = n;
                color = c;
            }
        }

        public static class Weapon {
            public final String name;

            public Weapon(String n) {
                name = n;
            }
        }
    }
}
