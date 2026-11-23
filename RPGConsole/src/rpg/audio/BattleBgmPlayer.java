package rpg.audio;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import rpg.battle.BattleType;

// Plays the demonstration battle music through the standard Java audio API.
public final class BattleBgmPlayer {

	private final Path audioDirectory;
	private Clip activeClip;

	public BattleBgmPlayer(Path audioDirectory) {
		this.audioDirectory = Objects.requireNonNull(audioDirectory, "Audio directory cannot be null.");
	}

	public synchronized void play(BattleType battleType) {
		Objects.requireNonNull(battleType, "Battle type cannot be null.");

		stop();

		Path audioPath = audioDirectory.resolve(resolveFileName(battleType));
		Clip clip = null;

		try (AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(audioPath.toFile())) {
			clip = AudioSystem.getClip();
			clip.open(audioInputStream);
			clip.loop(Clip.LOOP_CONTINUOUSLY);
			activeClip = clip;
		} catch (UnsupportedAudioFileException | IOException | LineUnavailableException
				| IllegalArgumentException exception) {
			closeClip(clip);
			System.err.println(
					"Aviso: não foi possível reproduzir a música de batalha: "
							+ audioPath + " (" + exception.getMessage() + ")"
			);
		}
	}

	public synchronized void stop() {
		closeClip(activeClip);
		activeClip = null;
	}

	private static String resolveFileName(BattleType battleType) {
		return switch (battleType) {
		case NORMAL -> "battle_normal.wav";
		case BOSS -> "battle_boss.wav";
		case EPIC -> "battle_epic.wav";
		};
	}

	private static void closeClip(Clip clip) {
		if (clip == null) {
			return;
		}

		clip.stop();
		clip.flush();
		clip.close();
	}
}
