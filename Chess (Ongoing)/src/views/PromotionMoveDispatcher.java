package views;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.ImageIcon;
import javax.swing.JButton;

import pieces.Piece;
import board.Position;
import controller.AppRegistry;
import controller.Controller;

/**
 * Handles pawn promotion dispatch to controller
 * @author moro
 *
 */
public class PromotionMoveDispatcher extends MouseAdapter {
	
	public void mouseReleased(MouseEvent e) {
		if(e.getButton() == MouseEvent.BUTTON1) {
			JButton choiceBtn = (JButton)e.getComponent();
			ImageIcon choiceImg = (ImageIcon)choiceBtn.getIcon();		
			Class<? extends Piece> chosen = IconFactory.classFromIcon(choiceImg);
			Controller.doPawnPromotion(chosen);
			SimpleBoard.instance.refreshGUI();
		}
	}
}
