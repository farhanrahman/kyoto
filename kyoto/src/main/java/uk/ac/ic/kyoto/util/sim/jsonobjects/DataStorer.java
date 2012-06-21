/**
 * 
 */
package uk.ac.ic.kyoto.util.sim.jsonobjects;

import uk.ac.ic.kyoto.util.sim.mongo.MongoConnector;

/**
 * @author farhanrahman
 *
 */
public class DataStorer {


	/**
	 * 
	 */
	private static String collectionName = "trades";
	
	private MongoConnector mongoConnector;
	
	public DataStorer() {
		this.mongoConnector = new MongoConnector();
	}
	
	public DataStorer(String host, String port, String dbName, String username, String password){
		this.mongoConnector = new MongoConnector(host,port,dbName,username,password);
	}		
	
	public DataStorer(String host, String port, String dbName) {
		this.mongoConnector = new MongoConnector(host,port,dbName);
	}
	
	/**
	 * Stores into the mongodb a jsonObject
	 * @param jsonObject
	 */
	public void storeTradeData(String jsonObject){
		mongoConnector.storeObject(DataStorer.collectionName, jsonObject);
	}
}
