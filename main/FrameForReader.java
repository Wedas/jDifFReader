package main;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.StringTokenizer;

import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

public class FrameForReader {
	private static JFrame frame;
	private static JTextPane textPane;
	private static JComboBox <String> fontList;
	private static JComboBox<Integer> textSizeList;
	private static File archiveFile;
	private static int caretPosition=0;
	public FrameForReader(Dimension dim, String font, Integer size, Color textColor, Color background, File file, int caretPos)
			{
		FrameForReader frameReader = new FrameForReader();
		frame.setSize(dim);
		fontList.setSelectedItem(font);
		textSizeList.setSelectedItem(size);
		textPane.setForeground(textColor);
		textPane.setBackground(background);
		archiveFile=file;
		caretPosition=caretPos;
		openFile(archiveFile);				
	};
	public FrameForReader() {
		frame = new JFrame("JDifFReader");		
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		textPane = new JTextPane();
		textPane.setEditable(false);
		JScrollPane scrollForText = new JScrollPane(textPane);
		scrollForText.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrollForText.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

		JPanel settingsPanel = new JPanel();
		settingsPanel.setLayout(new FlowLayout());
		JLabel fontLabel = new JLabel("Font");
		String fonts[] = 
				GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
		fontList = new JComboBox<>(fonts);		
		fontList.addActionListener((event)->{
			String selectedFont = (String)fontList.getSelectedItem();
			Font fontToSet = new Font(selectedFont, Font.PLAIN, textPane.getFont().getSize());
			textPane.setFont(fontToSet);		
		});
		settingsPanel.add(fontLabel);
		settingsPanel.add(fontList);
		Integer[] textSizeArray = {8,10,12,14,16,18,20,22,24,26,28,30,32,34,36,38,40};
		textSizeList = new JComboBox<>(textSizeArray);	
		textSizeList.addActionListener((event)->{
			textPane.setFont(textPane.getFont().deriveFont(Float.valueOf((float)(int)textSizeList.getSelectedItem())));
		});
		JLabel textSizeLabel = new JLabel("Text size");
		settingsPanel.add(textSizeLabel);
		settingsPanel.add(textSizeList);
		JButton textColorButton = new JButton("Text color");
		textColorButton.addActionListener((event)->{
			Color newTextColor = JColorChooser.showDialog(frame, "Choose text color", textPane.getForeground());
			textPane.setForeground(newTextColor);
		});	
		settingsPanel.add(textColorButton);
		JButton backgroundColorButton = new JButton("Background color");
		backgroundColorButton.addActionListener((event)->{
			Color newTextColor = JColorChooser.showDialog(frame, "Choose background color", textPane.getBackground());
			textPane.setBackground(newTextColor);
		});	
		settingsPanel.add(backgroundColorButton);
		JButton saveSettings = new JButton("Save settings");
		saveSettings.addActionListener((event)->{
			try(ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(new File("JDifFReader.settings")))) 
			{
				oos.writeObject(frame.getSize());
				oos.writeObject(fontList.getSelectedItem());
				oos.writeObject(textSizeList.getSelectedItem());
				oos.writeObject(textPane.getForeground());
				oos.writeObject(textPane.getBackground());
				oos.writeObject(archiveFile);
				oos.writeInt(textPane.getCaretPosition());
				oos.flush();
				oos.close();
			} catch (IOException e) {

				e.printStackTrace();
			}
		});
		settingsPanel.add(saveSettings);		

		JMenuBar menuBar = new JMenuBar();
		JMenu openMenu = new JMenu("File");
		JMenuItem openFile = new JMenuItem("Open");
		openFile.addActionListener((event)->{
			JFileChooser fileChooser = new JFileChooser();
			FileFilter filter = new FileNameExtensionFilter(null, "fb2", "epub");
			fileChooser.setFileFilter(filter);
			if(fileChooser.showOpenDialog(frame)==JFileChooser.APPROVE_OPTION){
				archiveFile = fileChooser.getSelectedFile();
				caretPosition = 0;
				openFile(archiveFile);	
			}		
		});
		openMenu.add(openFile);
		menuBar.add(openMenu);
		JMenu about = new JMenu("About");
		JMenuItem help = new JMenuItem("?");
		help.addActionListener((event)->{
			JTextArea aboutText = new JTextArea();			
			aboutText.setEditable(false);
			aboutText.setText("JDifFReader reads EPUB, FB2 files."
					+ "Pressing \"Save settings\" button will save current font, dimension, colors, text and text position so that "
					+ "next time you open it the environment will be the same");
			aboutText.setPreferredSize(new Dimension(300, 100));			
			aboutText.setLineWrap(true);
			aboutText.setWrapStyleWord(true);
			aboutText.setBackground(help.getBackground());
			JOptionPane.showMessageDialog(frame, aboutText);
		});
		about.add(help);
		menuBar.add(about);
		frame.setJMenuBar(menuBar);

		frame.getContentPane().add(settingsPanel, BorderLayout.NORTH);	
		frame.getContentPane().add(scrollForText, BorderLayout.CENTER);
		frame.setPreferredSize(Toolkit.getDefaultToolkit().getScreenSize());
		frame.setVisible(true);
		frame.pack();
	}
	private static void openFile(File file) {
		if(file!=null){
			try {
				textPane.setText("");
				StringTokenizer fileName = new StringTokenizer(file.getName(),".");
				String extension = "";
				while(fileName.hasMoreTokens()) extension=fileName.nextToken();
				if(extension.equalsIgnoreCase("epub")) EngineEPUB.openEPUBBook(file, textPane, caretPosition);
				if(extension.equalsIgnoreCase("fb2")) EngineFB2.openFB2(file, textPane, caretPosition);				
			} catch (Exception e) {				
				textPane.setText(e.getMessage());
			}
		}	
	}
}
