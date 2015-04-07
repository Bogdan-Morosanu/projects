package view;


import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.WindowConstants;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.DocumentFilter;
import javax.swing.text.Element;

import controller.Controller;
public class NonEditableView {
	public static final String PROMPT = ">>> ";
	
	private static JFrame mainFrame;
	private static JButton getScriptButton;
	private static JTextArea promptArea;
	
	public JComponent makeUI() {
		JButton clearNameSpace = new JButton("Clear Name Space");
	
		clearNameSpace.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent evt) {
				Controller.clearNameSpace();
			}
		});
		
		
		getScriptButton = new JButton("Script File");
				
		promptArea = new JTextArea(16,0);
		promptArea.setText(">>> ");
		promptArea.setFont( new Font("Helvetica", Font.BOLD, 14));
		
		
		((AbstractDocument)promptArea.getDocument()).setDocumentFilter(
				new NonEditableLineDocumentFilter());
		
		JPanel mainPannel = new JPanel(new BorderLayout());
		
		JPanel actionBar = new JPanel(new GridLayout(1,4));
		actionBar.add( clearNameSpace );
		actionBar.add( getScriptButton );
		
		mainPannel.add( actionBar, BorderLayout.SOUTH );
		mainPannel.add( new JScrollPane(promptArea), BorderLayout.NORTH);
		
		
		getScriptButton.addActionListener(new ActionListener(){

			final JFileChooser fc = new JFileChooser();
			
			{
				fc.setCurrentDirectory(new File("."));
			}
			
			public void actionPerformed(ActionEvent evt) {
				 if (evt.getSource() == getScriptButton) {	

					int returnVal = fc.showOpenDialog( mainFrame );

			        if (returnVal == JFileChooser.APPROVE_OPTION) {
			            File file = fc.getSelectedFile();
			            
			            String scriptResult = "";
			            try {
			            	scriptResult = Controller.script( file.getAbsolutePath(), Controller.ECHO  );
						
			            } catch (IOException e) {
							scriptResult = e.toString();
						}
			            
			            promptArea.insert( scriptResult, promptArea.getCaretPosition() );
			            
			        } else {
			            //Open command cancelled by user.
			        }
				 } 
			}
		});
		return mainPannel;
	}
	
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			@Override public void run() { createAndShowGUI(); }
		});
	}
	public static void createAndShowGUI() {
		mainFrame = new JFrame();
		
		mainFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		mainFrame.getContentPane().add(new NonEditableView().makeUI());
		mainFrame.setSize(600,340);
		mainFrame.setLocationRelativeTo(null);
		mainFrame.setVisible(true);
	}


	
	public static class NonEditableLineDocumentFilter extends DocumentFilter {

		
		@Override public void insertString(
				DocumentFilter.FilterBypass fb, int offset, String string,
				AttributeSet attr) throws BadLocationException {
			if(string == null) {
				return;
			}else{
				replace(fb, offset, 0, string, attr);
			}
		}
		
		@Override public void remove(
				DocumentFilter.FilterBypass fb, int offset,
				int length) throws BadLocationException {
			replace(fb, offset, length, "", null);
		}
		
		
		@Override public void replace(
				DocumentFilter.FilterBypass fb, int offset, int length,
				String text, AttributeSet attrs) throws BadLocationException {
			
			Document doc = fb.getDocument();
			Element root = doc.getDefaultRootElement();
			
			int count = root.getElementCount();
			int index = root.getElementIndex(offset);
			
			Element cur = root.getElement(index);
			
			int promptPosition = cur.getStartOffset()+PROMPT.length();
			//As Reverend Gonzo says:
			if(index==count-1 && offset-promptPosition>=0) {
				
				if(text.equals("\n")) {
				
					String cmd = doc.getText(promptPosition, offset-promptPosition);
					
					if(cmd.isEmpty()) {
						text = "\n" + PROMPT;
					}else{
						text = Controller.processMessage(cmd) + "\n" + PROMPT;
					}
				}
				fb.replace(offset, length, text, attrs);
			}
		}
	}
}
	


