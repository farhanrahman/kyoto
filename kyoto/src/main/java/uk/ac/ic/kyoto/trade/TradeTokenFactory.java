/**
 * 
 */
package uk.ac.ic.kyoto.trade;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.Singleton;

/**
 * @author farhanrahman
 *
 */
public class TradeTokenFactory {
	@Provides @Singleton
	public static TradeToken get(){
		Injector inj = Guice.createInjector(new TradeTokenModule());
		return inj.getInstance(TradeToken.class);
	}
}
