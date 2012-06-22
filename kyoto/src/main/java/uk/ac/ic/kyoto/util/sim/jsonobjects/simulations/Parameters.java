/**
 * 
 */
package uk.ac.ic.kyoto.util.sim.jsonobjects.simulations;

/**
 * @author farhanrahman
 *
 */
public class Parameters {
	private String finishTime;
	private String testkey;
	private String worldemision;
	public String getFinishTime(){return finishTime;}
	public void setFinishTime(String finishTime){this.finishTime = finishTime;}
	public String getTestkey(){return testkey;}
	public void setTestkey(String testkey){this.testkey = testkey;}
	public String getWorldemission(){return worldemision;}
	public void setWorldemission(String worldemision){this.worldemision = worldemision;}
	
	public String toString(){
		String s = "";
		s += "{ \"finishTime\" : " + "\"" + finishTime + "\"";
		s += " , \"testkey\" : " + "\"" + testkey + "\"";
		s += " , \"worldemision\" : " + "\"" + worldemision + "\"}";
		return s;
	}
}
