package uk.ac.ic.kyoto.roguestates;


import java.util.Random;
import java.util.Set;
import java.util.UUID;

import uk.ac.ic.kyoto.countries.AbstractCountry;
import uk.ac.ic.kyoto.countries.Offer;
import uk.ac.ic.kyoto.countries.OfferMessage;
import uk.ac.ic.kyoto.exceptions.CannotLeaveKyotoException;
import uk.ac.ic.kyoto.exceptions.NotEnoughCashException;
import uk.ac.ic.kyoto.exceptions.NotEnoughLandException;
import uk.ac.ic.kyoto.services.FossilPrices;
import uk.ac.imperial.presage2.core.environment.ParticipantSharedState;
import uk.ac.imperial.presage2.core.messaging.Input;
import uk.ac.imperial.presage2.core.network.NetworkAddress;
import uk.ac.imperial.presage2.util.fsm.FSMException;


public class CanadaAgent extends AbstractCountry {

	private double	AverageGDPRate; // to be stored in an array or DB for furhter analysis.
	long Currentyear;				//The present year global	
	long Prevyear=Currentyear-1;	//Previous year
	int n=8,m=3;					//Number of players and projects initially
	int industry_players=0;
	int industry_projects=0;
	double ratio;
	public CanadaAgent(UUID id, String name,String ISO, double landArea, double arableLandArea, double GDP,
			double GDPRate, double emissionsTarget, double energyOutput, double carbonOutput) {
		super(id, name, ISO, landArea, arableLandArea, GDP,
				GDPRate, energyOutput, carbonOutput);
		//
		// TODO Auto-generated constructor stub
	}
	
	
	@Override
	protected Set<ParticipantSharedState> getSharedState() {
		return super.getSharedState();
	}

	/**
	 * Increase carbon absorption as long as you have land area and money
	 */
	
			
	/******************************************************
	@Override
	//Check Carbon Output data and decide a feasible target to set 
	//as we are intially part of kyoto 
	 * @return
	 *********************************************************/
 
		FossilPrices price = new FossilPrices(null);
		
		/*Function to check oil prices to ramp or reduce production  */
		
		boolean check_oil_prices(long year){
			if(((price.getOilPrice(year)) > 100)){
				System.out.print("We can reduce production");
				return true;
			}
			else return false;
			}
		
		
	/*Emisions target to be checked against to gauge performance  */			
	public double SetEmissionsTarget(){
		return this.getEmissionsTarget();
		
	}
	
	/*Check our performancce  */
	boolean check_emmision(long year){
		if((this.getEnergyOutput()) <= (this.getPrevEnergyOutput())){
			System.out.print("Good progress");
			return true;
		}
		else return false;
	}
	
	/*Industry reduction functions  */
	/*
	investment_ammount=this.
	
	*/
	
	
	
	Random rand1= new Random();
	Random rand2= new Random();
	
	/*This function checks the industry projects against players  */
	/*to determine feasibility of carbon reduction */
	
	public boolean check_industry(){
				
		industry_players=rand1.nextInt(n);
		industry_projects=rand2.nextInt(m);
		if((industry_players<=8) && (industry_projects<=5)){
			return true;
		}	
		else return false;
	}
	
	/*if the GDP rate is less than or equal to the */
	/*available to spend against gdp ratio we return true and stay in kyoto  */
	public boolean check_ratio(){
		double ratio,carb,gdp_r,gdp_a;
		carb=this.getAvailableToSpend();
		gdp_r=this.getGDPRate();
		gdp_a=this.getGDP();
		ratio=carb/gdp_a;
		if(ratio <= gdp_r){
			return true;
		}
		else return false;
	}
	
	
	/*
	boolean JoiningCriteriaMet()
<<<<<<< HEAD
	{
		if(((this.getCarbonOutput() - this.getCarbonOffset()) >this.getEmissionsTarget())){
=======
	{ // TODO emissions target will only resolve if you are already a member of Kyoto
		if(((this.getCarbonOutput()) > this.getEmissionsTarget()) && ()){
>>>>>>> 3804685022313493de0f441c6beb2984c3fea077
			return true;
		}
		//Only useful after 2011 when canada leaves
		else leaveKyoto();
		return false;
		
		//If GDP growth achieved in the past 4 years
		//
	}
*/
	
	protected void processInput(Input input) {
		if(this.tradeProtocol.canHandle(input)){
			this.tradeProtocol.handle(input);
		}else{
			OfferMessage offerMessage = this.tradeProtocol.decodeInput(input);
			NetworkAddress from =this.tradeProtocol.extractNetworkAddress(input);
			double quantity = offerMessage.getOfferQuantity();
			if(analyse_offer(offerMessage)){
				try{
					this.tradeProtocol.respondToOffer(from, quantity,offerMessage);
				} catch(IllegalArgumentException e1){
					logger.warn(e1);
				} catch(FSMException e1){
					logger.warn(e1);
				}
			}
			
		}
		
		
	}
	
	double calc_target()
	{
		return((this.getEmissionsTarget())-(this.getCarbonOutput())+this.getCarbonOffset()+this.getCarbonAbsorption());
	}
	
	boolean analyse_offer(OfferMessage offerMessage){
		//If the offer is feasible return true else false
		double cost = offerMessage.getOfferUnitCost();
		double quantity = offerMessage.getOfferQuantity();
		double available = this.getAvailableToSpend();
		double carb_red_cost=this.carbonReductionHandler.getInvestmentRequired(this.calc_target());
		double trade_cost = (cost*quantity);
		if((trade_cost < carb_red_cost) && (trade_cost<available) &&(carb_red_cost<available)){
			return true;
		}
		else return false;
		
	}
	
