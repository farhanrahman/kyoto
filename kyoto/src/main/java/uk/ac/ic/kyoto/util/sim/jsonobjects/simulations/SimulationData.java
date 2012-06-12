/**
 * 
 */
package uk.ac.ic.kyoto.util.sim.jsonobjects.simulations;

import java.util.Collection;
import java.util.Map;

/**
 * @author farhanrahman
 *
 */
public class SimulationData {
	private String _id;
	private String name;
	private String classname;
	private String description;
	private String state;
	private Integer finishTime;
	private Long createdAt;
	private Integer currentTime;
	private Integer finishedAt;
	private Parameters parameters;
	private Integer parent;
	private Collection<?> children;
	private Map<String,CountryData> countries;
	
	public String get_id(){return _id;}
	public void set_id(String _id){this._id = _id;}
	public String getName(){return name;}
	public void setName(String name){this.name = name;}
	public String getClassname(){return classname;}
	public void setClassname(String classname){this.classname = classname;}
	public String getDescription(){return description;}
	public void setDescription(String description){this.description = description;}
	public String getState(){return state;}
	public void setState(String state){this.state = state;}
	public Integer getFinishTime(){return finishTime;}
	public void setFinishTime(Integer finishTime){this.finishTime = finishTime;}
	public Long getCreatedAt(){return createdAt;}
	public void setCreatedAt(Long createdAt){this.createdAt = createdAt;}
	public Integer getCurrentTime(){return currentTime;}
	public void setCurrentTime(Integer currentTime){this.currentTime = currentTime;}
	public Integer getFinishedAt(){return finishedAt;}
	public void setFinishedAt(Integer finishedAt){this.finishedAt = finishedAt;}
	public Parameters getParameters(){return parameters;}
	public void setParameters(Parameters parameters){this.parameters = parameters;}
	public Integer getParent(){return parent;}
	public void setParent(Integer parent){this.parent = parent;}
	public Collection<?> getChildren(){return children;}
	public void setChildren(Collection<?> children){this.children = children;}
	public Map<String, CountryData> getCountries(){return countries;}
	public void setCountries(Map<String, CountryData> countries){this.countries = countries;}
	
	public String toString(){
		String c = "";
		for(String key : countries.keySet()){
			c += countries.get(key).toString();
		}		
		String s = "";
		s += "{ _id:" + _id;
		s += " ,name: " + name;
		s += " ,classname: " + classname;
		s += " ,description: " + description;
		s += " ,state: " + state;
		s += " ,finishTime: " + finishTime;
		s += " ,createdAt: " + createdAt;
		s += " ,parameters: " + parameters.toString();
		s += " ,parent: " + parent;
		s += " ,children: " + children.toString();
		s += " ,countries: " + c;
		s += " }";
		return s;
	}
}
