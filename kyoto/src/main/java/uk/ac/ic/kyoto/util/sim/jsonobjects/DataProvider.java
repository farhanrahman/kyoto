/**
 * 
 */
package uk.ac.ic.kyoto.util.sim.jsonobjects;

import java.util.List;

import uk.ac.ic.kyoto.util.sim.jsonobjects.simulations.SimulationData;
import uk.ac.ic.kyoto.util.sim.mongo.MongoConnector;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;

/**
 * @author farhanrahman
 *
 */
public class DataProvider {

	/**
	 * 
	 */
	
	private MongoConnector mongoConnector;
	
	public DataProvider() {
		this.mongoConnector = new MongoConnector();
	}
	
	public DataProvider(String host, String port, String dbName, String username, String password){
		this.mongoConnector = new MongoConnector(host,port,dbName,username,password);
	}		
	
	public DataProvider(String host, String port, String dbName) {
		this.mongoConnector = new MongoConnector(host,port,dbName);
	}
	
	/**
	 * Gets a list of DBObjects from
	 * the mongodb, which reads from
	 * collection "simulations". The list
	 * from the mongoConnector is then
	 * converted to a Java Object and returned
	 * to the caller.
	 * @param simID
	 * @return
	 */
	public JSONObjectContainer<SimulationData> getSimulationData(Integer simID){
		
		JSONObjectContainer<SimulationData> o = new JSONObjectContainer<SimulationData>();
		
		List<DBObject> jasonObjects = mongoConnector.getDBObjects(simID);

		if(jasonObjects != null && jasonObjects.size() > 0){
			DBObject ob = jasonObjects.get(0);
        	Gson gson = new GsonBuilder().create();
        	String json = JSON.serialize(ob);
        	json = json.replaceAll("\"\"", "0");
			SimulationData data = gson.fromJson(json, SimulationData.class);
			o.setObject(data);			
		}
        
		return o;
	}	

}
