package ai.BitModel;



/**
 * Engine that uses the BitPatterns class to generate real chess moves in BitMap notation
 * should be able to run concurrently
 * @author moro
 */

public class BitMoveEngine {
	
	private static final boolean DEBUG = false;
	
	private static final long INVALID_MOVE_MASK = -1; //all bits set
	
	private long blackMapShadow = 0;
	private long whiteMapShadow = 0;
	
	private long attackShadow = 0;
	
	/**
	 * kings and pawns can also attack squares they cannot move to
	 */
	private long[] kingAndPawnAttacks = null; 
	
	
	private long[] kingMoves;
	private long[] knightMoves;
	private long[] pawnMoves;
	private long[] queenMoves;
	private long[] rookMoves;
	private long[] bishopMoves;
	
	private BitBoard board;
	
	public BitMoveEngine(BitBoard board) {
		this.board = board;
		
		blackMapShadow = board.blackBishops | board.blackKing | board.blackKnights 
						 | board.blackPawns | board.blackQueens | board.blackRooks;
		
		whiteMapShadow = board.whiteBishops | board.whiteKing | board.whiteKnights 
						 | board.whitePawns | board.whiteQueens | board.whiteRooks;
		
		
		
		kingAndPawnAttacks = mergeMoveLists(new long[][] {
				getKingAttackMasks(),
				getPawnAttackMasks(),
		});
		
		
		
		long[] tempAttacks = mergeMoveLists(new long[][]{
				kingAndPawnAttacks,
				getQueenMoveMasks(),
				getBishopMoveMasks(),
				getKnightMoveMasks(),
				getRookMoveMasks(),
		});
		
		for(long attack : tempAttacks) {
			attackShadow |= attack;
		}
	
	}
	
	
	/**
	 * swaps internal board and sets up the move engine to use the newBoard
	 * @param newBoard
	 */
	public void swapBoard(BitBoard newBoard) {
		
		//force recalculation of moves 
		kingMoves = null;
		knightMoves = null;
		pawnMoves = null;
		rookMoves = null;
		queenMoves = null;
		bishopMoves = null;
				
		kingAndPawnAttacks = null;
		
		this.board = newBoard;
		
		blackMapShadow = newBoard.blackBishops | newBoard.blackKing | newBoard.blackKnights 
						 | newBoard.blackPawns | newBoard.blackQueens | newBoard.blackRooks;
		
		whiteMapShadow = newBoard.whiteBishops | newBoard.whiteKing | newBoard.whiteKnights 
						 | newBoard.whitePawns | newBoard.whiteQueens | newBoard.whiteRooks;
		
		
		
		kingAndPawnAttacks = mergeMoveLists(new long[][] {
				getKingAttackMasks(),
				getPawnAttackMasks(),
		});
		

		
		long[] tempAttacks = mergeMoveLists(new long[][]{
				kingAndPawnAttacks,
				getQueenMoveMasks(),
				getBishopMoveMasks(),
				getKnightMoveMasks(),
				getRookMoveMasks(),
		});
		
		for(long attack : tempAttacks) {
			attackShadow |= attack;
		}
		
		
	}
	
	
	/**
	 * Generates heuristic move templates for Rook moves.		
	 * @author moro
	 *
	 */
	private static class RookEngine {
		static long getTemplate(long bitPiece) {
			int rowNum = BitPatterns.bitPieceRowNum(bitPiece);
			int colNum = BitPatterns.bitPieceColNum(bitPiece);
			
			return BitPatterns.verticalBars[colNum] | 
				   BitPatterns.horizontalBars[rowNum];
		}
		
		
		
	}
	
	
	/**
	 * Generates heuristic move templates for Bishop moves.
	 * @author moro
	 *
	 */
	private static class BishopEngine {
		static long getTemplate(long bitPiece) {
			int mainDiagIndex = BitPatterns.mainDiagIndexOf(bitPiece);
			int secondDiagIndex = BitPatterns.secondDiagIndexOf(bitPiece);
			
			return BitPatterns.mainDiagonals[mainDiagIndex] |
				   BitPatterns.secondDiagonals[secondDiagIndex];
		}
		
		
	}
	
	
	/**
	 * Generates heuristic move templates for Queen moves.
	 * @author moro
	 *
	 */
	private static class QueenEngine {
		static long getTemplate(long bitPiece) {
			int rowNum = BitPatterns.bitPieceRowNum(bitPiece);
			int colNum = BitPatterns.bitPieceColNum(bitPiece);
			int mainDiagIndex = BitPatterns.mainDiagIndexOf(rowNum, colNum);
			int secondDiagIndex = BitPatterns.secondDiagIndexOf(rowNum, colNum);
			
			return BitPatterns.horizontalBars[rowNum] |
				   BitPatterns.verticalBars[colNum] |
				   BitPatterns.mainDiagonals[mainDiagIndex] |
				   BitPatterns.secondDiagonals[secondDiagIndex];
		}
	}
	
	
	/**
	 * Generates heuristic move template for Knight moves.
	 * @author moro
	 *
	 */
	private static class KnightEngine {
		static long getTemplate(long bitPiece) {
			
			//we shit the pattern to the bit piece, so bit piece will be arg number 2
			int rowDiff = BitPatterns.rowDiffBetweenBitMaps(BitPatterns.pieceAt33, bitPiece);
			int colDiff = BitPatterns.colDiffBetweenBitMaps(BitPatterns.pieceAt33, bitPiece);
		
			return BitPatterns.shiftBy(BitPatterns.horseAt33Template,rowDiff,colDiff);
		}
	}
	
	
	/**
	 * Generates Heuristic move templates for Kings.
	 * Also implements the method that checks for check status in BitMaps
	 * @author moro
	 *
	 */
	private static class KingEngine {
		
		static long getTemplate(long bitPiece) {

			//we shit the pattern to the bit piece, so bit piece will be arg number 2
			int rowDiff = BitPatterns.rowDiffBetweenBitMaps(BitPatterns.pieceAt33, bitPiece);
			int colDiff = BitPatterns.colDiffBetweenBitMaps(BitPatterns.pieceAt33, bitPiece);
			
			return BitPatterns.shiftBy(BitPatterns.kingAt33Template,rowDiff,colDiff);
		}
		
