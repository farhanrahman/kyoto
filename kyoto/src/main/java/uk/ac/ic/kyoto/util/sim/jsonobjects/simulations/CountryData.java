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
	private String Ratified;
	private String totalArea;
	private String landArea;
	private String waterArea;
	private String arableLandArea;
	private String GDP;
	private String GDPRate;
	private String availableToSpend;
	private String emissionsTarget;
	private String carbonOffset;
	private String carbonOutput1990;
	private String className;
	private String energyOutput;
	private String carbonOutput;
	
	public String toString(){
		String s = "";
		s += " { \"Type\" : \"" + Type + "\"";
		s += " , \"name\" : \"" + name + "\"";
		s += " , \"ISO\" : \"" + ISO + "\"";
		s += " , \"agentName\" : \"" + agentName + "\"";
		s += " , \"Ratified\" : \"" + Ratified + "\"";
		s += " , \"totalArea\" : \"" + totalArea + "\"";
		s += " , \"landArea\" : \"" + landArea + "\"";
		s += " , \"waterArea\" : \"" + waterArea + "\"";
		s += " , \"arableLandArea\" : \"" + arableLandArea + "\"";
		s += " , \"GDP\" : \"" + GDP + "\"";
		s += " , \"GDPRate\" : \"" + GDPRate + "\"";
		s += " , \"availableToSpend\" : \"" + availableToSpend + "\"";
		s += " , \"emissionsTarget\" : \"" + emissionsTarget + "\"";
		s += " , \"carbonOffset\" : \"" + carbonOffset + "\"";
		s += " , \"carbonOutput1990\" : \"" + carbonOutput1990 + "\"";
		s += " , \"className\" : \"" + className + "\"";
		s += " , \"energyOutput\" : \"" + energyOutput + "\"";
		s += " , \"carbonOutput\" : \"" + carbonOutput + "\"" + "}";
		return s;
		
	}
	
	public String getName(){return name;}
	public void setName(String name) {this.name = name;}
	public String getISO(){return ISO;}
	public void setISO(String iSO){ISO = iSO;}
	public String getAgentName(){return agentName;}
	public void setAgentName(String agentName){this.agentName = agentName;}
	public String getRatified(){return Ratified;}
	public void setRatified(String ratified){Ratified = ratified;}
	public String getTotalArea(){return totalArea;}
	public void setTotalArea(String totalArea){this.totalArea = totalArea;}
	public String getWaterArea(){return waterArea;}
	public void setWaterArea(String waterArea){this.waterArea = waterArea;}
	public String getArableLandArea(){return arableLandArea;}
	public void setArableLandArea(String arableLandArea){this.arableLandArea = arableLandArea;}
	public String getGDP(){return GDP;}
	public void setGDP(String gDP){GDP = gDP;}
	public String getGDPRate(){return GDPRate;}
	public void setGDPRate(String gDPRate){GDPRate = gDPRate;}
	public String getAvailableToSpend(){return availableToSpend;}
	public void setAvailableToSpend(String availableToSpend){this.availableToSpend = availableToSpend;}
	public String getEmissionsTarget(){return emissionsTarget;}
	public void setEmissionsTarget(String emissionsTarget){this.emissionsTarget = emissionsTarget;}
	public String getCarbonOffset(){return carbonOffset;}
	public void setCarbonOffset(String carbonOffset){this.carbonOffset = carbonOffset;}
	public String getEnergyOutput(){return energyOutput;}
	public void setEnergyOutput(String energyOutput){this.energyOutput = energyOutput;}
	public String getCarbonOutput(){return carbonOutput;}
	public void setCarbonOutput(String carbonOutput){this.carbonOutput = carbonOutput;}
	public String getType(){return Type;}
	public void setType(String type){this.Type = type;}
	public String getLandArea(){return landArea;}
	public void setLandArea(String landArea){this.landArea = landArea;}
	public String getCarbonOutput1990(){return carbonOutput1990;}
	public void setCarbonOutput1990(String carbonOutput1990){this.carbonOutput1990 = carbonOutput1990;}
	public String getClassName(){return className;}
	public void setClassName(String className){this.className = className;}
	
}