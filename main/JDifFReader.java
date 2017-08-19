package main;

import java.awt.Color;
import java.awt.Dimension;
import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;

public class JDifFReader {
	public static void main(String[] args) {	
		try(ObjectInputStream ois = new ObjectInputStream(new FileInputStream(new File("JDifFReader.settings"))))
		{
			Dimension dim = (Dimension) ois.readObject();
			String font = (String) ois.readObject();
			Integer size = (Integer) ois.readObject();
			Color textColor = (Color) ois.readObject();
			Color background = (Color) ois.readObject();
			File file = (File) ois.readObject();
			int caretPos = ois.readInt();
			FrameForReader frame = new FrameForReader(dim, font, size, textColor, background, file, caretPos);
		} catch(Exception e){
			FrameForReader frame = new FrameForReader();	
		}

}
}