		/**
		 * checks that the king of the player who last made a move is not in check
		 * that means it checks for the player who is not to move (i.e. !board.isWhiteMove )
		 * @param board

		 * @return
		 */
		static boolean isInCheck(BitBoard board) {
			
			boolean whiteMovedLast = !board.isWhiteMove;
			
			//basically all that could stand between our king and check from another piece
			long boardShadow = board.blackMapShadow | board.whiteMapShadow 
					^ ((whiteMovedLast) ? board.blackKing : board.whiteKing);
									
			
			if(whiteMovedLast) {
				long kingPiece = board.whiteKing;
				
				
				//get bits that might contain queens to attack
				long queenHeuristics =  board.blackQueens
										& QueenEngine.getTemplate(kingPiece);
				
				long[] queensUnpacked = unPackBitMap(queenHeuristics);
				
				for(long qAttacker : queensUnpacked) {
					if(kingHasLineOfSight(boardShadow,kingPiece,qAttacker)) {
						return true;
					}
				}
				
				long bishopHeuristics = board.blackBishops
										& BishopEngine.getTemplate(kingPiece);
				
				long[] bishopsUnpacked = unPackBitMap(bishopHeuristics);
				
				for(long bAttacker : bishopsUnpacked) {
					if(kingHasLineOfSight(boardShadow,kingPiece,bAttacker)) {
						return true;
					}
				}
				
				long rookHeuristics = board.blackRooks 
										& RookEngine.getTemplate(kingPiece);
				
				long[] rooksUnpacked = unPackBitMap(rookHeuristics);
				
				for(long rAttacker : rooksUnpacked) {
					if(kingHasLineOfSight(boardShadow,kingPiece,rAttacker)) {
						return true;
					}
				}
				
				long knightHeuristics = board.blackKnights	
										& KnightEngine.getTemplate(kingPiece);
				
				long[] knightsUnpacked = unPackBitMap(knightHeuristics);
				
				for(long nAttacker : knightsUnpacked) {
					if(kingHasLineOfSight(boardShadow,kingPiece,nAttacker)) {
						return true;
					}
				}
				
				int rowNum = BitPatterns.bitPieceRowNum(kingPiece);
				int colNum = BitPatterns.bitPieceColNum(kingPiece);
				
				long pawnHeuristics;
				
				pawnHeuristics = (1L << ((rowNum + 1) * 8 + colNum - 1)) + //upper left
								 (1L << ((rowNum + 1) * 8 + colNum + 1)); //upper right
				
				
				
				//get from board
				pawnHeuristics = pawnHeuristics & board.blackPawns;
				
				long[] pawnsUnpacked = unPackBitMap(pawnHeuristics);
				for(long pAttacker : pawnsUnpacked) {
					if(kingHasLineOfSight(boardShadow,kingPiece,pAttacker)) {
						return true;
					}
				}
				
				//king-to-king proximity check is done on last rows of this method
				
			} else {
				long kingPiece = board.blackKing;
				
				//get bits that might contain queens to attack
				long queenHeuristics =  board.whiteQueens
										& QueenEngine.getTemplate(kingPiece);
				
				long[] queensUnpacked = unPackBitMap(queenHeuristics);
				
				for(long qAttacker : queensUnpacked) {
					if(kingHasLineOfSight(boardShadow,kingPiece,qAttacker)) {
						return true;
					}
				}
				
				long bishopHeuristics = board.whiteBishops
										& BishopEngine.getTemplate(kingPiece);
				
				long[] bishopsUnpacked = unPackBitMap(bishopHeuristics);
				
				for(long bAttacker : bishopsUnpacked) {
					if(kingHasLineOfSight(boardShadow,kingPiece,bAttacker)) {
						return true;
					}
				}
				
				long rookHeuristics = board.whiteRooks
										& RookEngine.getTemplate(kingPiece);
				
				long[] rooksUnpacked = unPackBitMap(rookHeuristics);
				
				for(long rAttacker : rooksUnpacked) {
					if(kingHasLineOfSight(boardShadow,kingPiece,rAttacker)) {
						return true;
					}
				}
				
				long knightHeuristics = board.whiteKnights
										& KnightEngine.getTemplate(kingPiece);
				
				long[] knightsUnpacked = unPackBitMap(knightHeuristics);
				
				for(long nAttacker : knightsUnpacked) {
					if(kingHasLineOfSight(boardShadow,kingPiece,nAttacker)) {
						return true;
					}
				}
				
				int rowNum = BitPatterns.bitPieceRowNum(kingPiece);
				int colNum = BitPatterns.bitPieceColNum(kingPiece);
				
				long pawnHeuristics;
				

				pawnHeuristics = (1L << ((rowNum - 1) * 8 + colNum - 1)) + //lower left
						 		 (1L << ((rowNum - 1) * 8 + colNum + 1)); //lower right
				
				
				
				//get from board
				pawnHeuristics = pawnHeuristics & board.whitePawns;
				
				long[] pawnsUnpacked = unPackBitMap(pawnHeuristics);
				for(long pAttacker : pawnsUnpacked) {
					if(kingHasLineOfSight(boardShadow,kingPiece,pAttacker)) {
						return true;
					}
				}		
			
				
			}
			
			
			//now checking for king proximity
			int rowDiff = BitPatterns.rowDiffBetweenBitMaps(board.blackKing, board.whiteKing);
			int colDiff = BitPatterns.colDiffBetweenBitMaps(board.blackKing, board.whiteKing);
			if( Math.abs(rowDiff) < 2 &&
				Math.abs(colDiff) < 2) {
				return true;
			}
				
			
			
			return false;
		}
		
		private static boolean kingHasLineOfSight(long boardMask, long king, long attacker) {
			int posOne = BitPatterns.bitPieceToBitPos(king);
			int posTwo = BitPatterns.bitPieceToBitPos(attacker);
			
			long lineBetween = BitPatterns.linesBetween[posOne][posTwo];
			/*
			 * now that we have our line between pieces we must mask it so that
			 * any intersection between the line of sight set bits and pieces on the
			 * board will alter the line of sight, and then compare the line on sight
			 * with the initial line. if the line is the same, it means there was no
			 * bit set in between the two pieces and we can return true.
			 * 
			 * this works because our line does not include its end-point pieces,
			 * so they themselves do not alter it.
			 * 
			 */
			
			return lineBetween == (lineBetween & ~boardMask);
		}
		
	}
	
