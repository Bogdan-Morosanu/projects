package ai.BitModel;

import pieces.Bishop;
import pieces.Knight;
import pieces.Piece;
import pieces.Queen;
import pieces.Rook;
import controller.AppRegistry;
import controller.Move;
import controller.PromotionMove;
import ai.AiRegistry;
import ai.Evaluator;
import ai.GameState;


/**
 * TODO make program go for quickest chess, otherwise sometimes game does not terminate
 * this just means you teach him the three moves repeated give you draw
 * @author moro
 *
 */

public class BitBoard implements ai.GameState {
	
	private static final boolean DEBUG = false;
	private static final boolean VERBOSE_PRINTING = false;
	
	public final boolean isWhiteMove;
	
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
	
	public final long blackMapShadow;
	public final long whiteMapShadow;
	
	BitBoard(boolean whiteToMove,
			 long bPawns, long wPawns, long bKnights, long wKnights, 
			 long bKing, long wKing, long bBishops, long wBishops,
			 long bQueens, long wQueens, long bRooks, long wRooks) {
		
		isWhiteMove = whiteToMove;
		
		blackPawns= bPawns;
		whitePawns = wPawns;
		
		blackKnights = bKnights;
		whiteKnights = wKnights;
		
		blackKing = bKing;
		whiteKing = wKing;
		
		blackBishops = bBishops;
		whiteBishops = wBishops;
		
		blackQueens = bQueens;
		whiteQueens = wQueens;
		
		blackRooks = bRooks;
		whiteRooks = wRooks;
		
		blackMapShadow = bBishops | bKing | bKnights 
				 | bPawns | bQueens | bRooks;

		whiteMapShadow = wBishops | wKing | wKnights 
				 | wPawns | wQueens | wRooks;
		
	}
	
	
	private Evaluator evaluator;

	
	private double value = -Double.MAX_VALUE;
	private BitBoard[] children;
	
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
		
		BitBoard[] newChildren = new BitBoard[validChildren];
		
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
	public BitBoard[] children() {
		if(children == null) {
			BitMoveEngine engine = AiRegistry.getBitMoveEngine(this);			
			
			//return children = engine.getRookChildren();
			children = mergeBitBoardLists(new BitBoard[][] {
					engine.getBishopChildren(),
					engine.getKnightChildren(),
					engine.getKingChildren(),
					engine.getPawnChildren(),
					engine.getQueenChildren(),
					engine.getRookChildren(),
			});
			
			//if(isInCheck()) {
				if(children.length == 0) {
					
					if(DEBUG) {
						System.out.println("this better be a checkmate : " +
								BitMoveEngine.isBitBoardInCheck(this.sameReversePlayer()));
					}
					//dead
				}
			//}
			
			return children;
			
		} else {
			return children;
		}
	}
	
	public BitBoard sameReversePlayer() {
		return new BitBoard(
				 !isWhiteMove,
				 blackPawns, 
				 whitePawns, 
				 blackKnights, 
				 whiteKnights,						 
				 blackKing, 
				 whiteKing, 
				 blackBishops, 
				 whiteBishops,
				 blackQueens, 
				 whiteQueens, 
				 blackRooks,
				 whiteRooks 
				);
	}
	
	
	@Override 
	public String toString() {
		StringBuilder str = new StringBuilder();
		
		str.append("\nto move : " + (isWhiteMove ? "white\n" : "black\n"));
		
		long whiteMapShadow = this.whiteBishops | this.whiteKing | this.whiteKnights 
				 | this.whitePawns | this.whiteQueens | this.whiteRooks;
		
		long blackMapShadow = this.blackBishops | this.blackKing | this.blackKnights 
				 | this.blackPawns | this.blackQueens | this.blackRooks;
		
		str.append("\nWhite bit map shadow :\n");
		str.append(BitPatterns.formatBitString(whiteMapShadow));
		
		str.append("\nBlack bit map shadow :\n");
		str.append(BitPatterns.formatBitString(blackMapShadow));
		
		if(VERBOSE_PRINTING) {
			str.append("\nWhite pawn bit map : \n");
			str.append(BitPatterns.formatBitString(whitePawns));
			
	
			str.append("\nBlack pawn bit map : \n");
			str.append(BitPatterns.formatBitString(blackPawns));
			
	
			str.append("\nWhite King bit map : \n");
			str.append(BitPatterns.formatBitString(whiteKing));
			
	
			str.append("\nBlack King bit map : \n");
			str.append(BitPatterns.formatBitString(blackKing));
			
	
			str.append("\nWhite Knights bit map : \n");
			str.append(BitPatterns.formatBitString(whiteKnights));
			
	
			str.append("\nBlack Knights bit map : \n");
			str.append(BitPatterns.formatBitString(blackKnights));
			
	
			str.append("\nWhite Bishops bit map : \n");
			str.append(BitPatterns.formatBitString(whiteBishops));
			
	
			str.append("\nBlack Bishops bit map : \n");
			str.append(BitPatterns.formatBitString(blackBishops));
			
	
			str.append("\nWhite Rooks bit map : \n");
			str.append(BitPatterns.formatBitString(whiteRooks));
			
	
			str.append("\nBlack Rooks bit map : \n");
			str.append(BitPatterns.formatBitString(blackRooks));
			
			str.append("\nWhite Queens bit map : \n");
			str.append(BitPatterns.formatBitString(whiteQueens));
			
	
			str.append("\nBlack Queens bit map : \n");
			str.append(BitPatterns.formatBitString(blackQueens));

			str.append("\nfor less details about individual piece bitmaps, change BitBoard static VERBOSE_PRINTING field\n");
		
		} else {
			str.append("\nfor more details about individual piece bitmaps, change BitBoard static VERBOSE_PRINTING field\n");
		}
			
		return str.toString();
	}
	
