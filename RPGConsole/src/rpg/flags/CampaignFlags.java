package rpg.flags;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

// Stores the permanent set-only flags of one campaign.
public final class CampaignFlags {

    private final FlagCatalog flagCatalog;
    private final Set<String> setFlagIds;

    public CampaignFlags(FlagCatalog flagCatalog) {
        if (flagCatalog == null) {
            throw new IllegalArgumentException("Flag catalog cannot be null.");
        }

        this.flagCatalog = flagCatalog;
        this.setFlagIds = new LinkedHashSet<>();
    }

    public boolean isSet(String flagId) {
        FlagDefinition flagDefinition = flagCatalog.getById(flagId);

        return setFlagIds.contains(flagDefinition.getId());
    }

    public boolean set(String flagId) {
        FlagDefinition flagDefinition = flagCatalog.getById(flagId);

        return setFlagIds.add(flagDefinition.getId());
    }

    public List<FlagDefinition> getSetFlags() {
        List<FlagDefinition> setFlags = new ArrayList<>();

        for (FlagDefinition flagDefinition : flagCatalog.getAll()) {
            if (setFlagIds.contains(flagDefinition.getId())) {
                setFlags.add(flagDefinition);
            }
        }

        return List.copyOf(setFlags);
    }
}
