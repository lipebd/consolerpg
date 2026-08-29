package rpg.items;

import rpg.characters.GameCharacter;

// Represents a consumable item that restores HP.
public class HealingItem extends Item {

    private final int healingAmount;

    public HealingItem(
            String id,
            String name,
            String description,
            int healingAmount
    ) {
        super(id, name, description);
        this.healingAmount = healingAmount;
    }

    public int use(GameCharacter target) {
        return target.recoverHp(healingAmount);
    }

    public int getHealingAmount() {
        return healingAmount;
    }
}