/**
 * 
 */
package uk.ac.ic.kyoto.singletonfactory;


import uk.ac.ic.kyoto.tokengen.Token;
import uk.ac.ic.kyoto.tradehistory.TradeHistory;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.Singleton;

/**
 * Factory that provides
 * Singletons Token and TradeHistory
 * implementations. Need to change
 * the function name getToken() as
 * Token also has a getToken() function
 * @author farhanrahman
 *
 */
public class SingletonProvider {
	
	private static Injector inj = Guice.createInjector(new SingletonModule());
	@Provides @Singleton
	public static Token getToken(){
		return SingletonProvider.inj.getInstance(Token.class);
	}
	
	@Provides @Singleton
	public static TradeHistory getTradeHistory(){
		return SingletonProvider.inj.getInstance(TradeHistory.class);
	}
}
