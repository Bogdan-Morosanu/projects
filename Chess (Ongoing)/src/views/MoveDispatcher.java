package views;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import pieces.Piece;
import board.Position;
import controller.AppRegistry;
import controller.Controller;
import controller.Executor;
import controller.Move;

/**
 * Dispatches Move objects from the view towards the controller layer.
 * @author moro
 *
 */
public class MoveDispatcher extends MouseAdapter {
		
		private static Position from;
		private static Position to;
	
		public void mouseReleased(MouseEvent e) {
			if(e.getButton() == MouseEvent.BUTTON1) {
				
				ChessButton chBtn = (ChessButton)e.getComponent();
				
				
				Position selected = AppRegistry.getMutablePosition(chBtn.row,chBtn.col);
											
				System.out.println("Just selected : " + selected);
				
				if(MoveDispatcher.from == null) {	
					
					Piece selectedPiece = AppRegistry.getMap().getPieceAt(selected);
					if(selectedPiece == null || 
							(selectedPiece.getColor() != Controller.getCurrentPlayer().toPieceColor()) ) {
						//can't move null or other player's pieces.
						//return and cancel
						return;
					}
					
					MoveDispatcher.from = selected; //start move
				} else {
					
					Piece selectedPiece = AppRegistry.getMap().getPieceAt(selected);
					if(selectedPiece != null && selectedPiece.getColor() == Controller.getCurrentPlayer().toPieceColor()) {
						//can't capture own pieces. 
						//take this to assume you want to move another one of your own pieces
						MoveDispatcher.from = selected;
						return;
					}
						
					MoveDispatcher.to = selected; //end move
					MoveDispatcher.dispatchMove();
				}
			}
		}
		
		public static void dispatchMove() {
			Move mv = new Move(from,to);
			from = null;
			to = null;
			
			System.out.println("adding " + mv);
		
			Executor.addMove(mv);
			Controller.executeTurnActions();
			System.out.println(AppRegistry.getStringStatus());

		}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
