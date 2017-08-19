/**
 * @author <a href="mailto:vapeinnn@gmail.com">Vape in NN</a> 
 */
package main;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class EngineEPUB {
	private static boolean afterImage=false;
	public static void openEPUBBook(File archiveFile, JTextPane textPane, int caretPosition) throws IOException, ParserConfigurationException, SAXException, DOMException, BadLocationException{
		Node rootFileNode = findNodeInArchive(archiveFile, "container.xml", "rootfile");
		String contentFileName = getPathToRootFile(rootFileNode);
		Node manifestNode = findNodeInArchive(archiveFile, contentFileName, "manifest");
		processManifest(archiveFile, manifestNode, textPane, caretPosition);	
	}

	private static String getPathToRootFile(Node rootFile){
		Node pathNode = rootFile.getAttributes().getNamedItem("full-path");
		return pathNode.getNodeValue();
	}

	private static void processManifest(File archiveFile, Node manifestNode, JTextPane textPane, int caretPosition) 
			throws IOException, ParserConfigurationException, SAXException, DOMException, BadLocationException{

		NodeList manifestList = manifestNode.getChildNodes();
		for(int i =0; i<manifestList.getLength(); i++){
			Node contentNode=manifestList.item(i);
			if(contentNode.getNodeType()==Node.ELEMENT_NODE){
				NamedNodeMap contentNodeList = contentNode.getAttributes();
				for(int j = 0; j<contentNodeList.getLength(); j++){
					if(contentNodeList.item(j).getNodeValue().contains("image/jpeg")){
						String imageFileName = contentNodeList.getNamedItem("href").getNodeValue();
						processImage(archiveFile, imageFileName, textPane);
						afterImage = true;
					}
					if(contentNodeList.item(j).getNodeValue().contains("application/xhtml+xml")){
						String bodyFileName = contentNodeList.getNamedItem("href").getNodeValue();
						Node bodyNode = findNodeInArchive(archiveFile, bodyFileName, "body");					
						NodeList pList = bodyNode.getChildNodes();								
						for (int k=0; k<pList.getLength(); k++){
							processChildNodes(pList.item(k), textPane);
							afterImage=false;
						}					
					}							
				}								
			}
		}
		textPane.setCaretPosition(caretPosition);
	}
	private static void processChildNodes(Node childNode, JTextPane textPane) throws DOMException, BadLocationException {
		NodeList nodeList = childNode.getChildNodes();
		for(int i=0; i<nodeList.getLength(); i++){
			if(nodeList.item(i).hasChildNodes())
				processChildNodes(nodeList.item(i), textPane);
			else {
				if(nodeList.item(i).getTextContent()==null||nodeList.item(i).getTextContent().trim().length()==0) continue;			
				javax.swing.text.Document doc = textPane.getDocument();						
				doc.insertString(doc.getLength(), (afterImage?"\n":"\n      ")+nodeList.item(i).getTextContent(), null);			
			}

		}

	}

	private static void processImage(File zipArchive, String fileName, JTextPane textPane) throws IOException{		
		ZipFile zipFile = new ZipFile(zipArchive);
		Enumeration<? extends ZipEntry> zipContent = zipFile.entries();
		while(zipContent.hasMoreElements()){
			ZipEntry insideZip = zipContent.nextElement();
			if(insideZip.getName().contains(fileName)){	
				BufferedImage image = ImageIO.read(zipFile.getInputStream(insideZip));
				textPane.insertIcon(new ImageIcon(image));
				zipFile.close();
				break;
			}
		}
	}
	private static Node findNodeInArchive(File zipArchive, String fileName, String nodeName) 
			throws IOException, ParserConfigurationException, SAXException{
		Node requiredNode = null;
		ZipFile zipFile = new ZipFile(zipArchive);
		Enumeration<? extends ZipEntry> zipContent = zipFile.entries();
		while(zipContent.hasMoreElements()){
			ZipEntry insideZip = zipContent.nextElement();
			if(insideZip.getName().contains(fileName)){	
				DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();			
				docFactory.setFeature("http://xml.org/sax/features/namespaces", false);
				docFactory.setFeature("http://xml.org/sax/features/validation", false);
				docFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
				docFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
				DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
				InputStream is = zipFile.getInputStream(insideZip);		
				Document unzippedXML = docBuilder.parse(is);				
				NodeList singleList = unzippedXML.getElementsByTagName(nodeName);
				requiredNode = singleList.item(0);				
				is.close();
				zipFile.close();
				break;					
			}
		}
		return requiredNode;			
	}
}
