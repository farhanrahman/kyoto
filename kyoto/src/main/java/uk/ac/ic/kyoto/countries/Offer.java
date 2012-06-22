/**
 * 
 */
package uk.ac.ic.kyoto.countries;

import uk.ac.ic.kyoto.trade.InvestmentType;
import uk.ac.ic.kyoto.trade.TradeType;

/**
 * @author cmd08 and farhanrahman
 *
 */
public class Offer{
	final double quantity;
	final double unitCost;
	final TradeType type;
	final InvestmentType itype;

	public static String TRADE_PROPOSAL = "Trade proposal";	

	Offer(double quantity, double unitCost, TradeType type) {
		this.quantity = quantity;
		this.unitCost = unitCost;
		this.type = type;
		this.itype = InvestmentType.INVALID;
	}
	
	Offer(double quantity, double unitCost, TradeType type, InvestmentType itype) {
		this.quantity = quantity;
		this.unitCost = unitCost;
		this.type = type;
		this.itype = itype;
	}

	public double getQuantity() {
		return quantity;
	}

	public double getUnitCost() {
		return unitCost;
	}

	public double getTotalCost() {
		return unitCost * quantity;
	}

	public TradeType getType(){
		return this.type;
	}
	
	public InvestmentType getInvestmentType(){
		return this.itype;
	}

	@Override
	public String toString() {
		return "Trade: "+quantity+" @ "+unitCost; 
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Offer))
			return false;
		Offer other = (Offer) obj;
		if (itype != other.itype)
			return false;
		if (Double.doubleToLongBits(quantity) != Double
				.doubleToLongBits(other.quantity))
			return false;
		if (type != other.type)
			return false;
		if (Double.doubleToLongBits(unitCost) != Double
				.doubleToLongBits(other.unitCost))
			return false;
		return true;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((itype == null) ? 0 : itype.hashCode());
		long temp;
		temp = Double.doubleToLongBits(quantity);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		temp = Double.doubleToLongBits(unitCost);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	public Offer reverse(){
		TradeType t = this.type;
		InvestmentType i = this.itype;
		
		switch (t) {
			case BUY: t = TradeType.SELL; break;
			case SELL: t = TradeType.BUY; break;
			case INVEST: t = TradeType.RECEIVE; break;
			case RECEIVE: t = TradeType.INVEST; break;
		}
		switch (i) {
			case ABSORB: return new Offer(this.quantity, this.unitCost, t, i);
			case REDUCE: return new Offer(this.quantity, this.unitCost, t, i);
			default:	 return new Offer(this.quantity, this.unitCost, t);
		}
		
	}

}