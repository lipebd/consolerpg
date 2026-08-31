package rpg.battle;

import java.util.Objects;

import rpg.characters.Enemy;
import rpg.characters.GameCharacter;
import rpg.characters.PlayableCharacter;

// Represents one participant inside a battle turn order.
public final class BattleTurnEntry {

	private final int actionNumber;
    private final GameCharacter character;
    private final String displayName;
    private final BattleSide side;

    private BattleTurnEntry(
            GameCharacter character,
            String displayName,
            BattleSide side,
            int actionNumber
    ) {
        this.character = Objects.requireNonNull(
                character,
                "Battle character cannot be null."
        );

        if (displayName == null || displayName.isBlank()) {
            throw new IllegalArgumentException(
                    "Battle display name cannot be empty."
            );
        }
        
        if (actionNumber < 1) {
            throw new IllegalArgumentException(
                    "Battle action number must be positive."
            );
        }

        this.actionNumber = actionNumber;

        this.displayName = displayName;

        this.side = Objects.requireNonNull(
                side,
                "Battle side cannot be null."
        );
    }

    public static BattleTurnEntry fromPartyMember(
            PlayableCharacter member
    ) {
        Objects.requireNonNull(
                member,
                "Party member cannot be null."
        );

        return new BattleTurnEntry(
                member,
                member.getName(),
                BattleSide.PARTY,
                1
        );
    }

    public static BattleTurnEntry fromEnemySlot(
            EnemyBattleSlot slot
    ) {
        Objects.requireNonNull(
                slot,
                "Enemy battle slot cannot be null."
        );

        Enemy enemy = slot.getEnemy();

        return new BattleTurnEntry(
                enemy,
                slot.getDisplayName(),
                BattleSide.ENEMY,
                1
        );
    }
    
    public BattleTurnEntry createActionEntry(
            int actionNumber
    ) {
        return new BattleTurnEntry(
                character,
                displayName,
                side,
                actionNumber
        );
    }
    
    public int getActionNumber() {
        return actionNumber;
    }

    public double getInitiativeScore() {
        return (double) getSpeed() / actionNumber;
    }

    public boolean isAlive() {
        return character.isAlive();
    }

    public int getSpeed() {
        return character.getSpeed();
    }

    public int getLuck() {
        return character.getLuck();
    }

    public GameCharacter getCharacter() {
        return character;
    }

    public String getDisplayName() {
        return displayName;
    }

    public BattleSide getSide() {
        return side;
    }
}