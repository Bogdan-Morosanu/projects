package ai.BitModel;

/**
 * Houses utility methods and read-only templates for generating arbitrary bit patterns.
 * the read-only part of the templates is somewhat essential.
 * @author moro
 *
 */
public class BitPatterns {	
	private static final boolean DEBUG = true;
	
	static long initTimeStamp = System.currentTimeMillis();
	//speed tracking purposes
	
	public static long[] mainDiagonals = new long[15];
	public static long[] secondDiagonals  = new long[15];
	public static long[] verticalBars  = new long[8];
	public static long[] horizontalBars  = new long[8];
	
	/**
	 * Will contain a table of bitmaps that for each linesBetween[posOne][posTwo] will return a bitmap
	 * representing the line (if any) connecting the two positions, with the ends themselves not included
	 */
	public static long[][] linesBetween = new long[64][64];
	
	/**
	 * Part of the BitMoveEngine assume this template to be at [3][3] to speed up
	 * their computations. do not move this unless you're prepared to refactor that code as well.
	 * 
	 * template used for all pieces that generate their moves via superimposing a
	 * template of possible moves over some position and then shifting it
	 * so that it is centered in piece position. that "some position is [3][3].
	 * i.e. D4 on the chess board.
	 */
	public static long pieceAt33 = 1L << 3 * 8 + 3;
	
	/**
	 * TODO ... these templates probably should be refactored in the Piece Engine inner classes
	 * of the MoveEngine class.
	 * 
	 * bitmap for king moves # row 3 col 3
	 * excludes castling for now
	 */
	public static long kingAt33Template;
	
	/**
	 * bitmap for horse moves @ row 3 col 3
	 */
	public static long horseAt33Template;
	
	/**
	 * bitmap for black and white pawn moves @ row 3 col 3
	 * excludes oppening 2sq moves and enpassant captures for now
	 * 
	 * -------- initializing pawn templates --------
	 * white pawn moves up the board, hence the row displacement is +1
	 * the black pawn on the other hand comes down the board, so the row
	 * displacement for it will be -1
	 */
	
	public static final long wPawnAt33CaptureTemplate = (1L << (4 * 8 + 4))  //capture row 4 col 4
												+ (1L << (4 * 8 + 2)); 		//capture row 4 col 2
	public static final long wPawnAt33MoveTemplate = (1L << (4 * 8 + 3)); //just move forward
	

