package ai.BitModel;

import ai.GameState;
import ai.StateGenerator;

@Deprecated
public class BitChecker implements StateGenerator {

	private static final long rowLen = 1 << 8;
	
	
	@Override
	public GameState[] generateChildren(GameState parent) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @param args
	 */
	
	private boolean bPvalid(long start, long end) {
		return false;
	}
	
	private boolean wPvalid(long start, long end) {
		return false;
	}
	
	private boolean KnightValid(long start, long end) {
		
		return false;
	}
	
	/**
	 * assumes only one bit has changed since last valid position
	 * does not check for castles!
	 * @param start
	 * @param end
	 * @return
	 */
	private boolean simpleKingValid(long start, long end) {
		long diff = start ^ end;
		long startPiece = diff ^ end;
		long endPiece = diff ^ start;
		
		int colDiff = getColDiff(startPiece,endPiece);
		int rowDiff = getRowDiff(startPiece,endPiece);
		
		return colDiff <= 1 && rowDiff <= 1;
	}
	

	/**
	 * assumes only one bit changed since last valid position
	 * @param start
	 * @param end
	 * @return
	 */
	
	private boolean BishopValid(long start, long end) {
		
		long diff = start ^ end;
		long startPiece = diff ^ end;
		long endPiece = diff ^ start;
		
		int colDiff = getColDiff(startPiece,endPiece);
		int rowDiff = getRowDiff(startPiece,endPiece);
		
		return colDiff == rowDiff;
	}
	
	private boolean QueenValid(long start, long end) {
		long diff = start ^ end;
		long startPiece = diff ^ end;
		long endPiece = diff ^ start;
		
		int colDiff = getColDiff(startPiece,endPiece);
		int rowDiff = getRowDiff(startPiece,endPiece);
		
		return colDiff == rowDiff ||
			   (colDiff == 0 && rowDiff > 0) ||
			   (colDiff > 0 && rowDiff == 0);
	}

	
	/**
	 * assumes only one bit changed since last valid position
	 * @param start
	 * @param end
	 * @return
	 */
	private boolean RookValid(long start, long end) {
		long diff = start ^ end;
		long startPiece = diff ^ end;
		long endPiece = diff ^ start;

		int colDiff = getColDiff(startPiece,endPiece);
		int rowDiff = getRowDiff(startPiece,endPiece);
				
		return (colDiff == 0 && rowDiff > 0) || (colDiff > 0 && rowDiff == 0);
	}
	
	/**
	 * assumes each bitmap has only one toggled bit
	 * @param onePiece
	 * @param otherPiece
	 * @return guaranteed positive distance along row axis
	 */
	private static int getRowDiff(long onePiece, long otherPiece) {
		int i = 0;
		
		long max = (onePiece > otherPiece) ? onePiece : otherPiece;
		long min = (onePiece == max) ? otherPiece : onePiece;
		long counter = max / min; //scale so that min is positioned @ [0][0];
		
		while(counter > 1) {
			counter /= rowLen;
			i++; 
			//start subtracting rows and count them
		}
		
		return i;
	}
	
	/**
	 * assumes each bitmap has only one toggled bit
	 * @param onePiece
	 * @param otherPiece
	 * @return guaranteed positive distance along column axis
	 */
	private static int getColDiff(long onePiece, long otherPiece) {
		long max = (onePiece > otherPiece) ? onePiece : otherPiece;
		long min = (onePiece == max) ? otherPiece : onePiece;
		long scaled = max / min; //scale so that min is positioned @ [0][0];
		
		return (int)(scaled %rowLen - min % rowLen); 
		//min % rowLen is start 
		//scaled % rowLEn is end col
	}
	
	/**
	 * implicit assumption : only one bit has been shifted since last valid configuration!
	 * @param allBitBoards
	 * @return
	 */
	private boolean noClashes(long[] allBitBoards) {
		long seed = 0;
		
		for(int i = 1; i < allBitBoards.length; i++) {
			for(int j = i + 1; j < allBitBoards.length; j++) {
				seed += allBitBoards[i] & allBitBoards[j]; //non-zero if pieces overlap
			}
		}
		
		return seed == 0;
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
