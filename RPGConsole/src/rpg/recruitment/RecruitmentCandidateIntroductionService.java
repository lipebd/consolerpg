package rpg.recruitment;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import rpg.characters.CharacterNameSelector;
import rpg.characters.Party;
import rpg.characters.PlayableCharacter;

// Creates persistent candidate identity at the first narrative introduction.
public final class RecruitmentCandidateIntroductionService {

    private final CharacterNameSelector characterNameSelector;
    private final RecruitmentCandidateStateCatalog candidateStateCatalog;
    private final Party party;

    public RecruitmentCandidateIntroductionService(
            CharacterNameSelector characterNameSelector,
            RecruitmentCandidateStateCatalog candidateStateCatalog,
            Party party) {
        if (characterNameSelector == null) {
            throw new IllegalArgumentException("Character name selector cannot be null.");
        }

        if (candidateStateCatalog == null) {
            throw new IllegalArgumentException(
                    "Recruitment candidate state catalog cannot be null.");
        }

        if (party == null) {
            throw new IllegalArgumentException("Party cannot be null.");
        }

        this.characterNameSelector = characterNameSelector;
        this.candidateStateCatalog = candidateStateCatalog;
        this.party = party;
    }

    public RecruitmentCandidateState introduce(String candidateId) {
        Optional<RecruitmentCandidateState> existingState =
                candidateStateCatalog.findByCandidateId(candidateId);

        if (existingState.isPresent()) {
            return existingState.get();
        }

        List<String> reservedNames = collectReservedNames();

        String generatedName = characterNameSelector.selectExcluding(reservedNames);

        RecruitmentCandidateState candidateState =
                new RecruitmentCandidateState(candidateId, generatedName);

        candidateStateCatalog.register(candidateState);

        return candidateState;
    }

    private List<String> collectReservedNames() {
        List<String> reservedNames = new ArrayList<>();

        for (PlayableCharacter member : party.getMembers()) {
            reservedNames.add(member.getName());
        }

        for (RecruitmentCandidateState candidateState : candidateStateCatalog.getAll()) {
            reservedNames.add(candidateState.getName());
        }

        return List.copyOf(reservedNames);
    }
}
