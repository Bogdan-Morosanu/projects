package controller;

import pieces.Pawn;
import pieces.Piece;
import board.Map;
import board.Position;

//TODO refactor PromotionMoveDispatcher to use this class as well
public class PromotionMove extends Move {
	
	private Class<? extends Piece> choice;
	
	public PromotionMove(Position from, Position to, Class<? extends Piece> promotedTo) {
		super(from, to);
		this.choice = promotedTo;
	}

	@Override
	public boolean execute() throws NotYourPieceException, ExposedYourSelfToCheckException {
		boolean retVal = super.execute();
		Map map = AppRegistry.getMap();
		Pawn pawnPromoted = (Pawn)map.getPieceAt(to);
		
		Controller.doPawnPromotion(choice,pawnPromoted);
		return retVal;
	}
	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
