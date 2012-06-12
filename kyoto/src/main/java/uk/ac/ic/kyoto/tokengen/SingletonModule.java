/**
 * 
 */
package uk.ac.ic.kyoto.tokengen;


import uk.ac.ic.kyoto.tradehistory.TradeHistory;
import uk.ac.ic.kyoto.tradehistory.TradeHistoryImplementation;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;

/**
 * Configures the module
 * for binding Interfaces
 * to Singletons
 * @author farhanrahman
 *
 */
public class SingletonModule extends AbstractModule {

	/* (non-Javadoc)
	 * @see com.google.inject.AbstractModule#configure()
	 */
	@Override
	protected void configure() {
		bind(Token.class).to(TokenGenerator.class).in(Singleton.class);
		bind(TradeHistory.class).to(TradeHistoryImplementation.class).in(Singleton.class);
	}

}
