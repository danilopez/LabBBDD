import java.io.File;
import java.sql.SQLException;
import java.util.StringTokenizer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XmlParser {
	private Conector conector;
	private Document doc;

	public XmlParser(File file) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setValidating(false);
        factory.setNamespaceAware(true);

        SchemaFactory schemaFactory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");

        factory.setSchema(schemaFactory.newSchema(
            new Source[] {new StreamSource("dbtoxml.xsd")}));

		DocumentBuilder docBuilder =factory.newDocumentBuilder();
		doc = docBuilder.parse(file);
		doc.getDocumentElement().normalize();
		conector = new Conector();
	}

	public void createDB() throws XPathExpressionException, SQLException {
		NodeList tableNodeList = doc.getElementsByTagName("table");
		for (int i = 0; i < tableNodeList.getLength(); i++) {
			// Recorremos la lista de nodos "table" que hayamos encontrado
			Node tableNode = tableNodeList.item(i);
			createTable(tableNode);
		}
		// Tablas creadas
		// Creamos las constraints
		NodeList constList = doc.getElementsByTagName("constraint");

		parseConstraints(constList);
	}

	public void createTable(Node tableNode) throws XPathExpressionException,
			SQLException {
		String tableName = "";
		if (tableNode.getNodeType() == Node.ELEMENT_NODE) {
			Element nameElement = (Element) tableNode;
			tableName = getTagValue("name", nameElement);
		}
		String sql = "create table " + tableName + " ( "
				+ getColumnsString(tableNode) + " )";
		System.out.println(sql);
		try {
			conector.execute(sql);
		} catch (SQLException e) {
			if (e.getErrorCode() == 955) {
				System.out
						.println("ERROR: La tabla "
								+ tableName
								+ " ya estaba creada. Se procede a borrarla y a volver a crearla");
				conector.execute("drop table " + tableName
						+ " cascade constraints");
				conector.execute(sql);
			} else {
				throw new SQLException(e);
			}
		}
	}

	private static String getColumnsString(Node tableNode) throws XPathExpressionException {
		String result = "";
		XPath xpath = XPathFactory.newInstance().newXPath();
		NodeList nodeListColumn = (NodeList) xpath.evaluate("column",
				tableNode, XPathConstants.NODESET);
		for (int j = 0; j < nodeListColumn.getLength(); j++) {
			Node columnNode = nodeListColumn.item(j);
			if (columnNode.getNodeType() == Node.ELEMENT_NODE) {
				Element columnElement = (Element) columnNode;
				result = result.concat(getTagValue("name", columnElement) + " "
						+ getTagValue("data_type", columnElement));
				if (getTagValue("nullable",columnElement).equals("N")) {
					result = result.concat(" NOT NULL ");
				}
				result = result.concat(", ");
			}
		}
		return result.substring(0, result.length() - 2);
	}

	private void parseConstraints(NodeList constList) throws SQLException {
		String tableName = "", constraintName = "", constraintType = "", sqlU = "", sqlPK = "", sqlFK = "", sqlC = "";
		for (int temp = 0; temp < constList.getLength(); temp++) {
			Node constNode = constList.item(temp);

			Node parentNode = constNode.getParentNode();
			if (parentNode.getNodeType() == Node.ELEMENT_NODE) {
				Element tempElement = (Element) parentNode;
				tableName = getTagValue("name", tempElement);
			}

			if (constNode.getNodeType() == Node.ELEMENT_NODE) {
				Element element = (Element) constNode;
				constraintName = element.getAttribute("idcons");
				constraintType = getTagValue("type", element);

				if (constraintType.equals("U")) {
					NodeList uniqueList = element.getElementsByTagName("unique_members");
					
					for (int i = 0; i < uniqueList.getLength(); i++) {
						Node uniqueNode = uniqueList.item(i);

						if (uniqueNode.getNodeType() == Node.ELEMENT_NODE) {
							Element uniqueElement = (Element) uniqueNode;
							NodeList columnsList = uniqueElement.getElementsByTagName("column");
							String columns = "";
							for (int j = 0; j < columnsList.getLength(); j++) {
								Node columnNode = columnsList.item(j);
								
								if (columnNode.getNodeType() == Node.ELEMENT_NODE) {
									Element columnElement = (Element) columnNode;
									columns = columns.concat(getTagValue("name", columnElement) + ", ");
								}
							}
							columns = columns.substring(0, columns.length() - 2);
							sqlU = sqlU.concat("ALTER TABLE " 
									+ tableName
									+ " ADD CONSTRAINT " 
									+ constraintName
									+ " UNIQUE (" 
									+ columns 
									+ ");");
						}
					}
				} else if (constraintType.equals("P")) {
					NodeList pkList = element.getElementsByTagName("pk_members");
					
					for (int i = 0; i < pkList.getLength(); i++) {
						Node pkNode = pkList.item(i);
						if (pkNode.getNodeType() == Node.ELEMENT_NODE) {
							Element pkElement = (Element) pkNode;
							NodeList columnsList = pkElement.getElementsByTagName("column");
							String columns = "";
							for (int j = 0; j < columnsList.getLength(); j++) {
								Node columnNode = columnsList.item(j);
								if (columnNode.getNodeType() == Node.ELEMENT_NODE) {
									Element columnElement = (Element) columnNode;
									columns = columns.concat(getTagValue("name", columnElement) + ", ");
								}
							}
							columns = columns.substring(0, columns.length() - 2);
							sqlPK = sqlPK.concat("ALTER TABLE " 
									+ tableName
									+ " ADD CONSTRAINT " 
									+ constraintName
									+ " PRIMARY KEY (" 
									+ columns 
									+ ");");
						}
					}

				} else if (constraintType.equals("R")) {
					NodeList fkList = element.getElementsByTagName("fk_members");
					String columns = "", targetTable = "";
					
					for (int i = 0; i < fkList.getLength(); i++) {
						Node fkNode = fkList.item(i);
						if (fkNode.getNodeType() == Node.ELEMENT_NODE) {
							Element fkElement = (Element) fkNode;
							NodeList columnsList = fkElement.getElementsByTagName("column");
							for (int j = 0; j < columnsList.getLength(); j++) {
								Node columnNode = columnsList.item(j);
								if (columnNode.getNodeType() == Node.ELEMENT_NODE) {
									Element columnElement = (Element) columnNode;
									columns = columns.concat(getTagValue("name", columnElement) + ", ");
								}
							}
							columns = columns.substring(0, columns.length() - 2);
							NodeList refList = fkElement.getElementsByTagName("references");
							Node refNode = refList.item(0);
							if (refNode.getNodeType() == Node.ELEMENT_NODE) {
								Element refElement = (Element) refNode;
								targetTable = getTagValue("ref_table_name",refElement);
							}
						}
						sqlFK = sqlFK.concat("ALTER TABLE "
								+ tableName
								+ " ADD CONSTRAINT "
								+ constraintName
								+ " FOREIGN KEY ( "
								+ columns
								+ " ) REFERENCES "
								+ targetTable
								+ ";");
					}
				} else if (constraintType.equals("C")) {
					sqlC = sqlC.concat("ALTER TABLE "
							+ tableName
							+ " ADD CONSTRAINT "
							+ constraintName
							+ " CHECK ( "
							+ getTagValue("search_condition",element)
							+ " );");
				}
			}

		}
		StringTokenizer stU, stPK, stFK, stC;
		
		stU = new StringTokenizer(sqlU,";");
		while(stU.hasMoreTokens()) {
			String temp = stU.nextToken();
			System.out.println(temp);
			conector.execute(temp);	
		}
		
		stPK = new StringTokenizer(sqlPK,";");
		while (stPK.hasMoreElements()) {
			String temp = stPK.nextToken();
			System.out.println(temp);
			conector.execute(temp);
		}
		
		stFK = new StringTokenizer(sqlFK,";");
		while(stFK.hasMoreTokens()) {
			String temp = stFK.nextToken();
			System.out.println(temp);
			conector.execute(temp);
		}
		
		stC = new StringTokenizer(sqlC,";");
		while(stC.hasMoreTokens()) {
			String temp = stC.nextToken();
			System.out.println(temp);
			conector.execute(temp);
		}
	}

	public static String getTagValue(String stringTag, Element element) {
		NodeList nodeList = element.getElementsByTagName(stringTag).item(0).getChildNodes();
		Node nodeValue = nodeList.item(0);

		return nodeValue.getNodeValue();
	}

}
