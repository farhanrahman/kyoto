/**
 * 
 */
package uk.ac.ic.kyoto.util.sim.mongo;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Properties;
import java.util.Set;

import uk.ac.ic.kyoto.util.sim.jsonobjects.simulations.SimulationData;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoException;
import com.mongodb.util.JSON;

/**
 * @author farhanrahman
 *
 */
public class MongoConnector {

	/*DEFAULT PORTS AND HOSTS*/
	private Integer port = 27017;
	private String host = "localhost";
	private String dbName = "presage";
	
	private String username = "";
	private String password = "";
	
	private Mongo m;
	private DB db;
	
	private final boolean SHOULD_AUTHENTICATE;
	
	/**
	 * 
	 */
	
	public MongoConnector(){
		this.SHOULD_AUTHENTICATE = false;
		this.host = this.getHostFromDBProperties();
		try {
			this.m = new Mongo(this.host, this.port);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (MongoException e) {
			e.printStackTrace();
		}
		
	}
	
	public MongoConnector(String host, String port, String dbName, String username, String password){
		this.port = Integer.parseInt(port);
		this.host = host;
		this.dbName = dbName;
		this.username = username;
		this.password = password;
		this.SHOULD_AUTHENTICATE = true;		
	}	
	
	public MongoConnector(String host, String port, String dbName) {
		this.SHOULD_AUTHENTICATE = false;
		this.port = Integer.parseInt(port);
		this.host = host;
		this.dbName = dbName;
	}
	
	/**
	 * Read from db.properties file
	 * and update the host currently
	 * being used.
	 * @return
	 */
	private String getHostFromDBProperties(){
		Properties dbProperty = new Properties();
		try{
			String h = "";
			dbProperty.load(new FileInputStream("src/main/resources/db.properties"));
			h = dbProperty.getProperty("mongo.host");
			return h;
		}catch(IOException e){
			e.printStackTrace();
			return this.host;
		}
	}
	
	private void openConnection(){
		try {
			this.m = new Mongo(this.host,this.port);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (MongoException e) {
			e.printStackTrace();
		}		
	}
	
	private void closeConnection(){
		if(this.m != null){
			this.m.close();
		}
	}
	
	private boolean authenticate(){
		if(this.db != null){
			return db.authenticate(username, this.password.toCharArray());
		}else{
			return false;
		}
	}
	
	public void testConnection(){
		this.openConnection();
		this.db = m.getDB(this.dbName);
		
		if(this.SHOULD_AUTHENTICATE == true){
			this.authenticate();
		}
		/*QUERY AND TEST*/
		
		Set<String> colls = db.getCollectionNames();

		for (String s : colls) {
		    System.out.println(s);
		}
		
		DBCollection collection = db.getCollection("simulations");
        BasicDBObject query = new BasicDBObject();
        
        query.put("_id", 1);
        DBCursor cur = collection.find(query);
        
        while(cur.hasNext()) {
        	DBObject ob = cur.next();
        	Gson gson = new GsonBuilder().create();
        	String json = JSON.serialize(ob);
        	System.out.println(json);
			SimulationData data = gson.fromJson(json, SimulationData.class);
		    
			System.out.println(data.toString());        	
        }
        
		this.closeConnection();
	}
	

	/*======GETTERS AND SETTERS=====*/
	
	public Integer getPort() {
		return port;
	}
	
	public void setPort(Integer port) {
		this.port = port;
	}
	
	public String getHost() {
		return host;
	}
	
	public void setHost(String host) {
		this.host = host;
	}

	public String getDbName() {
		return dbName;
	}

	public void setDbName(String dbName) {
		this.dbName = dbName;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

}
