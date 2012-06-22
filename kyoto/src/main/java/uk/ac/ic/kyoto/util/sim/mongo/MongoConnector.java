/**
 * 
 */
package uk.ac.ic.kyoto.util.sim.mongo;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

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
	
	private boolean SHOULD_AUTHENTICATE = false;
	private final String collectionName = "simulations";
	
	/**
	 * 
	 */
	
	/**
	 * Default constructor. Use this constructor
	 * when you want to use the default port, host
	 * and database as listed above.
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
	
	/**
	 * Constructor that takes in all the parameters to
	 * set the required fields
	 * @param host
	 * @param port
	 * @param dbName
	 * @param username
	 * @param password
	 */
	public MongoConnector(String host, String port, String dbName, String username, String password){
		this.port = Integer.parseInt(port);
		this.host = host;
		this.dbName = dbName;
		this.username = username;
		this.password = password;
		this.SHOULD_AUTHENTICATE = true;		
	}	
	
	/**
	 * 
	 * @param host
	 * @param port
	 * @param dbName
	 */
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
	
	/**
	 * Opens up a connection with the given details
	 * returns true if connection opened successfully
	 * @return
	 */
	private boolean openConnection(){
		try {
			this.m = new Mongo(this.host,this.port);
			return true;
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (MongoException e) {
			e.printStackTrace();
		}
		return false;
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
	
	/**
	 * Gets a list of {@DBObject}s from the
	 * given details for simulation with _id = simID
	 * and collection = colName
	 * @param simID
	 * @param colName
	 * @return
	 */
	public List<DBObject> getDBObjects(Long simID, String colName){
		List<DBObject> list = new ArrayList<DBObject>();

		if(this.openConnection() == false){
			return list;
		}
		
		this.db = m.getDB(this.dbName);
		
		if(this.SHOULD_AUTHENTICATE == true){
			this.authenticate();
		}		
		
		DBCollection collection = db.getCollection(colName);
        BasicDBObject query = new BasicDBObject();
        
        query.put("_id", simID);
        DBCursor cur = collection.find(query);		

        while(cur.hasNext()) {
        	list.add(cur.next());
        }
        
		this.closeConnection();
        
		return Collections.unmodifiableList(list);
	}
	
	/**
	 * Stores a json object into the collection
	 * name provided as an argument.
	 * @param colName
	 * @param jsonObject
	 */
	public void storeObject(String colName, String jsonObject){
		if(this.openConnection() == false){
			return;
		}
		
		this.db = m.getDB(this.dbName);
		
		if(this.SHOULD_AUTHENTICATE == true){
			this.authenticate();
		}		
		
		DBCollection collection = db.getCollection(colName);
        
		DBObject dbObject = (DBObject) JSON.parse(jsonObject);
		
		collection.insert(dbObject);
        
		this.closeConnection();
	}
	
	/**
	 * Uses default collection name "simulations"
	 * @param simID
	 * @return
	 */
	public List<DBObject> getDBObjects(Long simID){
		return this.getDBObjects(simID, this.collectionName);
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
		if(!this.password.equals("")){
			this.SHOULD_AUTHENTICATE = true;
		}else{
			this.SHOULD_AUTHENTICATE = false;
		}
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		if(!this.username.equals("")){
			this.SHOULD_AUTHENTICATE = true;
		}else{
			this.SHOULD_AUTHENTICATE = false;
		}
		this.password = password;
	}

}
