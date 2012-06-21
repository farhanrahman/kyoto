/**
 * 
 */
package uk.ac.ic.kyoto.util.sim.jsonobjects.tradedata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import uk.ac.ic.kyoto.countries.OfferMessage;

/**
 * @author farhanrahman
 *
 */
public class TradeObject {
	private Map<UUID,OfferMessage> trades = new HashMap<UUID,OfferMessage>();
	/**
	 * 
	 */
	public TradeObject(Map<UUID,OfferMessage> trades) {
		if(trades != null){
			this.trades = trades;
		}
	}
	
	@Override
	public String toString(){
		String s = "{";
		ArrayList<UUID> ids = new ArrayList<UUID>(this.trades.keySet());
		
		for(UUID id : ids){
			OfferMessage o = trades.get(id);
			s += " \"" + o.getTradeID() + "\" : {";
			s += " \"broadCaster\" : " + "\"" + o.getBroadCaster()+ "\",";
			s += " \"initiator\" : " + "\"" + o.getInitiator() + "\",";
			s += " \"quantity\" : " + "\"" + o.getOfferQuantity() + "\",";
			s += " \"unitCost\" : " + "\"" + o.getOfferUnitCost() + "\",";
			s += " \"tradeType\" : " + "\"" + o.getOfferType() + "\",";
			s += " \"investmentType\" : " + "\"" + o.getOfferInvestmentType() + "\"";
			
			if(ids.indexOf(id) == ids.size() - 1){
				s += "}";
			}else{
				s += "},";
			}
		}
		s += "}";
		return s;
	}

}
