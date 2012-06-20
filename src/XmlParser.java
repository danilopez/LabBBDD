import java.io.File;
import java.sql.SQLException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
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
		DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance()
				.newDocumentBuilder();
		doc = docBuilder.parse(file);
		doc.getDocumentElement().normalize();
		conector = new Conector(
				"jdbc:oracle:thin:@olimpia.lcc.uma.es:1521:edgar",
				"LBD12_76638576", "4238");
	}

	public void createDB() throws XPathExpressionException, SQLException {
		NodeList tableNodeList = doc.getElementsByTagName("table");
		String tableName;
		for (int i = 0; i < tableNodeList.getLength(); i++) {
			// Recorremos la lista de nodos "table" que hayamos encontrado
			Node tableNode = tableNodeList.item(i);
			createTable(tableNode);
		}
		// Tablas creadas
		// Creamos las constraints
		NodeList constList = doc.getElementsByTagName("constraint");

		parseConstraints(constList);

		/*
		 * for (int i = 0; i < tableNodeList.getLength(); i++) { Node tableNode
		 * = tableNodeList.item(i); String tableName = ""; if
		 * (tableNode.getNodeType() == Node.ELEMENT_NODE) { Element nameElement
		 * = (Element) tableNode; tableName = getTagValue("name",nameElement); }
		 * //XPath xpath = XPathFactory.newInstance().newXPath(); //NodeList
		 * constraintList = (NodeList)
		 * xpath.evaluate("constraint",tableNode,XPathConstants.NODESET);
		 * //parseConstraints(constraintList,tableName); }
		 */
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
						.println("La tabla "
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
						+ getTagValue("data_type", columnElement) + ", ");
			}
		}
		return result.substring(0, result.length() - 2);
	}

	/*
	 * private void parseConstraints(NodeList nodeList, String tableName) throws
	 * XPathExpressionException, SQLException { XPath xpath =
	 * XPathFactory.newInstance().newXPath(); String constraintName = ""; String
	 * constraintType = "";
	 * 
	 * for (int j = 0; j < nodeList.getLength(); j++) { Node constraintNode =
	 * nodeList.item(j); if (constraintNode.getNodeType() == Node.ELEMENT_NODE)
	 * { Element constraintElement = (Element) constraintNode; constraintName =
	 * constraintElement.getAttribute("idcons"); constraintType =
	 * getTagValue("type",constraintElement); if (constraintType.equals("U")) {
	 * // Unique constraint String sql = ""; String columns = ""; NodeList
	 * uniqueList = (NodeList)
	 * xpath.evaluate("unique_members",constraintNode,XPathConstants.NODESET);
	 * 
	 * for (int i = 0; i < uniqueList.getLength(); i++) { Node uniqueNode =
	 * uniqueList.item(i); if (uniqueNode.getNodeType() == Node.ELEMENT_NODE) {
	 * Element uniqueElement = (Element) uniqueNode; columns =
	 * columns.concat(getTagValue("column",uniqueElement) + ", "); } } sql =
	 * "alter table " + tableName + " add constraint " + constraintName +
	 * " unique ( " + columns.substring(0, columns.length()-2) + " )";
	 * conector.execute(sql); } else if (constraintType.equals("P")) { //
	 * Primary key constraint String sql = ""; String columns = ""; NodeList
	 * uniqueList = (NodeList)
	 * xpath.evaluate("pk_members",constraintNode,XPathConstants.NODESET);
	 * 
	 * for (int i = 0; i < uniqueList.getLength(); i++) { Node uniqueNode =
	 * uniqueList.item(i); if (uniqueNode.getNodeType() == Node.ELEMENT_NODE) {
	 * Element uniqueElement = (Element) uniqueNode; columns =
	 * columns.concat(getTagValue("column",uniqueElement) + ", "); } } sql =
	 * "alter table " + tableName + " add constraint " + constraintName +
	 * " primary key ( " + columns.substring(0, columns.length()-2) + " )";
	 * conector.execute(sql); System.out.println(sql); } else if
	 * (constraintType.equals("R")) { // Foreign key constraint String sql = "";
	 * String columnsOrigin = ""; String tableTarget = "";
	 * 
	 * NodeList foreignKeyList = (NodeList)
	 * xpath.evaluate("fk_members/column",constraintNode
	 * ,XPathConstants.NODESET);
	 * 
	 * 
	 * System.out.println("Cantidad de elementos de FK_MEMBERS: " +
	 * foreignKeyList.getLength());
	 * 
	 * for (int i = 0; i < foreignKeyList.getLength(); i++) { Node
	 * foreignKeyNode = foreignKeyList.item(i); if (foreignKeyNode.getNodeType()
	 * == Node.ELEMENT_NODE) { Element foreignKeyElement = (Element)
	 * foreignKeyNode; columnsOrigin =
	 * columnsOrigin.concat(getTagValue("column",foreignKeyElement) + ", "); } }
	 * 
	 * NodeList referencesList = (NodeList)
	 * xpath.evaluate("fk_members/references"
	 * ,constraintNode,XPathConstants.NODESET);
	 * 
	 * Node referenceNode = referencesList.item(0); if
	 * (referenceNode.getNodeType() == Node.ELEMENT_NODE) { Element
	 * referenceElement = (Element) referenceNode; tableTarget =
	 * getTagValue("ref_table_name",referenceElement); }
	 * 
	 * sql = "alter table " + tableName + " add constraint " + constraintName +
	 * " foreign key ( " + columnsOrigin.substring(0, columnsOrigin.length()-2)
	 * + " ) references " + tableTarget; //conector.execute(sql);
	 * System.out.println(sql); } else if (constraintType.equals("C")) { //
	 * Check constraint } } } }
	 */

	private void parseConstraints(NodeList constList) {
		String tableName = "";
		String constraintName = "";
		String constraintType = "";
		for (int temp = 0; temp < constList.getLength(); temp++) {
			String sql = "";
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
				System.out.println("Constraint " + constraintName + " of type "
						+ constraintType + " from " + tableName + " table");

				if (constraintType.equals("U")) {
					NodeList uniqueList = element.getElementsByTagName("unique_members");
					// Recorremos los elementos que tenga "unique_members"
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
									columns = columns.concat(getTagValue("column", columnElement) + ", ");
									System.out.println("Columna: " + getTagValue("column",columnElement));
								}
							}
							columns = columns.substring(0, columns.length() - 2);
							sql = "ALTER TABLE " 
									+ tableName
									+ " ADD CONSTRAINT " 
									+ constraintName
									+ " UNIQUE (" 
									+ columns 
									+ ")";
							System.out.println(sql);
						}

					}
				} else if (constraintType.equals("P")) {
					NodeList pkList = element.getElementsByTagName("pk_members");
					

				} else if (constraintType.equals("R")) {

				} else if (constraintType.equals("C")) {

				}
			}

		} // end for(constList)
	}

	public static String getTagValue(String stringTag, Element element) {
		NodeList nodeList = element.getElementsByTagName(stringTag).item(0).getChildNodes();
		Node nodeValue = nodeList.item(0);

		return nodeValue.getNodeValue();
	}

}