	/**
	 * Generates heuristic move template for White Pawns
	 * @author moro
	 *
	 */
	private static class WPawnEngine {
		
		/**
		 * since our bitMove has two bits set start and end point,
		 * we must check for end point only, and that must be on last row.
		 * we assume obviously, that this is a valid pawn move
		 */
		static final long promotionLane = BitPatterns.horizontalBars[7];
		
		/**
		 * checks if white pawn move results in promotion.
		 * Precondition : valid pawn move passed in.
		 * @param bitMove
		 * @return
		 */
		static boolean readyForPromotion(long bitMove) {
			return (promotionLane & bitMove) != 0;
		}
		
		static long getMovesTemplate(long bitPiece) {

			//we shit the pattern to the bit piece, so bit piece will be arg number 2
			int rowDiff = BitPatterns.rowDiffBetweenBitMaps(BitPatterns.pieceAt33, bitPiece);
			int colDiff = BitPatterns.colDiffBetweenBitMaps(BitPatterns.pieceAt33, bitPiece);
			
			long usualMove = BitPatterns.shiftBy(BitPatterns.wPawnAt33MoveTemplate
					,rowDiff,colDiff);
			
			/**
			 * now we test for opening two square move. 
			 * since our difference is against the [3][3] then a row diff of -2
			 * yields a final row of 3 - 2 == 1, the first row for a white pawn
			 */
			if(rowDiff != -2) {
				return usualMove;
			} else {
			
				int rowNum = 3 + rowDiff;
				int colNum = 3 + colDiff;
			
				long twoSquareMove = BitPatterns.verticalBars[colNum] & 
								 BitPatterns.horizontalBars[rowNum + 2];
			
				return usualMove | twoSquareMove;
			}
		}
		
		static long getCaptureTemplate(long bitPiece) {

			//we shit the pattern to the bit piece, so bit piece will be arg number 2
			int rowDiff = BitPatterns.rowDiffBetweenBitMaps(BitPatterns.pieceAt33, bitPiece);
			int colDiff = BitPatterns.colDiffBetweenBitMaps(BitPatterns.pieceAt33, bitPiece);
			
			return BitPatterns.shiftBy(BitPatterns.wPawnAt33CaptureTemplate
											,rowDiff,colDiff);
		}

	}
	
	/**
	 * Generates heuristic template for Black Pawns
	 * @author moro
	 *
	 */
	private static class BPawnEngine {
		/**
		 * since our bitMove has two bits set start and end point,
		 * we must check for end point only, and that must be on last row.
		 * we assume obviously, that this is a valid pawn move
		 */
		static final long promotionLane = BitPatterns.horizontalBars[0];
		
		/**
		 * checks if black pawn move results in promotion.
		 * Precondition : valid pawn move passed in.
		 * @param bitMove
		 * @return
		 */
		static boolean readyForPromotion(long bitMove) {
			return (promotionLane & bitMove) != 0;
		}
		
		static long getMovesTemplate(long bitPiece) {

			//we shit the pattern to the bit piece, so bit piece will be arg number 2
			int rowDiff = BitPatterns.rowDiffBetweenBitMaps(BitPatterns.pieceAt33, bitPiece);
			int colDiff = BitPatterns.colDiffBetweenBitMaps(BitPatterns.pieceAt33, bitPiece);
			
			long usualMove = BitPatterns.shiftBy(BitPatterns.bPawnAt33MoveTemplate
									,rowDiff,colDiff);
			/**
			 * now we test for opening two square move. 
			 * since our difference is against the [3][3] then a row diff of 3
			 * yields a final row of 3 + 3 == 6, the first row for a black pawn
			 */
			if(rowDiff != 3) {
				return usualMove;
			} else {
				
				int rowNum = 3 + rowDiff;
				int colNum = 3 + colDiff;
				
				long twoSquareMove = BitPatterns.verticalBars[colNum] & 
									 BitPatterns.horizontalBars[rowNum - 2];
				
				return usualMove | twoSquareMove;
			}
		}
		
		static long getCaptureTemplate(long bitPiece) {

			//we shit the pattern to the bit piece, so bit piece will be arg number 2
			int rowDiff = BitPatterns.rowDiffBetweenBitMaps(BitPatterns.pieceAt33, bitPiece);
			int colDiff = BitPatterns.colDiffBetweenBitMaps(BitPatterns.pieceAt33, bitPiece);
			
			return BitPatterns.shiftBy(BitPatterns.bPawnAt33CaptureTemplate
											,rowDiff,colDiff);
		}
	}
	
	private long maskWithMapShadow(long moveMap, boolean forWhite) {
		if(forWhite) {
			return moveMap & (~whiteMapShadow);
		} else {
			return moveMap & (~blackMapShadow);
		}
	
	}
	
	/**
	 * returns move mask array for a single bishop
	 * @param bishop
	 * @param forWhite
	 * @return
	 */
	private long[] getThisBishopMoveMasks(long bishop) {
		long heuristic = BishopEngine.getTemplate(bishop);
		
		heuristic = maskWithMapShadow(heuristic,board.isWhiteMove);
		
		long heuristicMoves[] = unPackBitMap(heuristic);
		
		for(int i = 0; i < heuristicMoves.length; i++) {
			if(!hasLineOfSight(bishop,heuristicMoves[i])) {
				heuristicMoves[i] = INVALID_MOVE_MASK; //will be filtered later by getBishopMoveMasks method
			} else {
				heuristicMoves[i] += bishop; 
				//our move map must always have two bits set
				//start and end square of the move
			}
		}
		
		return heuristicMoves;
	}
	