	public String toStringTerse() {
			StringBuilder str = new StringBuilder();
			
			str.append("\nto move : " + (isWhiteMove ? "white\n" : "black\n"));
			
			long whiteMapShadow = this.whiteBishops | this.whiteKing | this.whiteKnights 
					 | this.whitePawns | this.whiteQueens | this.whiteRooks;
			
			long blackMapShadow = this.blackBishops | this.blackKing | this.blackKnights 
					 | this.blackPawns | this.blackQueens | this.blackRooks;
			
			str.append("\nWhite bit map shadow :\n");
			str.append(BitPatterns.formatBitString(whiteMapShadow));
			
			str.append("\nBlack bit map shadow :\n");
			str.append(BitPatterns.formatBitString(blackMapShadow));
		
			return str.toString();
	}
	
	public String toStringVerbose() {
		StringBuilder str = new StringBuilder();
		
		str.append("\nto move : " + (isWhiteMove ? "white\n" : "black\n"));
		
		long whiteMapShadow = this.whiteBishops | this.whiteKing | this.whiteKnights 
				 | this.whitePawns | this.whiteQueens | this.whiteRooks;
		
		long blackMapShadow = this.blackBishops | this.blackKing | this.blackKnights 
				 | this.blackPawns | this.blackQueens | this.blackRooks;
		
		str.append("\nWhite bit map shadow :\n");
		str.append(BitPatterns.formatBitString(whiteMapShadow));
		
		str.append("\nBlack bit map shadow :\n");
		str.append(BitPatterns.formatBitString(blackMapShadow));
		
		//VERBOSE PRINTING FOLLOWS
			str.append("\nWhite pawn bit map : \n");
			str.append(BitPatterns.formatBitString(whitePawns));
			
	
			str.append("\nBlack pawn bit map : \n");
			str.append(BitPatterns.formatBitString(blackPawns));
			
	
			str.append("\nWhite King bit map : \n");
			str.append(BitPatterns.formatBitString(whiteKing));
			
	
			str.append("\nBlack King bit map : \n");
			str.append(BitPatterns.formatBitString(blackKing));
			
	
			str.append("\nWhite Knights bit map : \n");
			str.append(BitPatterns.formatBitString(whiteKnights));
			
	
			str.append("\nBlack Knights bit map : \n");
			str.append(BitPatterns.formatBitString(blackKnights));
			
	
			str.append("\nWhite Bishops bit map : \n");
			str.append(BitPatterns.formatBitString(whiteBishops));
			
	
			str.append("\nBlack Bishops bit map : \n");
			str.append(BitPatterns.formatBitString(blackBishops));
			
	
			str.append("\nWhite Rooks bit map : \n");
			str.append(BitPatterns.formatBitString(whiteRooks));
			
	
			str.append("\nBlack Rooks bit map : \n");
			str.append(BitPatterns.formatBitString(blackRooks));
			
			str.append("\nWhite Queens bit map : \n");
			str.append(BitPatterns.formatBitString(whiteQueens));
			
	
			str.append("\nBlack Queens bit map : \n");
			str.append(BitPatterns.formatBitString(blackQueens));

			
		return str.toString();
	}

	
	
