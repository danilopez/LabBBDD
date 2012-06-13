import java.io.File;

import javax.xml.parsers.ParserConfigurationException;

public class Principal {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
    	File file = new File("file.xml");
    	// TODO: validar XML
    	try {
			XmlParser xmlParser = new XmlParser(file);
			xmlParser.createDB();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