	/**
	 * returns move masks for all bishops of specified color on board.
	 * a move mask is a bit pattern such that 
	 * oldBishopMap ^ moveMask == newBishopMask.
	 * this means it has bits set for start and end position
	 * (oldStart = 1) ^ (bitMask = 1) is thus set to 0
	 * (oldEnd = 0) ^ (bitMask = 1) is thus set to 1
	 * if this detects a collision, 
	 * we should also xor adversary map shadow, since
	 * it might have failed because of some piece to capture was there.
	 * 
	 * @param forWhite
	 * @return
	 */
	public long[] getBishopMoveMasks() {
		if(bishopMoves != null) {
			return bishopMoves;
		}
		
		long [] bishops;
		
		if(board.isWhiteMove) {

			bishops = unPackBitMap(board.whiteBishops);

		} else {

			bishops = unPackBitMap(board.blackBishops);
		}
		
		long[][] heuristics = new long[bishops.length][];
		
		for(int i = 0; i < bishops.length; i++) {
			heuristics[i] = getThisBishopMoveMasks(bishops[i]);
		}
		
		return bishopMoves = filterInvalidMoveMasks(heuristics);
	}
	
	/**
	 * does not currently check for check, but rather every method uses the 
	 * filterInCheck method @see filterInCheck to filter BitBoards in check
	 * @param forWhite
	 * @return
	 */
	
	public long[] getKingMoveMasks() {
		if(kingMoves != null) {
			return kingMoves;
		}
			
		long kingPiece;
		
		if(board.isWhiteMove) {
			kingPiece = board.whiteKing;
		} else {
			kingPiece = board.blackKing;
		}
		
		long heuristic = KingEngine.getTemplate(kingPiece);
		
		//mask with own pieces -- you cannot capture them
		heuristic = maskWithMapShadow(heuristic,board.isWhiteMove);
			
		long[] moves = unPackBitMap(heuristic);
		for(int i = 0; i < moves.length; i++) {
			moves[i] += kingPiece; //add departure information
		}
		
		return kingMoves = moves;
	}
	
	public long[] getKingAttackMasks() {
		long kingPiece;
		
		if(board.isWhiteMove) {
			kingPiece = board.whiteKing;
		} else {
			kingPiece = board.blackKing;
		}
		
		long heuristic = KingEngine.getTemplate(kingPiece);
		
		
		long[] moves = unPackBitMap(heuristic);
		for(int i = 0; i < moves.length; i++) {
			moves[i] += kingPiece; //add departure information
		}
		
		return moves;
	}
	
	private long[] getThisKnightMoveMasks(long knight) {
	
		long heuristic = KnightEngine.getTemplate(knight);
		
		heuristic = maskWithMapShadow(heuristic,board.isWhiteMove);
		
		long[] moves = unPackBitMap(heuristic);
		for(int i = 0; i < moves.length; i++) {
			moves[i] += knight; //record departure information
		}
		
		return moves;
	}
	
	public long[] getKnightMoveMasks() {
		if(knightMoves != null) {
			return knightMoves;
		}
		
		long[] knights;
		
		if(board.isWhiteMove) {
			knights = unPackBitMap(board.whiteKnights);
		} else {
			knights = unPackBitMap(board.blackKnights);
		}
		
	
		long[][] moves = new long[knights.length][]; 
		for(int i = 0; i < knights.length; i++) {
			moves[i] = getThisKnightMoveMasks(knights[i]);
		}
		
		
		return knightMoves = filterInvalidMoveMasks(moves);
	}
	
	private long[] getThisPawnMoveMasks(long pawnPiece) {
		
		long movesForwardMap;
		long captureMoves;
		
		if(board.isWhiteMove) {
			movesForwardMap = WPawnEngine.getMovesTemplate(pawnPiece);
			captureMoves = WPawnEngine.getCaptureTemplate(pawnPiece);
		
		} else {
			movesForwardMap = BPawnEngine.getMovesTemplate(pawnPiece);
			captureMoves = BPawnEngine.getCaptureTemplate(pawnPiece);

		}
		
		/**
		 * check for visibility of two square opening move
		 * TODO recheck code and decide if necessary to refactor this.
		 */
		long[] moveHeuristics;
		if(Long.bitCount(movesForwardMap) > 1) {
			moveHeuristics = unPackBitMap(movesForwardMap);
			movesForwardMap = 0;
			for(long heuristic : moveHeuristics) {
				if(hasLineOfSight(heuristic,pawnPiece)) {
					movesForwardMap |= heuristic;
				}
			}
				

		}

		
		
		movesForwardMap = (movesForwardMap & (~whiteMapShadow)) & (~blackMapShadow);
		
		if(board.isWhiteMove) {
			captureMoves = captureMoves & blackMapShadow;
		} else {
			captureMoves = captureMoves & whiteMapShadow;
		}
		


			/*
			 * our final moves are those that are either captures
			 * or moves forward, and included in the initial heuristics
			 */
		long[] heuristics =  unPackBitMap(captureMoves ^ movesForwardMap);
		
		for(int i = 0; i < heuristics.length; i++) {
			heuristics[i] += pawnPiece; //mark with information about departure position
		}
		
		
		return heuristics;
	}
	
	public long[] getPawnMoveMasks() {
		if(pawnMoves != null) {
			return pawnMoves;
		}
		
		long[] pawns;
		
		if(board.isWhiteMove) {
			pawns = unPackBitMap(board.whitePawns);
		} else {
			pawns = unPackBitMap(board.blackPawns);
		}
		
	
		long[][] moves = new long[pawns.length][]; 
		for(int i = 0; i < pawns.length; i++) {
			moves[i] = getThisPawnMoveMasks(pawns[i]);
		}
		
		
		return pawnMoves = filterInvalidMoveMasks(moves);
	}
	
	private long[] getThisPawnAttackMasks(long pawnPiece) {
		long captureMoves;
		
		if(board.isWhiteMove) {
			captureMoves = WPawnEngine.getCaptureTemplate(pawnPiece);
		} else {
			captureMoves = BPawnEngine.getCaptureTemplate(pawnPiece);
		}

		return unPackBitMap(captureMoves);
	}
	
