/**
 * 
 */
package uk.ac.ic.kyoto.util.sim.jsonobjects;

/**
 * Generic container for Java Object
 * parsed from JSON object
 * @author farhanrahman
 *
 */
public class JSONObjectContainer<T> {
	private T object;
	
	public JSONObjectContainer(){
		this.object = null;
	}
	
	public JSONObjectContainer(T object){
		this.setObject(object);
	}

	public T getObject() {
		return object;
	}

	public void setObject(T object) {
		this.object = object;
	}
}
