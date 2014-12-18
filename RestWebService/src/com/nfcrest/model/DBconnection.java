package com.nfcrest.model;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.xml.bind.annotation.XmlRootElement;

import org.codehaus.jettison.json.JSONException;
@XmlRootElement
public class DBconnection {
    /**
     * Method to create DB Connection
     * 
     * @return
     * @throws Exception
     */
    @SuppressWarnings("finally")
    public static Connection createConnection() throws Exception {
        Connection con = null;
        try {
            Class.forName(Constants.dbClass);
            con = DriverManager.getConnection(Constants.dbUrl + Constants.dbName, Constants.dbUser, Constants.dbPwd);
        } catch (Exception e) {
            throw e;
        } finally {
            return con;
        }
    }
    
    public static String get_data(int id) throws SQLException, JSONException
    {
    	Connection dbConn = null;
        String output=null;
            try {
            	
                dbConn = DBconnection.createConnection();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            Statement stmt = dbConn.createStatement();
           
            String query = "SELECT * FROM pinfo where pid = " + id;
            
            ResultSet rs = stmt.executeQuery(query);
            
            //int age;
            while ( rs.next() ) 
            {	
            	output=rs.getString(2);
	           // age  = rs.getInt("age");
	            System.out.println( output );
	            
	            //JSONObject obj= new JSONObject();
	            //obj.put("ID", id);
	            //obj.put("AGE", age);
            	//output= obj.toString();
            }
            rs.close();
            stmt.close();
            dbConn.close();
           
			return output;
        	}
    		
    }
