package rpg.items;

// Represents the common data shared by all game items.
public abstract class Item {

    private final String id;
    private final String name;
    private final String description;

    protected Item(
            String id,
            String name,
            String description
    ) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
    
    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }

        if (!(object instanceof Item otherItem)) {
            return false;
        }

        return id.equals(otherItem.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}