package ai;

/**
 * Interface for static eval function used by alpha-beta
 * @author moro
 *
 */
public interface Evaluator {
	public double eval(GameState g);
}
