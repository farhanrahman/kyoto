package uk.ac.ic.kyoto;

import uk.ac.imperial.presage2.rules.facts.Agent;

public class CountryFile {
	
	Agent agent;
	
	private double	emissionTarget;
	private long 	carbonOffset;
	private float 	availableToSpend;
	private long 	carbonTraded;

	public CountryFile(Agent agent, double emissionsTarget, long carbonOffset,
					float availableToSpend, long carbonTraded) {
		
		super();
		this.agent = agent;
		this.emissionTarget = emissionsTarget;
		this.carbonOffset = carbonOffset;
		this.availableToSpend = availableToSpend;
		this.carbonTraded = carbonTraded;		
	}

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
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		CountryFile other = (CountryFile) obj;
		if (agent == null) {
			if (other.agent != null) {
				return false;
			}
		} else if (!agent.equals(other.agent)) {
			return false;
		}
		return true;
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