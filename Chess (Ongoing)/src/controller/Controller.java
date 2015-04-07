package controller;

import ai.BitModel.BitTranslator;
import board.InvalidPositionException;
import board.Map;
import board.Position;
import board.PositionList;
import pieces.King;
import pieces.Knight;
import pieces.Pawn;
import pieces.Piece;
import pieces.PieceList;
import views.SimpleBoard;

/**
 * Controller Class Handles game flags : check, checkmate, player to move, etc 
 * @author moro
 *
 */
public class Controller {
	
	private static final boolean DEBUG = false;
	private static final boolean AI_GAME = false;
	
	public enum GameStatus {
		WHITE_WIN, BLACK_WIN, DRAW, ONGOING;
		
		public String toString() {
			switch(this) {
				case WHITE_WIN: return "White wins!";
				case BLACK_WIN: return "Black wins!";
				case DRAW: return "Draw!";
				default: return "Ongoing!";
			}
		}
	}
	
	public enum Player {
		WHITE, BLACK;
		
		public Piece.Color toPieceColor() {
			if(this == WHITE) {
				return Piece.Color.WHITE;
			} else {
				return Piece.Color.BLACK;
			}
		}
	}
	
	private static GameStatus gameStatus = GameStatus.ONGOING;
	private static boolean whiteInCheck = false;
	private static boolean whiteTrapped = false;
	private static boolean blackInCheck = false;
	private static boolean blackTrapped = false;
	private static Player toMove = Player.WHITE;
	private static Pawn pawnToBePromoted = null;
	
	
	public synchronized static GameStatus checkStatus() {
		
		Controller.pawnToBePromoted = findPawnPromotion();
		

		King whiteKing = AppRegistry.getWhiteKing();
		King blackKing = AppRegistry.getBlackKing();
		
		Controller.whiteInCheck = computeCheckStatus(whiteKing);
		Controller.blackInCheck = computeCheckStatus(blackKing);
		
		Controller.whiteTrapped = computeTrapStatus(whiteKing);
		Controller.blackTrapped = computeTrapStatus(blackKing);
		
		
		if(isCheckMated(whiteKing)) {
			return GameStatus.BLACK_WIN;
		} 
		
		if(isCheckMated(blackKing)) {
			return GameStatus.WHITE_WIN;
		} 
		
		//TODO implement map.getPiecesOfColor();
		//if(whiteTrapped || blackTrapped) return GameStatus.DRAW;
		
		return GameStatus.ONGOING;
	}
	
	public static boolean isCheckMated(King k) {
		if(isInCheck(k) && isKingTrapped(k)) {
			//we must get possible defenders
			Map map = AppRegistry.getMap();
			
			PieceList attackers = 
					map.getPossibleAttackersOfColor(k,k.getColor().getAdversary());
			
			if(attackers.size() > 1) {
				//King is forked, block impossible, checkmate
				return true;
			}
			
			//otherwise this is our sole attacker
			Piece attacker = attackers.get(0);
			
			PositionList toBlock = null;
			
			try {
				if(attacker instanceof Knight) {
					//no line between king and Knigth!
					toBlock = new PositionList();
					toBlock.add(attacker.getPosition());
				
				} else {
					toBlock = map.getPositionLine(k.getPosition(), attacker.getPosition());
				}
			} catch(InvalidPositionException e) {
				//technically impossible, unless map data is corrupt
				System.out.println(e.getMessage());
			}
			
			for(Position pos : toBlock) {
				PieceList defenders = map.getPossibleAttackersOfColor(pos, k.getColor());
				if(defenders.size() > 0) {
					if(defenders.size() == 1 && defenders.get(0) == k) {
						//it looks like we are the sole defender
						return true; //tough luck
					}
					return false; //we have found our defender!
				}
			}
			
			return true; //nobody to defend, trapped and in check
		} else {
			return false; //not trapped
		}
	}
	
	public static boolean isInCheck(King k) {
		return (k.getColor() == Piece.Color.WHITE) ? whiteInCheck : blackInCheck;
	}
	
	public static boolean computeCheckStatus(King k) {
		
		//DEBUG System.out.println("\nfor " + k);
		
		Map map = AppRegistry.getMap();
		PieceList attackers = map.getPossibleAttackers(k);
		
		/*if(DEBUG) for(Piece attacker : attackers) {
			System.out.println(attacker.toString() );
		}*/
		
		if(attackers.containsColor(k.getAdversaryColor())) {
			return true;
		}
		
		return false;
	}
	
	public static boolean computeTrapStatus(King k) {
		//we are under fire, must see if King can move to different place
		if(DEBUG) {
			System.out.println("looking for escapes for : " + k);
		}
		Map map = AppRegistry.getMap();
		PositionList possibleEscapes = map.getSurroundingConstPositions(k.getPosition());
		for(Position escape : possibleEscapes) {
			
			if(DEBUG) {
				System.out.println("testing for escape @ " + escape);
			}
			if(escape != null && k.moveTypePermitted(escape)) {
				return false;
			}
		}
		
		return true;
	}
	
	public static boolean isKingTrapped(King k) {
		return (k.getColor() == Piece.Color.WHITE) ? whiteTrapped : blackTrapped;
	}
	
