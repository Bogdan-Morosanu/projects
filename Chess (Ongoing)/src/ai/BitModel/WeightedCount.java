package ai.BitModel;

import ai.Evaluator;
import ai.GameState;

/**
 * assumes White is maximizer and Black is minimizer
 * @author moro
 *
 */
public class WeightedCount implements Evaluator {
	
	private static final double QUEEN_VAL = 9.0;
	private static final double ROOK_VAL = 5.0;
	private static final double BISHOP_VAL = 3.0;
	private static final double KNIGHT_VAL = 3.0;
	private static final double PAWN_VAL = 1.0;
	private static final double CENTER_VAL = 1.0;
	private static final double SMALL_CENTER_VAL = 0.5;
	
	private static final long CENTER_FILTER = 
			
			(BitPatterns.verticalBars[2] | BitPatterns.verticalBars[3] |
			BitPatterns.verticalBars[4] | BitPatterns.verticalBars[5]) 
			
			&
			
			(BitPatterns.horizontalBars[2] | BitPatterns.horizontalBars[3] |
			BitPatterns.horizontalBars[4] | BitPatterns.horizontalBars[5]);
	
	private static final long SMALL_CENTER_FILTER = 
			
			(BitPatterns.verticalBars[3] | BitPatterns.verticalBars[4]) 
			& 
			(BitPatterns.horizontalBars[3] | BitPatterns.horizontalBars[4]);
	
	@Override
	public double eval(GameState g) {
		if(!(g instanceof BitBoard)) {
			/*
			 * nothing much, just invalidating the LSP
			 */
			throw new IllegalStateException("can't evaluate something that is not a BitBoard");
		}
		
		BitBoard board = (BitBoard) g;
		
		int queenDelta = Long.bitCount(board.blackQueens) - Long.bitCount(board.whiteQueens);
		int rookDelta = Long.bitCount(board.blackRooks) - Long.bitCount(board.whiteRooks);
		int bishopDelta = Long.bitCount(board.blackBishops) - Long.bitCount(board.whiteBishops);
		int knightDelta = Long.bitCount(board.blackKnights) - Long.bitCount(board.whiteKnights);
		int pawnDelta = Long.bitCount(board.blackPawns) - Long.bitCount(board.whitePawns);
		
		double pieceVal =
			   queenDelta * QUEEN_VAL + rookDelta * ROOK_VAL +
			   bishopDelta * BISHOP_VAL + knightDelta * KNIGHT_VAL +
			   pawnDelta * PAWN_VAL;
		
		
		/*
		 * ---- Now we must check for controlling the center ----
		 */
		
		
		
		long blackCenter = board.blackMapShadow & CENTER_FILTER;
		long smallBlackCenter = board.blackMapShadow & SMALL_CENTER_FILTER;
		
		long whiteCenter = board.whiteMapShadow & CENTER_FILTER;
		long smallWhiteCenter = board.whiteMapShadow & SMALL_CENTER_FILTER;
		
		double centerVal = 
				(Long.bitCount(blackCenter) - Long.bitCount(whiteCenter)) * CENTER_VAL +
				(Long.bitCount(smallBlackCenter) - Long.bitCount(smallWhiteCenter)) * SMALL_CENTER_VAL;
		
		return pieceVal + centerVal;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
