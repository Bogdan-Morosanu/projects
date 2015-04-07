package ai;

import java.io.IOException;

import ai.AlphaBeta.TestTree;
import ai.BitModel.BitBoard;
import ai.BitModel.BitTranslator;
import board.Map;
import controller.AppRegistry;
import pieces.Piece;
import pieces.PieceList;

public class TestController {

	private static PieceList piecesOnBoard;
	
	/**
	 * ---------- Initialize internal Piece reference -----
	 */
	static {
		
		Map map = AppRegistry.getMap();
		
		piecesOnBoard = map.toPieceList();
		
	}
	
	private static void refresh() {
		Map map = AppRegistry.getMap();
		
		piecesOnBoard = map.toPieceList();
		
		BitTranslator.wipe();
		
		for(Piece p : piecesOnBoard) {
			BitTranslator.registerPiece(p);
		}
	}
	
	public static void main(String[] args) throws IOException {
		
		long start = System.currentTimeMillis();
		BitBoard board = BitTranslator.getStateOfMap();
		
		TestTree gameTree = new TestTree(board);
		
		gameTree.expandToLevel(board, 5);
		
		BitBoard current = board;
		/**
		 * BitMoveEngine engine = new BitMoveEngine(board);
		 
		for(int i = 0; i < 5; i++) {

			System.out.println(engine);
			current = current.children()[0];
			engine.swapBoard(current);
		}
			*/
		
		System.out.println("game tree expansion done in " + (System.currentTimeMillis() - start) + " miliseconds");
		System.out.println("game tree comprised of " + gameTree.stateNum + " states ");
		
		System.in.read(new byte[100]);
		//BitBoard lastBoard = gameTree.generateDFSCheck();
		
		
		//System.out.println("game tree check search done in " + (System.currentTimeMillis() - start) + " miliseconds");
		//System.out.println("game tree height is now  " + gameTree.height + " states ");
		
	}

}














