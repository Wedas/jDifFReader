package main;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.xml.bind.DatatypeConverter;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

public class EngineFB2 {
	public static void openFB2(File file, JTextPane textPane, int caretPosition) throws XMLStreamException, IOException, BadLocationException {
		XMLInputFactory inputFactory = XMLInputFactory.newInstance();
		InputStream inStrXML = new FileInputStream(file);
		parseCoverPage(inStrXML, textPane);
		inStrXML.close();
		XMLEventReader evtReader = inputFactory.createXMLEventReader(
				new FileInputStream(file));	
		while(evtReader.hasNext()){
			XMLEvent event = evtReader.nextEvent();				
			if(event.isStartElement()&&event.asStartElement().getName().getLocalPart().contains("body")){
				getCharacters(event, evtReader, textPane);	
				evtReader.close();
				break;
			}			
			}
		textPane.setCaretPosition(caretPosition);		
		}

	private static void parseCoverPage(InputStream inStrXML, JTextPane textPane) throws XMLStreamException, IOException {
		XMLInputFactory inputFactory = XMLInputFactory.newInstance();
		XMLEventReader evtReader = inputFactory.createXMLEventReader(inStrXML);	
		while(evtReader.hasNext()){
			XMLEvent imageEvent = evtReader.nextEvent();
			if(imageEvent.isStartElement()&&imageEvent.asStartElement().getName().getLocalPart().equalsIgnoreCase("coverpage")){
				while(evtReader.hasNext()){
					imageEvent = evtReader.nextEvent();
					if(imageEvent.isStartElement()&&imageEvent.asStartElement().getName().getLocalPart().equalsIgnoreCase("binary")){
						String evtName = imageEvent.asStartElement().getName().getLocalPart();
						String imageString = "";
						while(evtReader.hasNext()){
							imageEvent = evtReader.nextEvent();
							if(imageEvent.isEndElement()
									&&imageEvent.asEndElement().getName().getLocalPart().equalsIgnoreCase(evtName)) break;			
							if(imageEvent.isCharacters()) imageString+=	imageEvent.asCharacters().getData();		
						}
						byte[] imageArray = DatatypeConverter.parseBase64Binary(imageString);
						BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageArray));
						textPane.insertIcon(new ImageIcon(image));
						break;
					}
				}
				break;
			}
		}
	}

	
	private static void getCharacters(XMLEvent event, XMLEventReader evtReader, JTextPane textPane) throws XMLStreamException, BadLocationException {
		String evtName = event.asStartElement().getName().getLocalPart();
		while(evtReader.hasNext()){
			event = evtReader.nextEvent();
			if(event.isEndElement()&&event.asEndElement().getName().getLocalPart().equalsIgnoreCase(evtName)) break;
			if(event.isStartElement()&&event.asStartElement().getName().getLocalPart().equalsIgnoreCase("id")) {
				event = evtReader.nextEvent();
				continue;
			}
			if(event.isCharacters()&&event.asCharacters().getData().trim().equals("")) continue;	
			if(event.isCharacters()) textPane.getDocument().insertString(
					textPane.getDocument().getLength(), "\n      "+event.asCharacters().getData()+"\n", null);			
		}		
	}

}
