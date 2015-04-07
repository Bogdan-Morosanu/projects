package ai.BitModel;

import controller.Move;
import ai.Evaluator;
import ai.GameState;
import ai.StateGenerator;

@Deprecated
public class CachedLengthBitBoard implements GameState {

	/* 
	 * ----- Piece-wise Packed Bitmaps ----- 
	 */
	public final long blackPawns;
	public final long whitePawns;
	
	public final long blackKnights;
	public final long whiteKnights;
	
	public final long blackKing;
	public final long whiteKing;
	
	public final long blackBishops;
	public final long whiteBishops;
	
	public final long blackQueens;
	public final long whiteQueens;
	
	public final long blackRooks;
	public final long whiteRooks;
	
	/* 
	 * ----- Counts associated with each Bitmap ----- 
	 */
	
	public final int blackPawnCount;
	public final int whitePawnCount;
	
	public final int blackKnightCount;
	public final int whiteKnightCount;

	public final int blackBishopCount;
	public final int whiteBishopCount;
	
	public final int blackQueenCount;
	public final int whiteQueenCount;
	
	public final int blackRookCount;
	public final int whiteRookCount;
	
	CachedLengthBitBoard(long bPawns, long wPawns, long bKnights, long wKnights, 
			 long bKing, long wKing, long bBishops, long wBishops,
			 long bQueens, long wQueens, long bRooks, long wRooks) {
		
		blackPawns= bPawns;
		blackPawnCount = Long.bitCount(bPawns);
		
		whitePawns = wPawns;
		whitePawnCount = Long.bitCount(wPawns);
		
		blackKnights = bKnights;
		blackKnightCount = Long.bitCount(bKnights);
		
		
		whiteKnights = wKnights;
		whiteKnightCount = Long.bitCount(wKnights);
		
		blackKing = bKing;
		whiteKing = wKing;
		
		blackBishops = bBishops;
		blackBishopCount = Long.bitCount(bBishops);
		
		whiteBishops = wBishops;
		whiteBishopCount = Long.bitCount(wBishops);
		
		blackQueens = bQueens;
		blackQueenCount = Long.bitCount(bQueens);
		
		whiteQueens = wQueens;
		whiteQueenCount = Long.bitCount(wQueens);
		
		blackRooks = bRooks;
		blackRookCount = Long.bitCount(bRooks);
		
		whiteRooks = wRooks;
		whiteRookCount = Long.bitCount(wRooks);
		
	}
	
	
	private Evaluator evaluator;
	private StateGenerator generator;
	
	private double value = -Double.MAX_VALUE;
	private CachedLengthBitBoard[] children;
	
	@Override 
	public void dumpChildren() {
		children = null;
	}
	
	@Override 
	public void pruneChildren(boolean[] filter) {
		
		//compute new buffer size
		int validChildren = 0;
		for(int i = 0; i < filter.length; i++) {
			if(filter[i]) {
				validChildren++;
			}
		}
		
		CachedLengthBitBoard[] newChildren = new CachedLengthBitBoard[validChildren];
		
		for(int i = 0, j = 0; i < filter.length; i++) {
			if(filter[i]) {
				newChildren[j++] = children[i];
			}
		}
		
		children = newChildren;
		
	}
	
	
	@Override
	public double eval() {
		if(value == -Double.MAX_VALUE) {
			return value = evaluator.eval(this);
		} else {
			return value;
		}
	}

	@Override
	public CachedLengthBitBoard[] children() {
		if(children == null) {
			return children = (CachedLengthBitBoard[])generator.generateChildren(this);
		} else {
			return children;
		}
	}

	@Override
	public Move getTransitionMove(GameState other) {
		// TODO Auto-generated method stub
		return null;
	}
	

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}


}
