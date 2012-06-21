/**
 * 
 */
package uk.ac.ic.kyoto.util.sim.jsonobjects.tradedata;

import uk.ac.ic.kyoto.countries.OfferMessage;

/**
 * @author farhanrahman
 *
 */
public class TradeObject {
	private OfferMessage o = null;
	/**
	 * 
	 */
	public TradeObject(OfferMessage offerMessage) {
		if(offerMessage != null){
			this.o = offerMessage;
		}
	}
	
	@Override
	public String toString(){
		String s = "";
		if(o != null){
			s += " \"tradeID\" : " + " \"" + o.getTradeID() + "\",";
			s += " \"broadCaster\" : " + "\"" + o.getBroadCaster()+ "\",";
			s += " \"initiator\" : " + "\"" + o.getInitiator() + "\",";
			s += " \"quantity\" : " + "\"" + o.getOfferQuantity() + "\",";
			s += " \"unitCost\" : " + "\"" + o.getOfferUnitCost() + "\",";
			s += " \"tradeType\" : " + "\"" + o.getOfferType() + "\",";
			s += " \"investmentType\" : " + "\"" + o.getOfferInvestmentType() + "\"";
		}
		return s;
	}

}
