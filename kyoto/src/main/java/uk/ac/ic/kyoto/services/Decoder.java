/**
 * 
 */
package uk.ac.ic.kyoto.services;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Provides services to countries to
 * 
 * 1) 	Decode a given UUID to a 
 * 		country ISO or country name.
 * 
 * 2)	Decode a given country name
 * 		or ISO to a registered UUID
 * 
 * @author farhanrahman
 *
 */
public class Decoder {

	/*NAME 2 UUID - UUID 2 NAME*/
	private static Map<UUID,String> id2name = new HashMap<UUID,String>();
	private static Map<String,UUID> name2id = new HashMap<String,UUID>();
	
	/*ISO 2 UUID - UUID 2 ISO*/
	private static Map<UUID,String> id2ISO = new HashMap<UUID,String>();
	private static Map<String,UUID> ISO2id = new HashMap<String,UUID>();
	
	/**
	 * Package protected method that lets Simulation class
	 * add to the private maps that belong to the class.
	 * @param id
	 * @param name
	 * @param ISO
	 */
	static synchronized void addCountry(UUID id, String name, String ISO){
		/*Add to maps decoding id-name : name-id*/
		Decoder.id2name.put(id, name);
		Decoder.name2id.put(name, id);
		
		/*Add to maps decoding id-ISO : ISO-id*/
		Decoder.id2ISO.put(id, ISO);
		Decoder.ISO2id.put(name, id);
	}
	
	/**
	 * Takes as input the name of the country
	 * and returns the registered UUID
	 * created at initialisation of simulation.
	 * @param name
	 * @return UUID for country
	 */
	public static UUID getCountryIDForName(String name){
		synchronized(name2id){
			return name2id.get(name);
		}
	}
	
	/**
	 * Takes as input the ISO of the country
	 * and returns the registered UUID created
	 * during initialisation of simulation.
	 * @param ISO
	 * @return UUID for country
	 */
	public static UUID getCountryIDForISO(String ISO){
		synchronized(ISO2id){
			return ISO2id.get(ISO);
		}
	}
	
	/**
	 * Takes as input the UUID of the country
	 * and returns the registered name during
	 * initialisation of the simulation.
	 * @param id
	 * @return
	 */
	public static String getCountryNameForID(UUID id){
		synchronized(id2name){
			return id2name.get(id);
		}
	}
	
	/**
	 * Takes as input the UUID of the country
	 * and returns the registered ISO during
	 * initialisation of the simulation.
	 * @param id
	 * @return
	 */
	public static String getCountryISOForID(UUID id){
		synchronized(id2ISO){
			return id2ISO.get(id);
		}
	}
	
}