	public long[] getPawnAttackMasks() {
		long[] pawns;
		
		if(board.isWhiteMove) {
			pawns = unPackBitMap(board.whitePawns);
		} else {
			pawns = unPackBitMap(board.blackPawns);
		}
		
	
		long[][] moves = new long[pawns.length][]; 
		for(int i = 0; i < pawns.length; i++) {
			moves[i] = getThisPawnAttackMasks(pawns[i]);
		}
		
		
		return filterInvalidMoveMasks(moves);
	}
	
	
	private long[] getThisQueenMoveMasks(long queenPiece) {
		
		long heuristic = QueenEngine.getTemplate(queenPiece);
		heuristic = maskWithMapShadow(heuristic,board.isWhiteMove);
		
		long[] heuristicMoves = unPackBitMap(heuristic);
		
		for(int i = 0; i < heuristicMoves.length; i++) {
			if(!hasLineOfSight(queenPiece,heuristicMoves[i])) {
				heuristicMoves[i] = INVALID_MOVE_MASK; //will be filtered later by getQueenMoveMasks method
			} else {
				heuristicMoves[i] += queenPiece; 
				//our move map must always have two bits set
				//start and end square of the move
			}
		}
		
		return heuristicMoves;
	}
	
	public long[] getQueenMoveMasks() {
		if(queenMoves != null) {
			return queenMoves;
		}
		
		long [] queens;
		
		if(board.isWhiteMove) {
			queens = unPackBitMap(board.whiteQueens);

		} else {
			queens = unPackBitMap(board.blackQueens);
		}
		
		long[][] heuristics = new long[queens.length][];
		
		for(int i = 0; i < queens.length; i++) {
			heuristics[i] = getThisQueenMoveMasks(queens[i]);
		}
		
		return queenMoves = filterInvalidMoveMasks(heuristics);
	}
	
	
	private long[] getThisRookMoveMasks(long rookPiece) {
		long heuristic = RookEngine.getTemplate(rookPiece);
		heuristic = maskWithMapShadow(heuristic,board.isWhiteMove);
		
		long[] heuristicMoves = unPackBitMap(heuristic);
		
		for(int i = 0; i < heuristicMoves.length; i++) {
			if(!hasLineOfSight(rookPiece,heuristicMoves[i])) {
				heuristicMoves[i] = INVALID_MOVE_MASK; //will be filtered later by getQueenMoveMasks method
			} else {
				heuristicMoves[i] += rookPiece; 
				//our move map must always have two bits set
				//start and end square of the move
			}
		}
		
		return heuristicMoves;			
		
	}
	
	
	public long[] getRookMoveMasks() {
		if(rookMoves != null) {
			return rookMoves;
		}
		
		long [] rooks;
		
		if(board.isWhiteMove) {
			rooks = unPackBitMap(board.whiteRooks);

		} else {
			rooks = unPackBitMap(board.blackRooks);
		}
		
		long[][] heuristics = new long[rooks.length][];
		
		for(int i = 0; i < rooks.length; i++) {
			heuristics[i] = getThisRookMoveMasks(rooks[i]);
		}
		
		return rookMoves = filterInvalidMoveMasks(heuristics);
	}
	
	
	public boolean hasLineOfSight(long pieceOne, long pieceTwo) {
		int posOne = BitPatterns.bitPieceToBitPos(pieceOne);
		int posTwo = BitPatterns.bitPieceToBitPos(pieceTwo);
		
		long lineBetween = BitPatterns.linesBetween[posOne][posTwo];
		
		/*
		 * now that we have our line between pieces we must mask it so that
		 * any intersection between the line of sight set bits and pieces on the
		 * board will alter the line of sight, and then compare the line on sight
		 * with the initial line. if the line is the same, it means there was no
		 * bit set in between the two pieces and we can return true.
		 * 
		 * this works because our line does not include its end-point pieces,
		 * so they themselves do not alter it.
		 * 
		 * technically we've already masked with the color of the piece we are
		 * asking for but we may need this method for different purposes later on.
		 * no need to introduce new dependencies
		 */
		
		
		long maskedLine = (lineBetween & (~whiteMapShadow)) & (~blackMapShadow); 
		
		return maskedLine == lineBetween;
	}
	
	
	@Override
	public String toString() {
		StringBuilder strBuild = new StringBuilder("Move Generator details : ");
		
		strBuild.append("white map shadow follows \n" + BitPatterns.formatBitString(whiteMapShadow) + "\n");

		strBuild.append("black map shadow follows \n" + BitPatterns.formatBitString(blackMapShadow) + "\n");
		
		strBuild.append(board.isWhiteMove ? "white " : "black ");
		strBuild.append("control zone follows \n" + BitPatterns.formatBitString(attackShadow) + "\n");
		
		return strBuild.toString();
	}
	
	
	public BitBoard[] getBishopChildren() {

		long[] bishopMasks = getBishopMoveMasks();
		BitBoard[]  bishopBoards = new BitBoard[bishopMasks.length];
		
		
		if(board.isWhiteMove) {
		
			for(int i = 0; i < bishopBoards.length; i++) {
				long captureMask = ~bishopMasks[i];
			
			
				bishopBoards[i] = new BitBoard(
						!board.isWhiteMove,   //toggle player to move
						 board.blackPawns & (captureMask), 
						 board.whitePawns, 
						 board.blackKnights & (captureMask), 
						 board.whiteKnights, 
						 board.blackKing, 
						 board.whiteKing, 
						 board.blackBishops & (captureMask), 
						 board.whiteBishops ^ bishopMasks[i],
						 board.blackQueens & (captureMask), 
						 board.whiteQueens, 
						 board.blackRooks & (captureMask),
						 board.whiteRooks
						);
				} 
		} else {
			for(int i = 0; i < bishopBoards.length; i++) {
				long captureMask = ~bishopMasks[i];
				
				bishopBoards[i] = new BitBoard(
						!board.isWhiteMove,   //toggle player to move
						 board.blackPawns, 
						 board.whitePawns & (captureMask), 
						 board.blackKnights, 
						 board.whiteKnights & (captureMask), 
						 board.blackKing, 
						 board.whiteKing, 
						 board.blackBishops ^ bishopMasks[i], 
						 board.whiteBishops & (captureMask),
						 board.blackQueens, 
						 board.whiteQueens & (captureMask), 
						 board.blackRooks,
						 board.whiteRooks & (captureMask)
						);
			}
		}
		
		//pass board and MoveEngine
		return filterInCheck(bishopBoards);
	}
	
