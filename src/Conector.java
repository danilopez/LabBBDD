
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.*;
import uma.ConnectionManager;

public class Conector {
	ConnectionManager connManager;
	Connection conn;
	
    public Conector() throws SQLException {
		try {
			connManager = new ConnectionManager();
			conn = connManager.connect();
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    public void execute(String s) throws SQLException {
    	Statement stmt = this.conn.createStatement();
    	stmt.executeUpdate(s);
    	stmt.close();
    }
    

}