	@Override 
	public Move getTransitionMove(GameState board) {
		/**
		 * just invalidating LSP -- ClassCastExceptions AHOY!
		 */
		return getTransitionMove((BitBoard)board);
	}
	
	public Move getTransitionMove(BitBoard other) {

		
		if(other.isWhiteMove == this.isWhiteMove) {
			throw new IllegalStateException("can't generate map with same player still to move. Boards Follow " +
						this + other);
			
		}
		
		if(this.isWhiteMove) {
			if(this.whiteMapShadow == other.whiteMapShadow) {
				throw new IllegalStateException("white to move but I can see no difference in white piece positions");
			}
		} else {
			if(this.blackMapShadow == other.blackMapShadow) {
				throw new IllegalStateException("black to move but I can see no difference in black piece positions");				
			}
		}
		
		long difference = (this.isWhiteMove) ? (other.whiteMapShadow ^ this.whiteMapShadow) : (other.blackMapShadow ^ this.blackMapShadow);
		
		long startBit = (this.isWhiteMove) ? (this.whiteMapShadow & difference) : (this.blackMapShadow & difference);		
		long endBit = (this.isWhiteMove) ? (other.whiteMapShadow & difference) : (other.blackMapShadow & difference);
		
		/**
		 * now we calculate start and endpoint destinations in terms understood by the map Model
		 */
		
		int startRow = 0;
		try {
			startRow = BitPatterns.bitPieceRowNum(startBit);
		} catch(IllegalStateException e) {
			System.err.println(e.getMessage());
			System.err.println("offending boards follow\n" + this.toStringVerbose());
			System.err.println(other.toStringVerbose());
			
			Runtime.getRuntime().exit(-1);
		}
		int startCol = BitPatterns.bitPieceColNum(startBit);
		int endRow = BitPatterns.bitPieceRowNum(endBit);
		int endCol = BitPatterns.bitPieceColNum(endBit);
		
		
		/**
		 * we must check for the special case of a pawn promotion taking place.
		 * the following code checks if the player to move has lost
		 * a pawn as a result of his own move. (i.e. he has traded it for some other piece)
		 */
		boolean isPawnPromoted = (this.isWhiteMove) ? (Long.bitCount(this.whitePawns) - Long.bitCount(other.whitePawns) == 1) : 
													  (Long.bitCount(this.blackPawns) - Long.bitCount(other.blackPawns) == 1);
		

		if(!isPawnPromoted) { //just a kosher move... do you normal thing
			
			return new Move(AppRegistry.getConstPosition(startRow, startCol), 
							AppRegistry.getConstPosition(endRow, endCol));
		} else {
			//must figure out the type of piece promoted to
			int rooksAdded = (this.isWhiteMove) ? (Long.bitCount(other.whiteRooks) - Long.bitCount(this.whiteRooks)) : 
				  									  (Long.bitCount(other.blackRooks) - Long.bitCount(this.blackRooks));
			
			int queensAdded = (this.isWhiteMove) ? (Long.bitCount(other.whiteQueens) - Long.bitCount(this.whiteQueens)) : 
													   (Long.bitCount(other.blackQueens) - Long.bitCount(this.blackQueens));
			
			int bishopsAdded = (this.isWhiteMove) ? (Long.bitCount(other.whiteRooks) - Long.bitCount(this.whiteRooks)) : 
				  										(Long.bitCount(other.blackRooks) - Long.bitCount(this.blackRooks));
			

			int knightsAdded = (this.isWhiteMove) ? (Long.bitCount(other.whiteKnights) - Long.bitCount(this.whiteKnights)) : 
				  										(Long.bitCount(other.blackKnights) - Long.bitCount(this.blackKnights));
			
			int justChecking = rooksAdded + queensAdded + bishopsAdded + knightsAdded;
			if( 1 != justChecking ) {
				throw new IllegalStateException(  
						(justChecking > 1) ? 
							"I see you're trying to add more than one piece to the board..." :
							("Since you've promoted a pawn you might as well put some piece on the board... \n" +
								"like, any kind of piece")	
						);
			}
			
			Class< ? extends Piece > promotedTo = null;
			
			if(rooksAdded == 1) {
				promotedTo = Rook.class;
			}
			
			if(knightsAdded == 1) {
				promotedTo = Knight.class;
			}
			
			if(queensAdded == 1) {
				promotedTo = Queen.class;
			}
			
			if(bishopsAdded == 1) {
				promotedTo = Bishop.class;
			}
			
			return new PromotionMove(AppRegistry.getConstPosition(startRow, startCol), 
									 AppRegistry.getConstPosition(endRow, endCol),
									 promotedTo);
		}
	}
	
	
	