	public BitBoard[] getRookChildren() {
		long[] rookMasks = getRookMoveMasks();
		BitBoard[]  rookBoards = new BitBoard[rookMasks.length];
		
		
		if(board.isWhiteMove) {
		
			for(int i = 0; i < rookBoards.length; i++) {
				long captureMask = ~rookMasks[i];
			
			
				rookBoards[i] = new BitBoard(
						!board.isWhiteMove,   //toggle player to move
						 board.blackPawns & (captureMask), 
						 board.whitePawns, 
						 board.blackKnights & (captureMask), 
						 board.whiteKnights, 
						 board.blackKing, 
						 board.whiteKing, 
						 board.blackBishops & (captureMask), 
						 board.whiteBishops,
						 board.blackQueens & (captureMask), 
						 board.whiteQueens, 
						 board.blackRooks & (captureMask),
						 board.whiteRooks ^ rookMasks[i]
						);
				} 
		} else {
			for(int i = 0; i < rookBoards.length; i++) {
				long captureMask = ~rookMasks[i];
				
				rookBoards[i] = new BitBoard(
						!board.isWhiteMove,   //toggle player to move
						 board.blackPawns, 
						 board.whitePawns & (captureMask), 
						 board.blackKnights, 
						 board.whiteKnights & (captureMask), 
						 board.blackKing, 
						 board.whiteKing, 
						 board.blackBishops, 
						 board.whiteBishops & (captureMask),
						 board.blackQueens, 
						 board.whiteQueens & (captureMask), 
						 board.blackRooks  ^ rookMasks[i],
						 board.whiteRooks & (captureMask)
						);
			}
		}
		
		//pass board and MoveEngine
		return filterInCheck(rookBoards);
	}

	public BitBoard[] getPawnChildren() {
		long[] pawnMasks = getPawnMoveMasks();
		BitBoard[]  pawnBoards = new BitBoard[pawnMasks.length];
		
		
		if(board.isWhiteMove) {
			for(int i = 0; i < pawnMasks.length; i++) {
				if(WPawnEngine.readyForPromotion(pawnMasks[i])) {
					/** 
					 * since our pawn is ready for promotion, we must include a new list 
					 * of promotion moves in our pawnBoards it will have 4 elements since 
					 * there are 4 valid pieces into which a pawn can be promoted. we shall put 
					 * one in our designated place on the board, and the rest will be stored
					 * in a temporary array
					 */
					long captureMask = ~pawnMasks[i];
					
					//used to place piece on board
					long destinationMask = pawnMasks[i] & BitPatterns.horizontalBars[7];
					long pawnToDropMask = pawnMasks[i] & BitPatterns.horizontalBars[6];
					
					BitBoard[] promotionList = new BitBoard[3];
					
					//put queen in her designated place
					pawnBoards[i] = new BitBoard(
							!board.isWhiteMove,   //toggle player to move
							 board.blackPawns, 	 //can't capture pawns on last row, of course 
							 board.whitePawns ^ pawnToDropMask, 
							 board.blackKnights & (captureMask), 
							 board.whiteKnights, 
							 board.blackKing, 
							 board.whiteKing, 
							 board.blackBishops & (captureMask), 
							 board.whiteBishops,
							 board.blackQueens & (captureMask), 
							 board.whiteQueens ^ destinationMask, 
							 board.blackRooks & (captureMask),
							 board.whiteRooks 
							);
					
					//put rook in first place temporary array
					promotionList[0] = new BitBoard(
							!board.isWhiteMove,   //toggle player to move
							 board.blackPawns, 	 //can't capture pawns on last row, of course 
							 board.whitePawns ^ pawnToDropMask, 
							 board.blackKnights & (captureMask), 
							 board.whiteKnights, 
							 board.blackKing, 
							 board.whiteKing, 
							 board.blackBishops & (captureMask), 
							 board.whiteBishops,
							 board.blackQueens & (captureMask), 
							 board.whiteQueens, 
							 board.blackRooks & (captureMask),
							 board.whiteRooks ^ destinationMask
							);
					
					//put knight in second
					promotionList[1] = new BitBoard(
							!board.isWhiteMove,   //toggle player to move
							 board.blackPawns, 	 //can't capture pawns on last row, of course 
							 board.whitePawns ^ pawnToDropMask, 
							 board.blackKnights & (captureMask), 
							 board.whiteKnights ^ destinationMask, 
							 board.blackKing, 
							 board.whiteKing, 
							 board.blackBishops & (captureMask), 
							 board.whiteBishops,
							 board.blackQueens & (captureMask), 
							 board.whiteQueens, 
							 board.blackRooks & (captureMask),
							 board.whiteRooks
							);
					
					//put bishop in third
					promotionList[2] = new BitBoard(
							!board.isWhiteMove,   //toggle player to move
							 board.blackPawns, 	 //can't capture pawns on last row, of course 
							 board.whitePawns ^ pawnToDropMask, 
							 board.blackKnights & (captureMask), 
							 board.whiteKnights, 
							 board.blackKing, 
							 board.whiteKing, 
							 board.blackBishops & (captureMask), 
							 board.whiteBishops  ^ destinationMask,
							 board.blackQueens & (captureMask), 
							 board.whiteQueens, 
							 board.blackRooks & (captureMask),
							 board.whiteRooks
							);
					
					pawnBoards = BitBoard.mergeBitBoardLists(new BitBoard[][]{
																	pawnBoards,
																	promotionList,
																});
					
					
				} else { //no promotions, just kosher move
					long captureMask = ~pawnMasks[i];
					
					
					pawnBoards[i] = new BitBoard(
							!board.isWhiteMove,   //toggle player to move
							 board.blackPawns & (captureMask), 
							 board.whitePawns ^ pawnMasks[i], 
							 board.blackKnights & (captureMask), 
							 board.whiteKnights, 
							 board.blackKing, 
							 board.whiteKing, 
							 board.blackBishops & (captureMask), 
							 board.whiteBishops,
							 board.blackQueens & (captureMask), 
							 board.whiteQueens, 
							 board.blackRooks & (captureMask),
							 board.whiteRooks 
							 );
				
				}
			} //end for
		} else { //black to move
			for(int i = 0; i < pawnMasks.length; i++) {
				if(BPawnEngine.readyForPromotion(pawnMasks[i])) {
					/** 
					 * since our pawn is ready for promotion, we must include a new list 
					 * of promotion moves in our pawnBoards it will have 4 elements since 
					 * there are 4 valid pieces into which a pawn can be promoted. we shall put 
					 * one in our designated place on the board, and the rest will be stored
					 * in a temporary array
					 */
					long captureMask = ~pawnMasks[i];
					
					//used to place piece on board
					long destinationMask = pawnMasks[i] & BitPatterns.horizontalBars[0];
					long pawnToDropMask = pawnMasks[i] & BitPatterns.horizontalBars[1];
					
					BitBoard[] promotionList = new BitBoard[3];
					
					//put queen in her designated place
					pawnBoards[i] = new BitBoard(
							!board.isWhiteMove,   //toggle player to move
							 board.blackPawns ^ pawnToDropMask, 
							 board.whitePawns, 	 //can't capture pawns on last row, of course 
							 board.blackKnights, 
							 board.whiteKnights & (captureMask), 
							 board.blackKing, 
							 board.whiteKing, 
							 board.blackBishops, 
							 board.whiteBishops & (captureMask),
							 board.blackQueens ^ destinationMask, 
							 board.whiteQueens & (captureMask), 
							 board.blackRooks,
							 board.whiteRooks & (captureMask)
							);
					
					//put rook in first place temporary array
					promotionList[0] = new BitBoard(
							!board.isWhiteMove,   //toggle player to move
							 board.blackPawns  ^ pawnToDropMask, 
							 board.whitePawns, 	 //can't capture pawns on last row, of course 
							 board.blackKnights, 
							 board.whiteKnights & (captureMask), 
							 board.blackKing, 
							 board.whiteKing, 
							 board.blackBishops, 
							 board.whiteBishops & (captureMask),
							 board.blackQueens, 
							 board.whiteQueens & (captureMask), 
							 board.blackRooks ^ destinationMask,
							 board.whiteRooks & (captureMask)
							);
					
					//put knight in second
					promotionList[1] = new BitBoard(
							!board.isWhiteMove,   //toggle player to move
							 board.blackPawns  ^ pawnToDropMask, 
							 board.whitePawns, 	 //can't capture pawns on last row, of course 
							 board.blackKnights ^ destinationMask, 
							 board.whiteKnights & (captureMask), 
							 board.blackKing, 
							 board.whiteKing, 
							 board.blackBishops, 
							 board.whiteBishops & (captureMask),
							 board.blackQueens, 
							 board.whiteQueens & (captureMask), 
							 board.blackRooks,
							 board.whiteRooks & (captureMask)
							);
					
					//put bishop in third
					promotionList[2] = new BitBoard(
							!board.isWhiteMove,   //toggle player to move
							 board.blackPawns  ^ pawnToDropMask, 
							 board.whitePawns,	 //can't capture pawns on last row, of course 
							 board.blackKnights, 
							 board.whiteKnights & (captureMask), 
							 board.blackKing, 
							 board.whiteKing, 
							 board.blackBishops ^ destinationMask, 
							 board.whiteBishops & (captureMask),
							 board.blackQueens, 
							 board.whiteQueens & (captureMask), 
							 board.blackRooks,
							 board.whiteRooks & (captureMask)
							);
					
					pawnBoards = BitBoard.mergeBitBoardLists(new BitBoard[][]{
																	pawnBoards,
																	promotionList,
																});
				
				} else { //kosher move
					long captureMask = ~pawnMasks[i];
					
					pawnBoards[i] = new BitBoard(
							!board.isWhiteMove,   //toggle player to move
							 board.blackPawns   ^ pawnMasks[i], 
							 board.whitePawns & (captureMask), 
							 board.blackKnights, 
							 board.whiteKnights & (captureMask), 
							 board.blackKing, 
							 board.whiteKing, 
							 board.blackBishops, 
							 board.whiteBishops & (captureMask),
							 board.blackQueens, 
							 board.whiteQueens & (captureMask), 
							 board.blackRooks,
							 board.whiteRooks & (captureMask)
							);
				}
			}
		}
		
		//pass board and MoveEngine
		return filterInCheck(pawnBoards);
	}
	
