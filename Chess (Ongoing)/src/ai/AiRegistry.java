package ai;

import ai.BitModel.BitBoard;
import ai.BitModel.BitMoveEngine;

@Deprecated
public class AiRegistry {

	private InternalExecutor executor;
	
	/**
	 * TODO : make a per-thread copy of the generator object
	 */
	private static BitMoveEngine engine;

	public static BitMoveEngine getBitMoveEngine(BitBoard board) {
		if(engine == null) {
			return engine = new BitMoveEngine(board);
		} else {
			engine.swapBoard(board);
			return engine;
		}
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
