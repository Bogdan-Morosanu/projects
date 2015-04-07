package ai.BitModel;

import controller.AppRegistry;
import controller.Controller;
import controller.Controller.Player;
import board.Map;
import board.Position;
import pieces.Piece;
import pieces.King;
import pieces.Knight;
import pieces.Bishop;
import pieces.Queen;
import pieces.Rook;
import pieces.Pawn;
import pieces.PieceList;

/**
 * Translates from Map to Bit Array representation.
 * Is used by AI to read the Map.
 * @author moro
 *
 */
public class BitTranslator {

	private static PieceList kings = new PieceList();
	private static PieceList pawns = new PieceList();
	private static PieceList bishops = new PieceList();
	private static PieceList knights = new PieceList();
	private static PieceList queens = new PieceList();
	private static PieceList rooks = new PieceList();
	
	/**
	 * ---------- Initialize Bit Translator -----
	 */
	static {
		loadPieces();
	}
	
	
	public static void loadPieces() {
		
		kings.clear();
		pawns.clear();
		bishops.clear();
		knights.clear();
		queens.clear();
		rooks.clear();
		
		Map map = AppRegistry.getMap();
		
		PieceList piecesOnBoard = map.toPieceList();
		
		for(Piece p : piecesOnBoard) {
			BitTranslator.registerPiece(p);
		}

	}
	
	public static int indexTranslation(int row, int col) {
		return row * 8 + col;
	}
	
	public static void registerPiece(Piece p) {
		if(p instanceof King) {
			kings.add(p);
		} else if(p instanceof Pawn) {
			pawns.add(p);
		} else if(p instanceof Bishop) {
			bishops.add(p);
		} else if(p instanceof Knight) {
			knights.add(p);
		} else if(p instanceof Rook) {
			rooks.add(p);
		} else if(p instanceof Queen) {
			queens.add(p);
		}
	}
	
	public static long getPawns(Piece.Color c) {
		long seed = 0;
		for(Piece pawn : pawns) {
			
			if(pawn.getColor() == c) {
				Position pos = pawn.getPosition();
				
				//toggle bits at right indexes
				seed += 1L << indexTranslation(pos.row,pos.col);
			}
		}
		
		return seed;
	}
	
	public static long getKnights(Piece.Color c) {
		long seed = 0;
		for(Piece knight : knights) {
			
			if(knight.getColor() == c) {
				Position pos = knight.getPosition();
				//toggle bits at right indexes
				seed += 1L << indexTranslation(pos.row,pos.col);
			}
		}
		
		return seed;
	}

	public static long getKing(Piece.Color c) {
		long seed = 0;
		for(Piece king : kings) {
			if(king.getColor() == c) {
				Position pos = king.getPosition();
				seed += 1L << indexTranslation(pos.row,pos.col);
			}
		}
		return seed;
	}
	
	public static long getBishops(Piece.Color c) {
		long seed = 0;
		for(Piece bishop : bishops) {
			if(bishop.getColor() == c) {
				Position pos = bishop.getPosition();
				seed += 1L << indexTranslation(pos.row,pos.col);
			}
		}
		return seed;
	}
	

	public static long getQueens(Piece.Color c) {
		long seed = 0;
		for(Piece queen : queens) {
			if(queen.getColor() == c) {
				Position pos = queen.getPosition();
				seed += 1L << indexTranslation(pos.row,pos.col);
			}
		}
		return seed;
	}
	

	public static long getRooks(Piece.Color c) {
		long seed = 0;
		for(Piece rook : rooks) {
			if(rook.getColor() == c) {
				Position pos = rook.getPosition();
				seed += 1L << indexTranslation(pos.row,pos.col);
			}
		}
		return seed;
	}
	
	public static BitBoard getStateOfMap() {
		
		loadPieces();
		
		return new BitBoard(
				Controller.getCurrentPlayer() == Player.WHITE,
				
				getPawns(Piece.Color.BLACK),
				getPawns(Piece.Color.WHITE),
				
				getKnights(Piece.Color.BLACK),
				getKnights(Piece.Color.WHITE),
				
				getKing(Piece.Color.BLACK),
				getKing(Piece.Color.WHITE),
				
				getBishops(Piece.Color.BLACK),
				getBishops(Piece.Color.WHITE),
				
				getQueens(Piece.Color.BLACK),
				getQueens(Piece.Color.WHITE),
				
				getRooks(Piece.Color.BLACK),
				getRooks(Piece.Color.WHITE)
				);
		
	}
	
	public static void wipe() {
		kings.clear();
		pawns.clear();
		bishops.clear();
		knights.clear();
		queens.clear();
		rooks.clear();
	}
	
	public static void main(String[] args) {
		BitBoard bitBoard = getStateOfMap();
		System.out.println(bitBoard);
	}

}