	public BitBoard[] getQueenChildren() {
		long[] queenMasks = getQueenMoveMasks();
		BitBoard[]  queenBoards = new BitBoard[queenMasks.length];
		
		
		if(board.isWhiteMove) {
		
			for(int i = 0; i < queenBoards.length; i++) {
				long captureMask = ~queenMasks[i];
			
			
				queenBoards[i] = new BitBoard(
						!board.isWhiteMove,   //toggle player to move
						 board.blackPawns & (captureMask), 
						 board.whitePawns, 
						 board.blackKnights & (captureMask), 
						 board.whiteKnights, 
						 board.blackKing, 
						 board.whiteKing, 
						 board.blackBishops & (captureMask), 
						 board.whiteBishops,
						 board.blackQueens & (captureMask), 
						 board.whiteQueens  ^ queenMasks[i], 
						 board.blackRooks & (captureMask),
						 board.whiteRooks 
						);
				} 
		} else {
			for(int i = 0; i < queenBoards.length; i++) {
				long captureMask = ~queenMasks[i];
				
				queenBoards[i] = new BitBoard(
						!board.isWhiteMove,   //toggle player to move
						 board.blackPawns, 
						 board.whitePawns & (captureMask), 
						 board.blackKnights, 
						 board.whiteKnights & (captureMask), 
						 board.blackKing, 
						 board.whiteKing, 
						 board.blackBishops, 
						 board.whiteBishops & (captureMask),
						 board.blackQueens    ^ queenMasks[i], 
						 board.whiteQueens & (captureMask), 
						 board.blackRooks,
						 board.whiteRooks & (captureMask)
						);
			}
		}
		
		//pass board and MoveEngine
		return filterInCheck(queenBoards);
	}
	
