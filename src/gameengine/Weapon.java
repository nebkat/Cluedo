package gameengine;

public class Weapon {

    private final String name;
    private Coordinates position;

    Weapon (String name, Room room) {
        this.name = name;
        position = room.addItem();
    }

    public String getName() {
        return name;
    }

    public Coordinates getPosition() {
        return position;
    }

    void setRoom(Room room) {
        position = room.addItem();
    }

    public boolean hasName (String name) {
        return this.name.toLowerCase().trim().equals(name.toLowerCase().trim());
    }
}