	boolean analyse_offer(Offer offerMessage) {
		double cost = offerMessage.getUnitCost();
		double quantity = offerMessage.getQuantity();
		double available = this.getAvailableToSpend();
		double carb_red_cost=this.carbonReductionHandler.getInvestmentRequired(this.calc_target());
		double trade_cost = (cost*quantity);
		if((trade_cost < carb_red_cost) && (trade_cost<available) &&(carb_red_cost<available)){
			return true;
		}
		else return false;
	}
	
public void behaviour() {
		
		// Check environment save
		//Check industry reduction
		
		//If not recession we can consider carbon reductions
		double carbon_out=this.getCarbonOutput();
		double carb_offSet=this.getCarbonOffset();
		double money=this.getAvailableToSpend();
		double land_area=this.getArableLandArea();
		double carbon_reduction ;
		double industry_reduction;
		double carbon_absorbtion;
		double target=this.getEmissionsTarget();
		double carbon_change=target-getCarbonOutput()-getCarbonOffset()-getCarbonAbsorption();
		
		//check if we can achieve carbon reduction by foresting
		double absorption_change;
		try {
			absorption_change = this.carbonAbsorptionHandler.getCarbonAbsorptionChange(money, land_area);
		} catch (NotEnoughLandException e1) {
			absorption_change = 0;
		}
		if((absorption_change+this.getCarbonOutput()) <= this.getEmissionsTarget() ){
			System.out.print("We can achieve target");
				}
		
		//Check if industry within limit and GDP growth
		double inv_required;
		
			try {
				inv_required=this.carbonAbsorptionHandler.getInvestmentRequired(carbon_change, land_area);
			} catch (NotEnoughLandException e) {
				inv_required=Double.MAX_VALUE;
			}
		if(carbon_change>0){
			if(inv_required<=this.getAvailableToSpend()){
				System.out.print("we can invest in reduction");
			}
		}
		
		if(getCarbonOutput() - getCarbonOffset() - getCarbonAbsorption() < target ){
			System.out.print("Investing not needed");
		}else {
			try {
				this.carbonAbsorptionHandler.investInCarbonAbsorption(this.calc_target());
			} catch (NotEnoughLandException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NotEnoughCashException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			}
			//Consider investing in industry or buy credits after comparison
		}
		
		
		
//		Set<NetworkAddress> nodes = network.getConnectedNodes();
//		for (NetworkAddress i: nodes) {
//			try {
//				tradeProtocol.offer(i, 10, 5, TradeType.BUY);
//			} catch (FSMException e) {
//				e.printStackTrace();
//			}
//		}
	//	if (getAvailableToSpend() > 0) {
			//try {
				//carbonReductionHandler.invest(availableToSpend*0.1);
				//System.out.println("Spending " + getAvailableToSpend()* 0.1 + " on carbon reduction. Current carbon output is " + getCarbonOutput() + ".");
			//} catch (Exception e) {
				//logger.warn(e.getMessage(), e);
				//e.printStackTrace();
			//}
	//	}
//		if (availableToSpend > 0) {
//			try {
//				energyUsageHandler.investInCarbonIndustry((long) (availableToSpend*0.1));
//				System.out.println("Spending " + availableToSpend* 0.1 + " on industry investment.");
//				System.out.println();
//				} catch (Exception e) {
//				logger.warn(e.getMessage(), e);
//				e.printStackTrace();
//			}
//		}
//		System.out.println(energyUsageHandler.calculateCostOfInvestingInCarbonIndustry(500));
		//System.out.println("I have this much money: " + getAvailableToSpend() + ".");
		//System.out.println("My GDPRate is : " + getGDPRate());
		//System.out.println("My carbon output is : " + getCarbonOutput());
		//System.out.println("My energy output is : " + getEnergyOutput());
	//}
	
	@Override
	protected boolean acceptTrade(NetworkAddress from,Offer offerMessage) {
		// TODO Auto-generated method stub
		if(this.analyse_offer(offerMessage)){
			return true;
		}
		
		else return false;
	}
	
	@Override
	public void yearlyFunction() {//Implement any actions every year
		// TODO Auto-generated method stub
		//Check gdp growth against emmisions every year and  decide how to reduce
		if((this.getGDPRate()>0) && (this.carbonReductionHandler.getCarbonOutputChange(getAvailableToSpend(), getCarbonOutput(), getEnergyOutput()))<getEmissionsTarget()){
			isKyotoMember();
		}
	}

	
	
	@Override//Every 10 years do the check to stay or leave kyoto
	public void sessionFunction() {
		if (getCarbonOutput() - getCarbonOffset() > getEmissionsTarget()) {
			try {
				leaveKyoto();
			} catch (CannotLeaveKyotoException e) {
				System.out.print("Check");
			}
		}
			
	}
	
	
	@Override
	public void initialiseCountry() {
	/*carbonOutput = 80;
		try {
			tradeProtocol = new TradeProtocol(getID(), authkey, environment, network) {
				@Override
				protected boolean acceptExchange(NetworkAddress from,
						Trade trade) {
					if (carbonOutput - emissionsTarget + carbonOffset > 0) {
						return true;
					}
					return true;
				}
			};
		} catch (FSMException e) {
			logger.warn(e.getMessage(), e);
			e.printStackTrace();
		}
	}*/
		
		kyotoMemberLevel = KyotoMember.ANNEXONE;
		
	}
	
	

}