	public static Pawn findPawnPromotion() {
		
		
		Map map = AppRegistry.getMap();
		Position cornerNW = AppRegistry.getConstPosition(0, 0);
		Position cornerNE = AppRegistry.getConstPosition(0, 7);
		Position cornerSE = AppRegistry.getConstPosition(7, 7);
		Position cornerSW = AppRegistry.getConstPosition(7, 0);

		try {
			//see for black pawns
			
			PieceList firstLine = map.getPieceLinePlusOrigin(cornerNW,cornerNE);
			
			for(Piece p : firstLine) {

				if(p instanceof Pawn && p.getColor() == Piece.Color.BLACK) {
					return (Pawn)p;
				}
			}
			
		} catch(InvalidPositionException e) {
			//can't technically happen
			System.out.println(e.getMessage() + " : called from pawn promotion method in controller");
		}
		
		
		try {
			//check for white pawns
			PieceList lastLine = map.getPieceLinePlusOrigin(cornerSW,cornerSE);
			for(Piece p : lastLine) {
				if(p instanceof Pawn && p.getColor() == Piece.Color.WHITE) {
					return (Pawn)p;

				}
			}
		} catch(InvalidPositionException e) {
			//can't technically happen
			System.out.println(e.getMessage() + " : called from pawn promotion method in controller");
		}
		
		return null; 
	}
	
	public static Pawn pawnPromotionDue() {
		return Controller.pawnToBePromoted;
	}
	
	
	/**
	 * called from GUI
	 * @param choice
	 */
	public static void doPawnPromotion(Class<? extends Piece> choice) {
		Map map = AppRegistry.getMap();
		Piece.Color color = Controller.pawnToBePromoted.getColor();
		Position pos = Controller.pawnToBePromoted.getPosition();
		
		Piece replacerPiece = Piece.createPiece(choice,color,pos);
		
		map.setPieceAt(pos, replacerPiece);
		
		Controller.pawnToBePromoted = null; //reset promotion
	}
	
	/**
	 * called from PromotionMove class
	 * @param choice
	 * @param pawnPromoted
	 */
	public static void doPawnPromotion(Class<? extends Piece> choice, Pawn pawnPromoted) {
		Map map = AppRegistry.getMap();
		Piece.Color color = pawnPromoted.getColor();
		Position pos = pawnPromoted.getPosition();
		
		Piece replacerPiece = Piece.createPiece(choice,color,pos);
		
		map.setPieceAt(pos, replacerPiece);
		
	}
	
	
	public static Player getNextPlayer() {
		if(toMove == Player.WHITE) {
			return Player.BLACK;
		} else {
			return Player.WHITE;
		}
	}
	
	public static boolean isAITurn() {
		return Controller.getCurrentPlayer() == Player.BLACK;
	}
	
	public static void executeTurnActions() {
		//execute Player actions
		if(Executor.executeNext()) {
			Controller.toMove = getNextPlayer();	
			Controller.gameStatus = checkStatus();
			SimpleBoard.instance.refreshGUI();
		}
		
		if(AI_GAME) {
			//calculater AI
			if(isAITurn()) {
				Executor.loadAIMove();
			}
	
			//execute AI
			if(Executor.executeNext()) {
				Controller.toMove = getNextPlayer();	
				Controller.gameStatus = checkStatus();
	
				SimpleBoard.instance.refreshGUI();
			}
		}
	}
	
	public static void undoTurnActions() {
		if(Executor.undoLast()) {	
			Controller.toMove = getNextPlayer();
			Controller.gameStatus = checkStatus();
		}
	}
	
	 
	public String toString() {
		String str = "Status : " + Controller.gameStatus + "\n";
		str += "white in check : " + Controller.whiteInCheck + "\n";
		str += "black in check : " + Controller.blackInCheck + "\n";
		str += "toMove : " + Controller.toMove + "\n";
		str += "black trap king status " + Controller.blackTrapped + "\n";
		str += "white trap king status " + Controller.whiteTrapped + "\n";
		
		return str;		
	}
	
	public static Player getCurrentPlayer() {
		return Controller.toMove;
	}
	
	public static Player getWinner() {
		switch(checkStatus()){
			case WHITE_WIN : return Player.WHITE;
			case BLACK_WIN : return Player.BLACK;
			default : return null;
		}
	}
	
	@Deprecated
	public static void registerPieces() {
		Map map = AppRegistry.getMap();
		Position crawler;
		
		for(int row = 0; row < 7; row++) {
			for(int col = 0; col < 7; col++) {
				
				crawler = AppRegistry.getConstPosition(row,col);
				BitTranslator.registerPiece(map.getPieceAt(crawler));
				
			}
		}
	}
	
	
	public static void main(String[] args) {

		
		Executor.setChangesVisible(false);
		
		long start = System.currentTimeMillis();
		for(int i = 0; i < 100000; i++) {
			while(!Executor.finishedExecuting()) {
				
				Controller.executeTurnActions();
				
			}
		
			while(!Executor.finishedUndoing()) {
				Controller.undoTurnActions();
			}
		}
		
		System.out.println("finished test after " + (System.currentTimeMillis() - start) + " miliseconds");
	}

}
