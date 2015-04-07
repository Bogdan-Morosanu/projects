package ai;

/**
 * Interface for StateGenerator Class.
 * @author moro
 *
 */
public interface StateGenerator {
	public GameState[] generateChildren(GameState parent);
}