	@Override 
	public boolean equals(Object other) {
		BitBoard othBoard = (BitBoard)other;
		
		return  isWhiteMove == othBoard.isWhiteMove &&
				blackPawns == othBoard.blackPawns &&
				whitePawns == othBoard.whitePawns &&		
				
				blackKnights == othBoard.blackKnights &&
				whiteKnights == othBoard.whiteKnights &&
				
				blackKing == othBoard.blackKing &&
				whiteKing == othBoard.whiteKing &&
				
				blackBishops == othBoard.blackBishops &&
				whiteBishops == othBoard.whiteBishops &&
				
				blackQueens == othBoard.blackQueens &&
				whiteQueens == othBoard.whiteQueens &&
				
				blackRooks == othBoard.blackRooks &&
				whiteRooks == othBoard.whiteRooks;
			
	}
	
	@Override
	public int hashCode() {
		long longHash = blackPawns ^ whitePawns ^ blackKnights ^ whiteKnights
						^ blackKing ^ whiteKing ^ blackBishops ^ whiteBishops
						^ blackQueens ^ whiteQueens ^ blackRooks ^ whiteRooks;
	
		if(!isWhiteMove) {
			longHash = ~longHash;
		}
		
		return (int)longHash ^ (int)(longHash >> 32);
	}
	
	
	public static BitBoard[] mergeBitBoardLists(BitBoard[][] bitBoardLists) {
		
		int size = 0;
		
		for(BitBoard[] list : bitBoardLists) {
			size += list.length;
		}
		
		BitBoard[] allMoves = new BitBoard[size];
		
		size = 0;
		for(BitBoard[] list : bitBoardLists) {
			for(BitBoard moveMask : list) {
				allMoves[size++] = moveMask;
			}
		}
		
		return allMoves;
	}
	
	/**
	 * Might refactor this into caching transposition tables
	 * @return
	 */
	@Deprecated
	public static BitBoard getBitBoard(boolean isWhiteMove, long blackPawns, long whitePawns,
			  long blackKnights, long whiteKnights, 
			  long blackKing, long whiteKing,
			  long blackBishops, long whiteBishops,
			  long blackQueens, long whiteQueens,
			  long blackRooks, long whiteRooks) {
		
		/*
		 * Might want to use this method to cache a transposition table 
		 * int hash = staticHash(isWhiteMove,blackPawns,whitePawns,
				blackKnights,whiteKnights,blackKing,whiteKing,
				blackBishops,whiteBishops,blackQueens,whiteQueens,
				blackRooks,whiteRooks
				);
			BitBoard board = cache.get(hash);
		
			if(board == null) {
			
		 */
			
		
		return new BitBoard(isWhiteMove,blackPawns,whitePawns,
				blackKnights,whiteKnights,blackKing,whiteKing,
				blackBishops,whiteBishops,blackQueens,whiteQueens,
				blackRooks,whiteRooks);
			
		
	}
	
	
	public static int staticHash(boolean isWhiteMove, long blackPawns, long whitePawns,
								  long blackKnights, long whiteKnights, 
								  long blackKing, long whiteKing,
								  long blackBishops, long whiteBishops,
								  long blackQueens, long whiteQueens,
								  long blackRooks, long whiteRooks) {
		
		long longHash = blackPawns ^ whitePawns ^ blackKnights ^ whiteKnights
				^ blackKing ^ whiteKing ^ blackBishops ^ whiteBishops
				^ blackQueens ^ whiteQueens ^ blackRooks ^ whiteRooks;

		if(!isWhiteMove) {
			longHash = ~longHash;
		}

		return (int)longHash ^ (int)(longHash >> 32);
	}
	public static void main(String[] args) {
		
		BitPatterns.show(5);
		System.out.println(Long.MAX_VALUE);
	}

}
