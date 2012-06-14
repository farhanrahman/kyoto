/**
 * 
 */
package uk.ac.ic.kyoto.countries;

/**
 * @author cmd08 and farhanrahman
 *
 */
public class Offer{
	final int quantity;
	final int unitCost;
	final TradeType type;
	final InvestmentType itype;

	public static String TRADE_PROPOSAL = "Trade proposal";	

	public Offer(int quantity, int unitCost, TradeType type) {
		this.quantity = quantity;
		this.unitCost = unitCost;
		this.type = type;
		this.itype = InvestmentType.INVALID;
	}
	
	public Offer(int quantity, int unitCost, TradeType type, InvestmentType itype) {
		this.quantity = quantity;
		this.unitCost = unitCost;
		this.type = type;
		this.itype = itype;
	}

	public int getQuantity() {
		return quantity;
	}

	public int getUnitCost() {
		return unitCost;
	}

	public int getTotalCost() {
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

	public boolean equals(Offer t){
		if(this == t) {
			return true;
		} else if (	this.quantity == t.getQuantity() && 
					this.unitCost == t.getUnitCost() && 
					this.type == t.getType()) {
			return true;
		} else if (	this.quantity == -t.getQuantity() &&
					this.unitCost == t.getUnitCost() && 
					this.type == t.reverse().getType()){
			return true;
		} else if ( this.quantity == t.getQuantity() &&
					this.unitCost == -t.getUnitCost() &&
					this.type == t.reverse().getType()){
			return true;
		} else {
			return false;
		}
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