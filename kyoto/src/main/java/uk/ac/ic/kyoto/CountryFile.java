package uk.ac.ic.kyoto;

import uk.ac.imperial.presage2.rules.facts.Agent;

/**
 * May no longer be needed. Consult Sam before deletion
 * 
 * @author sam
 */
@Deprecated
public class CountryFile {
	
	Agent agent;
	
	//================================================================================
    // Private fields
    //================================================================================
	
	private double	emissionTarget;
	private long 	carbonOffset;
	private float 	availableToSpend;
	private long 	carbonTraded;
	
	//================================================================================
    // Constructor
    //================================================================================

	public CountryFile(Agent agent, double emissionsTarget, long carbonOffset,
					float availableToSpend, long carbonTraded) {
		
		super();
		this.agent = agent;
		this.emissionTarget = emissionsTarget;
		this.carbonOffset = carbonOffset;
		this.availableToSpend = availableToSpend;
		this.carbonTraded = carbonTraded;		
	}
	
	//================================================================================
    // Getters
    //================================================================================

	public Agent getAgent() {
		return agent;
	}

	public double getEmissionTarget() {
		return emissionTarget;
	}

	public long getCarbonOffset() {
		return carbonOffset;
	}

	public float getAvailableToSpend() {
		return availableToSpend;
	}

	public long getCarbonTraded() {
		return carbonTraded;
	}
	
	//================================================================================
    // Setters
    //================================================================================

	public void setEmissionTarget(double emissionTarget) {
		this.emissionTarget = emissionTarget;
	}

	public void setCarbonOffset(long carbonOffset) {
		this.carbonOffset = carbonOffset;
	}

	public void setAvailableToSpend(float availableToSpend) {
		this.availableToSpend = availableToSpend;
	}

	public void setCarbonTraded(long carbonTraded) {
		this.carbonTraded = carbonTraded;
	}
	
	//================================================================================
    // Overridden Public methods
    //================================================================================
	
	@Override
	public boolean equals(Object obj) {
		if (obj == this){
			return true;
		}
		
		if (!(obj instanceof CountryFile)){
			return false;
		}
		
		CountryFile c = (CountryFile) obj;
		
		return ((this.agent.equals(c.agent)) && (this.availableToSpend == c.availableToSpend)
				&& (this.carbonOffset == c.carbonOffset) && (this.carbonTraded == c.carbonTraded)
				&& (this.emissionTarget == c.emissionTarget));
	}
	
	@Override
	public int hashCode() {
		int result = 635;
		
		result = 3 * result + this.agent.hashCode();
		result = (int) (3 * result + this.availableToSpend);
		result = (int) (3 * result + this.carbonOffset);
		result = (int) (3 * result + this.carbonTraded);
		result = (int) (3 * result + this.emissionTarget);
		
		return result;
	}

	@Override
	public String toString() {
		return "CountryFile [agent=" + agent +
							", emissionTarget=" + emissionTarget +
							", carbonOffset=" + carbonOffset +
							", availableToSpend=" + availableToSpend +
							", carbonTraded=" + carbonTraded + "]";
	}
	
	
}