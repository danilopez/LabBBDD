
import java.io.File;
import java.sql.*;
import java.util.*;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Conector {
    private String direccion;
    private String usuario;
    private String password;
    private Connection conn;
    
    static {
	try {
	    DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());
	} catch (SQLException ex) {
	    throw new RuntimeException(ex);
	}
    }
    
    public  Conector(String direccion, String usuario, String password) throws SQLException {
	this.direccion = direccion;
	this.usuario = usuario;
	this.password = password;
	this.conn = DriverManager.getConnection(this.direccion,this.usuario,this.password);
    }
    
    public synchronized List<List<Object>> consulta(String sql) throws SQLException {
	List<List<Object>> principal = new ArrayList<List<Object>>();
	
	// Creamos y lanzamos una sentencia.
        Statement stmt = conn.createStatement();
        ResultSet rset = stmt.executeQuery(sql);
	
	// Procesamos el resutlado
	int numColumnas = rset.getMetaData().getColumnCount();
	
	while (rset.next()) {
	    List<Object> tupla = new ArrayList<Object>();
	    for (int i = 1; i <= numColumnas; i++) {
		tupla.add(rset.getObject(i));
	    }
	    principal.add(tupla);
	}
	

	// Cerramos la sentencia
	rset.close();
        stmt.close();
	
	return principal;
    }
    
    public static void main(String [] args) throws SQLException {
        Conector c = new Conector("jdbc:oracle:thin:@olimpia.lcc.uma.es:1521:edgar", "LBD12_76638576", "4238");
        /*	List<List<Object>> resultado = c.consulta("SELECT * FROM USER_TABLES");
		Object o = resultado.get(0).get(0);
		System.out.println(o.getClass());*/
        try {
        	DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        	File file = new File("file.xml");
        	Document doc = docBuilder.parse(file);
        	doc.getDocumentElement().normalize();
        	System.out.println("Root element: " + doc.getDocumentElement().getNodeName());
        	
        	NodeList tableNodeList = doc.getElementsByTagName("table");
        	for (int i = 0; i < tableNodeList.getLength(); i++) {
        		// Recorremos la lista de nodos "table" que hayamos encontrado
        		Node tableNode = tableNodeList.item(i);
        		// Creamos un nuevo nodo con el elemento a obtener datos
        		if (tableNode.getNodeType() == Node.ELEMENT_NODE) {
        			// Confirmamos que el nodo es un elemento simple
        			Element nameElement = (Element) tableNode;
        			// Transformamos el nodo en elementos
        			System.out.println("Table name: " + getTagValue("name",nameElement));
        		}
        		XPath xpath = XPathFactory.newInstance().newXPath();
        		NodeList nodeListColumn = (NodeList) xpath.evaluate("column/name",tableNode,XPathConstants.NODESET);
        		for (int j = 0; j < nodeListColumn.getLength(); j++) {
        			Node columnNode = nodeListColumn.item(j);
        			if (columnNode.getNodeType() == Node.ELEMENT_NODE) {
        				Element el = (Element) columnNode;
        				System.out.println("\tColumn name: " + el.getChildNodes().item(0).getNodeValue());
        			}
        		}
        		
        	}
        } catch (Exception e) {
        	e.printStackTrace();	
        }
    }
    
    private static String getTagValue(String stringTag, Element element) {
    	NodeList nodeList = element.getElementsByTagName(stringTag).item(0).getChildNodes();
    	Node nodeValue = (Node) nodeList.item(0);
    	
    	return nodeValue.getNodeValue();
    }
}
