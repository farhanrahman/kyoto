/**
 * 
 */
package uk.ac.ic.kyoto.trade;

import uk.ac.ic.kyoto.tokengen.TradeTokenGenerator;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.google.inject.assistedinject.FactoryModuleBuilder;

/**
 * @author farhanrahman
 *
 */
public class TradeTokenModule extends AbstractModule {

	/* (non-Javadoc)
	 * @see com.google.inject.AbstractModule#configure()
	 */
	@Override
	protected void configure() {
		/*install(new FactoryModuleBuilder()
	     .implement(TradeToken.class, TradeTokenGenerator.class)
	     .build(TradeTokenFactory.class));*/
		
		//bind(TradeToken.class).to(TradeTokenGenerator.class);
		bind(TradeToken.class).to(TradeTokenGenerator.class).in(Singleton.class);
	}

}