	public static final long bPawnAt33CaptureTemplate = (1L << (2 * 8 + 4))  //capture row 2 col 4
												+ (1L << (2 * 8 + 2)); 		//capture row 2 col 2
	public static final long bPawnAt33MoveTemplate = (1L << (2 * 8 + 3)); //just move forward	
	static {
		
		/*
		 * --------- Initializing main Lookup Tables used by program --------
		 */
		
		
		long firstHorizontal = 255;
		long firstVertical = 0;
		for(int i = 0; i < 8; i++) {
			firstVertical <<= 8;
			firstVertical++;
		}
		long mainDiagonal = 0;
		for(int i = 0; i < 8; i++) {
			mainDiagonal <<= 9;
			mainDiagonal++;
		}
		
		
		
		long secondDiagonal = 0;
		for(int i = 0; i < 8; i++) {
			secondDiagonal <<= 7;
			secondDiagonal += 128;
		}
		
		
		for(int i = 0; i < 8; i++) {
			//diagonals start in the middle of the board, thus we must shift
			mainDiagonals[i] = shiftBy(mainDiagonal, i - 7, 0);
			secondDiagonals[i] = shiftBy(secondDiagonal, i - 7, 0);
			mainDiagonals[14 - i] = shiftBy(mainDiagonal, 7 - i, 0);
			secondDiagonals[14 - i] = shiftBy(secondDiagonal, 7 - i, 0);
			
			
			
			verticalBars[i] = shiftBy(firstVertical,0,i);
			horizontalBars[i] = shiftBy(firstHorizontal,i,0);
		}
		
		//see attached excel for visual explanation of what's happenning
		long halfHorse = (horizontalBars[1] + horizontalBars[5]) &
						 (verticalBars[2] + verticalBars[4]);
		
		
		
		long otherHalfHorse = (horizontalBars[2] + horizontalBars[4]) &
							  (verticalBars[1] + verticalBars[5]);
		

		
		horseAt33Template = halfHorse + otherHalfHorse;
		
		/**
		 * will be used to mark the bits arround [3][3] for king.
		 * Staying in place is not a valid move so we mark that one out! 
		 */
		kingAt33Template = 0;
		for(int rowNum = 2; rowNum < 5; rowNum++) {
			for(int colNum = 2; colNum < 5; colNum++ ) {
				if(rowNum != 3 || colNum != 3) {
					kingAt33Template += 1L << (rowNum * 8 + colNum);
				}
			}
		}
		
		
		/*
		 * ----------- Initializing lookup table to determine line of sight between pieces -------------
		 * Will contain a table of bitmaps that for each linesBetween[posOne][posTwo] will return a bitmap
		 * representing the line (if any) connecting the two positions, with the ends themselves not included
		 */
		for(int first = 0; first < 64; first++) {
			for(int second = 0; second < 64; second++) {
				//deal with identicals first
				if(first == second) {
					linesBetween[first][second] = 0;
					//continue;
					
				} else {
					
					int rowDiff = Math.abs(second / 8 - first / 8);
					int colDiff = Math.abs(second % 8 - first % 8);
					
					
					if(rowDiff == colDiff || //rowDiff and colDiff cannot be both 0, identity case already excluded
					   rowDiff == 0 && colDiff > 0 ||
					   rowDiff > 0 && colDiff == 0) {
						
						//we have reached this point because we have a valid line
						linesBetween[first][second] = drawLineBetween(first,second);

						//continue;
						
					} else {
						
						//not a valid line
						linesBetween[first][second] = 0;
						//continue;
					}
				}
			}
	
		}
		
	}
	
	private static long drawLineBetween(int firstPos, int secondPos) {
		
		
		long firstNum = 1L << firstPos;
		long secondNum = 1L << secondPos;
		
		long step1 = 0;
		long step2 = secondNum;
		
		int rowDiff = rowDiffBetweenBitPos(firstPos,secondPos);
		int colDiff = colDiffBetweenBitPos(firstPos,secondPos);
		
		int rowStep = (rowDiff == 0 ? 0 : (rowDiff > 0 ? 1 : -1));
		int colStep = (colDiff == 0 ? 0 : (colDiff > 0 ? 1 : -1));
		
		/*
		 * will start a line from firstNum, adding one bit at a time in the
		 * direction of secondNum. will also cut off any bits that get to 
		 * secondNum. thus when our line is still drawing itself, bits are
		 * being set on the direction first -> second. when we reach second,
		 * however, line step1 &= ~secondNum will mask the last bit inserted
		 * and thus step1 will not change from the initial value it had, marking
		 * the end of the loop.
		 */
		while(step1 != step2) { 
			step2 = step1; //cache last result
			
			step1 += firstNum; //draw one bit more
			
			step1 = shiftBy(step1,rowStep,colStep); //shift whole line toward end
			
			step1 &= ~secondNum; //takeout end
		}
		
		return step2;
	}
	

	
	/**
	 * Returns an index into mainDiagonal lookup table that will overlap with bit set in piece
	 * @param piece
	 * @return
	 */
	public static int mainDiagIndexOf(long piece) {
		int rowNum = bitPieceRowNum(piece);
		int colNum = bitPieceColNum(piece);
		
		return rowNum - colNum + 7;
	}
	
