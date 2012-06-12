package uk.ac.ic.kyoto.util.sim.jsonobjects.simulations;


/**
 * 
 * @author farhanrahman
 *
 */
public class CountryData{
	private String Type;
	private String name;	
	private String ISO;
	private String agentName;
	private Long Ratified;
	private Long totalArea;
	private Long landArea;
	private Long waterArea;
	private Long arableLandArea;
	private Long GDP;
	private Long GDPRate;
	private Long availableToSpend;
	private Long emissionsTarget;
	private Long carbonOffset;
	private Long energyOutput;
	private Long carbonOutput;
	
	public String toString(){
		String s = "";
		s += " {ISO: " + ISO;
		s += " ,name: " + name;
		s += " ,agentName: " + agentName;
		s += " ,Type: " + Type;
		s += " ,Ratified: " + Ratified;
		s += " ,totalArea: " + totalArea;
		s += " ,waterArea: " + waterArea;
		s += " ,arableLandArea: " + arableLandArea;
		s += " ,GDP: " + GDP;
		s += " ,GDPRate: " + GDPRate;
		s += " ,landArea: " + landArea;
		s += " ,availableToSpend " + availableToSpend;
		s += " ,emissionsTarget " + emissionsTarget;
		s += " ,carbonOffset " + carbonOffset;
		s += " ,energyOutput " + energyOutput;
		s += " ,carbonOutput " + carbonOutput + "}";
		return s;
		
	}
	
	public String getName(){return name;}
	public void setName(String name) {this.name = name;}
	public String getISO(){return ISO;}
	public void setISO(String iSO){ISO = iSO;}
	public String getAgentName(){return agentName;}
	public void setAgentName(String agentName){this.agentName = agentName;}
	public Long getRatified(){return Ratified;}
	public void setRatified(Long ratified){Ratified = ratified;}
	public Long getTotalArea(){return totalArea;}
	public void setTotalArea(Long totalArea){this.totalArea = totalArea;}
	public Long getWaterArea(){return waterArea;}
	public void setWaterArea(Long waterArea){this.waterArea = waterArea;}
	public Long getArableLandArea(){return arableLandArea;}
	public void setArableLandArea(Long arableLandArea){this.arableLandArea = arableLandArea;}
	public Long getGDP(){return GDP;}
	public void setGDP(Long gDP){GDP = gDP;}
	public Long getGDPRate(){return GDPRate;}
	public void setGDPRate(Long gDPRate){GDPRate = gDPRate;}
	public Long getAvailableToSpend(){return availableToSpend;}
	public void setAvailableToSpend(Long availableToSpend){this.availableToSpend = availableToSpend;}
	public Long getEmissionsTarget(){return emissionsTarget;}
	public void setEmissionsTarget(Long emissionsTarget){this.emissionsTarget = emissionsTarget;}
	public Long getCarbonOffset(){return carbonOffset;}
	public void setCarbonOffset(Long carbonOffset){this.carbonOffset = carbonOffset;}
	public Long getEnergyOutput(){return energyOutput;}
	public void setEnergyOutput(Long energyOutput){this.energyOutput = energyOutput;}
	public Long getCarbonOutput(){return carbonOutput;}
	public void setCarbonOutput(Long carbonOutput){this.carbonOutput = carbonOutput;}
	public String getType(){return Type;}
	public void setType(String type){this.Type = type;}
	public Long getLandArea(){return landArea;}
	public void setLandArea(Long landArea){this.landArea = landArea;}
	
}