/**
 * 
 */
package uk.ac.ic.kyoto.util.sim.jsonobjects.simulations;

/**
 * @author farhanrahman
 *
 */
public class Parameters {
	private Integer finishTime;
	private String testkey;
	private Long worldemision;
	public Integer getFinishTime(){return finishTime;}
	public void setFinishTime(Integer finishTime){this.finishTime = finishTime;}
	public String getTestkey(){return testkey;}
	public void setTestkey(String testkey){this.testkey = testkey;}
	public Long getWorldemission(){return worldemision;}
	public void setWorldemission(Long worldemision){this.worldemision = worldemision;}
	
	public String toString(){
		String s = "";
		s += " {finishTime: " + finishTime;
		s += " ,testkey: " + testkey;
		s += " ,worldemission: " + worldemision + "}";
		return s;
	}
}