	/**
	 * Returns an index into mainDiagonal lookup table that will overlap with bit specified by row and column number.
	 * @param piece
	 * @return
	 */
	public static int mainDiagIndexOf(int rowNum, int colNum) {
		return rowNum - colNum + 7;
	}
	
	/**
	 * Returns an index into secondaryDiagonal lookup table that will overlap with bit set in piece 
	 * @param piece
	 * @return
	 */
	public static int secondDiagIndexOf(long piece) {
		int rowNum = bitPieceRowNum(piece);
		int colNum = bitPieceColNum(piece);
		
		return colNum + rowNum;
	}
	
	/**
	 * Returns an index into secondaryDiagonal lookup table that will overlap with bit specified by row and column number.
	 * @param piece
	 * @return
	 */
	public static int secondDiagIndexOf(int rowNum, int colNum) {
		return colNum + rowNum;
	}
	
	public static int bitPosColNum(int pos) {
		return pos % 8;
	}
	
	public static int bitPosRowNum(int pos) {
		return pos / 8;
	}
	
	public static int bitPieceColNum(long bitPiece) {
		if(DEBUG && (Long.bitCount(bitPiece) != 1)) {
			throw new IllegalStateException("Precondition invalidated! \nBitMap passed to " +
					"bitPieceColNum method should have only one bit set. map follows : \n" 
					+ formatBitString(bitPiece));
		}
		
		int colNum;

		
		for(colNum = 0; colNum < 8; colNum++) {
			if(bitPiece == (bitPiece & verticalBars[colNum])) {
				//we've hit the bit set inside pieceOne!
				break;
			}
		}
		
		return colNum;
	}
	
	public static int bitPieceRowNum(long bitPiece) {
		if(DEBUG && (Long.bitCount(bitPiece) != 1)) {
			throw new IllegalStateException("Precondition invalidated! \nBitMap passed to " +
					"bitPieceRowNum method should have only one bit set. map follows : \n" 
					+ formatBitString(bitPiece));
		}
		
		int rowNum;

		
		for(rowNum = 0; rowNum < 8; rowNum++) {
			if(bitPiece == (bitPiece & horizontalBars[rowNum])) {
				//we've hit the bit set inside pieceOne!
				break;
			}
		}
		
		return rowNum;
	}
	
	public static int colDiffBetweenBitPos(int posOne, int posTwo) {
		
		return posTwo % 8 - posOne % 8;
	}
	
	public static int rowDiffBetweenBitPos(int posOne, int posTwo) {
		return posTwo / 8 - posOne / 8;
	}
	
	public static int colDiffBetweenBitMaps(long pieceOne, long pieceTwo) {
		
		
		// TODO should probably be an assertion
		if(DEBUG && (Long.bitCount(pieceOne) != 1 || Long.bitCount(pieceTwo) != 1)) {
			throw new IllegalStateException("Precondition invalidated! \nBitMaps passed to " +
					"collDiffBetweenBitMaps method should have only one bit set. maps follow : \n" 
					+ formatBitString(pieceOne) +  "\n" + formatBitString(pieceTwo));
		}
		
		int colOne;
		int colTwo;
		
		for(colOne = 0; colOne < 8; colOne++) {
			if(pieceOne == (pieceOne & verticalBars[colOne])) {
				//we've hit the bit set inside pieceOne!
				break;
			}
		}
		
		for(colTwo = 0; colTwo < 8; colTwo++) {
			if(pieceTwo == (pieceTwo & verticalBars[colTwo])) {
				//we've hit the bit set inside pieceTwo!
				break;
			}
		}
		
		
		
		return colTwo - colOne;
	}
	
