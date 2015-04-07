package views;

import javax.swing.ImageIcon;
import javax.swing.JButton;

/**
 * JBUtton that knows it's place [row;col] on chess board
 * @author moro
 *
 */
public class ChessButton extends JButton {

	public final int row;
	public final int col;
	
	public ChessButton(String txt, int row, int col) {
		super(txt);
		this.row = row;
		this.col = col;
	}
	
	public ChessButton(ImageIcon icon, int row, int col) {
		super(icon);
		this.row = row;
		this.col = col;
		
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
