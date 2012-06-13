
import java.sql.*;

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
    
    public Conector(String direccion, String usuario, String password) throws SQLException {
		this.direccion = direccion;
		this.usuario = usuario;
		this.password = password;
		this.conn = DriverManager.getConnection(this.direccion,this.usuario,this.password);
    }
    
    public void execute(String s) throws SQLException {
    	Statement stmt = this.conn.createStatement();
    	stmt.executeUpdate(s);
    	stmt.close();
    }
    

}