	public static int rowDiffBetweenBitMaps(long pieceOne, long pieceTwo) {
		
		//TODO should probably be an assertion
		if(DEBUG && (Long.bitCount(pieceOne) != 1 || Long.bitCount(pieceTwo) != 1)) {
			throw new IllegalStateException("Precondition invalidated! \nBitMaps passed to " +
					"rowDiffBetweenBitMaps method should have only one bit set. maps follow : \n" 
					+ formatBitString(pieceOne) +  "\n" + formatBitString(pieceTwo));
		}
		
		int rowOne;
		int rowTwo;
		
		for(rowOne = 0; rowOne < 8; rowOne++) {
			if(pieceOne == (pieceOne & horizontalBars[rowOne])) {
				//we've hit the bit set inside pieceOne!
				break;
			}
		}
		
		for(rowTwo = 0; rowTwo < 8; rowTwo++) {
			if(pieceTwo == (pieceTwo & horizontalBars[rowTwo])) {
				//we've hit the bit set inside pieceTwo!
				break;
			}
		}
		
		if(rowTwo == 8 || rowOne == 8) {
			throw new IllegalStateException("no bits set in one of the pieces " 
					+ formatBitString(pieceOne) +  "\nor\n" + formatBitString(pieceTwo));
		}
		
		return rowTwo - rowOne;
	}
	
	/**
	 * returns a linear index bounded from 0 to 63 that represents the number of the square
	 * in which our piece is set.
	 * @param bitMap
	 * @return
	 */
	public static int bitPieceToBitPos(long bitMap) {

		for(int i = 0; i < 64; i++) {
			if(1L << i == bitMap) {
				return i;
			}
		}
		
		throw new IllegalStateException("bitMap of one Piece " + formatBitString(bitMap) 
			+ " should have exactly one bit set to be mapped to a Position on the board!");
	}
	
	public static void showAll(long[] bitMaps) {
		for(long bitMap : bitMaps) {
			show(bitMap);
		}		
	}

	public static String formatBitString(long bitMap) {
		String bStr = Long.toBinaryString(bitMap);
		
		String formatted = "";
		int initialLen = bStr.length();
		
		for(int i = 0; i < 64 - initialLen; i++) {
			bStr = "0" + bStr;
		}
		
		for(int i = 0; i < 64; i++) {
			formatted += (i % 8 == 7) ? bStr.charAt(i) + "\n" : bStr.charAt(i);
		}
		return formatted;
	}
	
	/**
	 * Main method used to shift bit patterns around the board!
	 * @param what
	 * @param rows
	 * @param cols
	 * @return
	 */
	
	public static long shiftBy(long what, int rows, int cols) {
		int linearOffset = rows * 8 + cols;
		long shifted =  (linearOffset > 0) ? (what << linearOffset) : (what >>> -linearOffset);
		return shifted & wraparroundMask(cols); //mask column wrap-around
	}
	
	/**
	 * column wrap-around mask. 
	 * bits shifted out of columns end up in higher rows, since our integer is actually a one-dimensional array of bits
	 * we must therefore mask columns when shifting left/right.
	 * rows do no have this problems. shifting off the last row means our bits are shifted off the LSB or MSB
	 * @param rows
	 * @param cols
	 * @return
	 */
	
	private static long wraparroundMask(int cols) {
		long mask = -1; // all bits toggled
		
		if(cols > 0) {
			//we are moving right, must mask the left side of our map
			//these values are located at the leftmost/lowest part of the vertical bars array
			for(int i = 0; i < cols; i++) {
				mask ^= verticalBars[i];
			}
		} else {
			//we are moving left, ergo we must mask the rightmost part of our map
			//these values are located at the rightmost/highest part of the vertical bars
			for(int i = 0; i > cols; i--) {
				mask ^= verticalBars[7 + i];
			}
		}
		
		return mask;
	}
	
	public static void show(long bitArr) {
		System.out.println(formatBitString(bitArr));
	}
	
	
	
