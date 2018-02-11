package com.wolfetones.cluedo.config;


import java.awt.*;
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
            new Room("Kitchen", "card-room-kitchen.png"),
            new Room("Living Room", "card-room-.png"),
            new Room("Conservatory", "card-room-conservatory.png"),
            new Room("Library", "card-room-library.png"),
            new Room("Billiard Room", "card-room-billiard-room.png"),
            new Room("Trophy Room", "card-room-trophy-room.png"),
            new Room("Bedroom", "card-room-bedroom.png"),
            new Room("Hall", "card-room-hall.png"),
            new Room("Studio", "card-room-studio.png")
        };

        public static final Suspect[] SUSPECTS = {
                new Suspect("Miss Scarlett", "card-suspect-miss-scarlett.png", Color.RED),
                new Suspect("Colonel Mustard", "card-suspect-colonel-mustard.png", Color.YELLOW),
                new Suspect("Mrs. White", "card-suspect-mrs-white.png", Color.WHITE),
                new Suspect("Reverend Green", "card-suspect-reverend-green.png", Color.GREEN),
                new Suspect("Mrs. Peacock", "card-suspect-mrs-peacock.png", Color.BLUE),
                new Suspect("Professor Plum", "card-suspect-professor-plum.png", Color.MAGENTA)
        };

        public static final Weapon[] WEAPONS = {
                new Weapon("Candlestick", "card-weapon-candlestick.png"),
                new Weapon("Fire Poker", "card-weapon-fire-poker.png"),
                new Weapon("Garden Shears", "card-weapon-garden-shears.png"),
                new Weapon("Poison", "card-weapon-poison.png"),
                new Weapon("Ice Pick", "card-weapon-ice-pick.png"),
                new Weapon("Revolver", "card-weapon-revolver.png")
        };

        public static class Room extends Card {
            public final boolean guess;

            Room(String n, String i) {
                this(n, i, false);
            }

            Room(String n, String i, boolean g) {
                super(n, i);
                guess = g;
            }
        }

        public static class Suspect extends Card {
            public final Color color;

            Suspect(String n, String i, Color c) {
                super(n, i);
                color = c;
            }
        }

        public static class Weapon extends Card {
            Weapon(String n, String i) {
                super(n, i);
            }
        }

        public abstract static class Card {
            public final String name;
            public final String image;

            Card(String n, String i) {
                name = n;
                image = i;
            }
        }
    }
}
