package views;


import java.util.ArrayList;

import javax.swing.ImageIcon;

import pieces.Bishop;
import pieces.King;
import pieces.Knight;
import pieces.Pawn;
import pieces.Piece;
import pieces.Queen;
import pieces.Rook;

/**
 * Registry Object Holding Icons
 * @author moro
 *
 */
public class IconFactory {

	private static ImageIcon[] icons = new ImageIcon[12];
	private static final String STD_ICON_PATH = "./icons/";
	
	static {
		
		icons[0] = new ImageIcon(STD_ICON_PATH + "wp.png"); //white pawn
		icons[1] = new ImageIcon(STD_ICON_PATH + "wk.png"); //white king
		icons[2] = new ImageIcon(STD_ICON_PATH + "wn.png"); //white knight
		icons[3] = new ImageIcon(STD_ICON_PATH + "wb.png"); //white bishop
		icons[4] = new ImageIcon(STD_ICON_PATH + "wq.png"); //white queen
		icons[5] = new ImageIcon(STD_ICON_PATH + "wr.png"); //white rook
		
		icons[6] = new ImageIcon(STD_ICON_PATH + "bp.png"); //black pawn
		icons[7] = new ImageIcon(STD_ICON_PATH + "bk.png"); //black king
		icons[8] = new ImageIcon(STD_ICON_PATH + "bn.png"); //black knight
		icons[9] = new ImageIcon(STD_ICON_PATH + "bb.png"); //black bishop	
		icons[10] = new ImageIcon(STD_ICON_PATH + "bq.png"); //black queen
		icons[11] = new ImageIcon(STD_ICON_PATH + "br.png"); //black rook
		
	}
	
	public static ImageIcon getIcon(Piece p) {
		if( p == null ) return null;
		
		if( p.getColor() == Piece.Color.WHITE ) {
			
			if(p instanceof Pawn) return icons[0];
			if(p instanceof King) return icons[1];
			if(p instanceof Knight) return icons[2];
			if(p instanceof Bishop) return icons[3];
			if(p instanceof Queen) return icons[4];
			
			return icons[5]; //white rook
			
		} else {

			if(p instanceof Pawn) return icons[6];
			if(p instanceof King) return icons[7];
			if(p instanceof Knight) return icons[8];
			if(p instanceof Bishop) return icons[9];
			if(p instanceof Queen) return icons[10];
			
			return icons[11]; //black rook
			
		}
	}
	
	public static ArrayList<ImageIcon> getPromotionSet(Piece.Color color) {
		ArrayList<ImageIcon> list = new ArrayList<ImageIcon>();
		if(color == Piece.Color.WHITE) {
			//icons[0] is white pawn icons[1] is white king
			for(int i = 2; i < 6; i++) {
				
				list.add(icons[i]);
			}
		} else {
			//icons[6] is black pawn icons[7] is black king
			for(int i = 8; i < 12; i++) {
				list.add(icons[i]);
			}
		}
		
		return list;
	}
	
	public static Class<? extends Piece> classFromIcon(ImageIcon icon) {
		for(int i = 0; i < 12; i++) {
			//can only get icon objects from icon factory
			//so our object must be one of the icons
			if(icon == icons[i]) {
				return classValue(i);
			}
		}
		
		return null; //can't get here
	}
	
	private static Class<? extends Piece> classValue(int i) {
		switch(i) {
			case 0 :
			case 6 : //Pawns 0 white, 6 black
				return Pawn.class;
			case 1 :
			case 7 : 
				return King.class;
			case 2 : 
			case 8 : 
				return Knight.class;
			case 3 :
			case 9 :
				return Bishop.class;
			case 4 :
			case 10 :
				return Queen.class;
			default :
				return Rook.class;
		}
	}
	
}