	private static boolean allRowDifferencesCoincideTest() {
		boolean checkFlag = true;
		
		for(int firstRow = 0; firstRow < 7; firstRow ++)
		for(int firstCol = 0; firstCol < 7; firstCol ++)
		for(int secondRow = 0; secondRow < 7; secondRow++)
		for(int secondCol = 0; secondCol < 7; secondCol++) {
			int firstPos = firstRow * 8 + firstCol;
			int secondPos = secondRow * 8 + secondCol;
			
			long firstPiece = 1L << firstPos;
			long secondPiece = 1L << secondPos;
			
			checkFlag = checkFlag && 
					(rowDiffBetweenBitPos(firstPos,secondPos) == (secondRow - firstRow) &&
					(rowDiffBetweenBitMaps(firstPiece,secondPiece) == (secondRow - firstRow)));
			
				if(!checkFlag) {
					System.out.println("Bug @ " + firstPos + " : " + secondPos 
							+ "\nsecondRow - firstRow : " + (secondRow - firstRow)
							+ "\nrowDiffBetweenBitPos(firstPos,secondPos) : " + rowDiffBetweenBitPos(firstPos,secondPos) 
							+ "\nrowDiffBetweenBitMaps(pieceOne,pieceTwo) : " + rowDiffBetweenBitMaps(firstPiece,secondPiece));
									
					return checkFlag;
				}
		}
	
		
		
		return checkFlag;
	
	}
	
	private static boolean allColDifferencesCoincideTest() {
		boolean checkFlag = true;
		
		for(int firstRow = 0; firstRow < 7; firstRow ++)
		for(int firstCol = 0; firstCol < 7; firstCol ++)
		for(int secondRow = 0; secondRow < 7; secondRow++)
		for(int secondCol = 0; secondCol < 7; secondCol++) {
			int firstPos = firstRow * 8 + firstCol;
			int secondPos = secondRow * 8 + secondCol;
			
			long firstPiece = 1L << firstPos;
			long secondPiece = 1L << secondPos;
			
			checkFlag = checkFlag && 
					(colDiffBetweenBitPos(firstPos,secondPos) == (secondCol - firstCol) &&
					colDiffBetweenBitMaps(firstPiece,secondPiece) == (secondCol - firstCol));
			
				if(!checkFlag) {
					System.out.println("Bug @ " + firstPos + " : " + secondPos 
							+ "\nsecondCol - firstCol : " + (secondCol - firstCol)
							+ "\ncolDiffBetweenBitPos(firstPos,secondPos) : " + colDiffBetweenBitPos(firstPos,secondPos) 
							+ "\ncolDiffBetweenBitMaps(firstPiece,secondPiece) : " + colDiffBetweenBitMaps(firstPiece,secondPiece));
									
					return checkFlag;
				}
		}
	
		
		
		return checkFlag;
	
	}
	
	
	private static boolean allSymmetricPairsTest() {
		boolean checkFlag = true;
			
			for(int row = 0; row < 7; row++) {
				for(int col = 0; col < 7; col++) {
					checkFlag = checkFlag && linesBetween[row][col] == linesBetween[col][row];
				
					if(!checkFlag) {
						System.out.println("Bug @ row : " + row + " col " + col 
								+ "\nvalues follow" + "  linesBetween[row][col] : ");
						show(linesBetween[row][col]);
						System.out.println("linesBetween[col][row] : ");
						show(linesBetween[col][row]);
						return checkFlag;
					}
				}
			}
			
			
		return checkFlag;
	}
	
	
	
	private static void speedTest() {
		long start = System.currentTimeMillis();
		
		long[] newPos = new long[10000];
		
		for(int i = 0; i < 3e8; i++) {
			newPos[i % 10000] = shiftBy(horseAt33Template,(i % 7) - 3, (i % 7) - 3);
		}

		System.out.println("Finished speed testing in " + (System.currentTimeMillis() - start) + " miliseconds");
	}
	
	
	public static void main(String[] args) {
		
		System.out.println(allSymmetricPairsTest());
		System.out.println(allRowDifferencesCoincideTest());
		System.out.println(allColDifferencesCoincideTest());
		speedTest();
		
	}
}