	public BitBoard[] getKnightChildren() {
		long[] knightMasks = getKnightMoveMasks();
		BitBoard[]  knightBoards = new BitBoard[knightMasks.length];
		
		
		if(board.isWhiteMove) {
		
			for(int i = 0; i < knightBoards.length; i++) {
				long captureMask = ~knightMasks[i];
			
			
				knightBoards[i] = new BitBoard(
						!board.isWhiteMove,   //toggle player to move
						 board.blackPawns & (captureMask), 
						 board.whitePawns, 
						 board.blackKnights & (captureMask), 
						 board.whiteKnights ^ knightMasks[i], 
						 board.blackKing, 
						 board.whiteKing, 
						 board.blackBishops & (captureMask), 
						 board.whiteBishops,
						 board.blackQueens & (captureMask), 
						 board.whiteQueens, 
						 board.blackRooks & (captureMask),
						 board.whiteRooks 
						);
				} 
		} else {
			for(int i = 0; i < knightBoards.length; i++) {
				long captureMask = ~knightMasks[i];
				
				knightBoards[i] = new BitBoard(
						!board.isWhiteMove,   //toggle player to move
						 board.blackPawns, 
						 board.whitePawns & (captureMask), 
						 board.blackKnights ^ knightMasks[i], 
						 board.whiteKnights & (captureMask), 
						 board.blackKing, 
						 board.whiteKing, 
						 board.blackBishops, 
						 board.whiteBishops & (captureMask),
						 board.blackQueens, 
						 board.whiteQueens & (captureMask), 
						 board.blackRooks,
						 board.whiteRooks & (captureMask)
						);
			}
		}
		
		//pass board and MoveEngine
		return filterInCheck(knightBoards);
	}

	public BitBoard[] getKingChildren() {
		long[] kingMasks = getKingMoveMasks();
		BitBoard[]  kingBoards = new BitBoard[kingMasks.length];
		
		
		if(board.isWhiteMove) {
		
			for(int i = 0; i < kingBoards.length; i++) {
				long captureMask = ~kingMasks[i];
			
			
				kingBoards[i] = new BitBoard(
						!board.isWhiteMove,   //toggle player to move
						 board.blackPawns & (captureMask), 
						 board.whitePawns, 
						 board.blackKnights & (captureMask), 
						 board.whiteKnights, 
						 board.blackKing, 
						 board.whiteKing ^ kingMasks[i], 
						 board.blackBishops & (captureMask), 
						 board.whiteBishops,
						 board.blackQueens & (captureMask), 
						 board.whiteQueens, 
						 board.blackRooks & (captureMask),
						 board.whiteRooks 
						);
				} 
		} else {
			for(int i = 0; i < kingBoards.length; i++) {
				long captureMask = ~kingMasks[i];
				
				kingBoards[i] = new BitBoard(
						!board.isWhiteMove,   //toggle player to move
						 board.blackPawns, 
						 board.whitePawns & (captureMask), 
						 board.blackKnights, 
						 board.whiteKnights & (captureMask), 
						 board.blackKing  ^ kingMasks[i], 
						 board.whiteKing, 
						 board.blackBishops, 
						 board.whiteBishops & (captureMask),
						 board.blackQueens, 
						 board.whiteQueens & (captureMask), 
						 board.blackRooks,
						 board.whiteRooks & (captureMask)
						);
			}
		}
		
		//pass board and MoveEngine
		return filterInCheck(kingBoards);
	}
	
	/**
	 * checks that the king of the player who last made a move is not in check
	 * that means it checks for the player who is not to move (i.e. !board.isWhiteMove )
	 * @param board
	 * @return
	 */
	
	public static boolean isBitBoardInCheck(BitBoard board) {
		return KingEngine.isInCheck(board);
	}
	
	public static BitBoard[] filterInCheck(BitBoard[] boardList) {
		
		boolean[] filter = new boolean[boardList.length];
		int notInCheckNumber = 0;
		
		for(int i = 0; i < boardList.length; i++) {
			if(filter[i] = KingEngine.isInCheck(boardList[i])) {
			
			} else {
				notInCheckNumber++;
			}
		}
		
		
		BitBoard[] safeBoardList = new BitBoard[notInCheckNumber];
		int j = 0;
		for(int i = 0; i < boardList.length; i++) {
			if(!filter[i]) {
				safeBoardList[j++] = boardList[i];
			}
		}
		
		return safeBoardList;
	}
		
	public static long[] mergeMoveLists(long[][] moveLists) {
		
		int size = 0;
		
		for(long[] list : moveLists) {
			size += list.length;
		}
		
		long[] allMoves = new long[size];
		
		size = 0;
		for(long[] list : moveLists) {
			for(long moveMask : list) {
				allMoves[size++] = moveMask;
			}
		}
		
		return allMoves;
	}
	
	public static long[] filterInvalidMoveMasks(long[][] moveHeuristics) {
		int moveCount = 0;
		for(long[] moveVect : moveHeuristics) {
			for(long move : moveVect) {
				if(move != INVALID_MOVE_MASK) {
					moveCount++;
				}
			}
		}
		
		long[] validMoves = new long[moveCount];
		
		moveCount = 0;
		for(long[] moveVect : moveHeuristics) {
			for(long move : moveVect) {
				if(move != INVALID_MOVE_MASK) {
					validMoves[moveCount++] = move;
				}
			}
		}
		
		return validMoves;
	}
	
	public static long[] unPackBitMap(long bitMap) {
		int bitCount = Long.bitCount(bitMap);
		long[] unpacked = new long[bitCount];
		
		for(int i = 0; i < bitCount; i++) {
			unpacked[i] = Long.highestOneBit(bitMap); //cache bit
			bitMap -= unpacked[i]; 				//delete bit from bitMap
		}
		
		return unpacked;
	}
	
	public static long packBitMap(long[] bitPieces) throws IllegalArgumentException {
		long bitMap = 0;
		
		for(long piece : bitPieces) {
			if(DEBUG) {
				if(Long.bitCount(piece) != 1) {
					throw new IllegalStateException("bitMap of one Piece " + 
							BitPatterns.formatBitString(bitMap) +
							" should have exactly one bit set to be mapped to a Position on the board!");	
				}					
			}
			
			//out of debug flag because o less Overhead incurred than first test
			if((bitMap & piece) != 0) {
				throw new IllegalArgumentException("pieces cannot be packed because of square collisions");
			}
		}
		
		return 0;
	}	
}