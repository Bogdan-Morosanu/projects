package ai;

import controller.Move;

/**
 * 
 * @author moro
 *
 */
public interface GameState {
	public double eval();
	public GameState[] children();
	public void pruneChildren(boolean[] filter);
	public void dumpChildren();
	public Move getTransitionMove(GameState other);
}
