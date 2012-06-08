/**
 * 
 */
package uk.ac.ic.kyoto.tokengen;


import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.google.inject.assistedinject.FactoryModuleBuilder;

/**
 * @author farhanrahman
 *
 */
public class TokenModule extends AbstractModule {

	/* (non-Javadoc)
	 * @see com.google.inject.AbstractModule#configure()
	 */
	@Override
	protected void configure() {
		/*install(new FactoryModuleBuilder()
	     .implement(TradeToken.class, TradeTokenGenerator.class)
	     .build(TradeTokenFactory.class));*/
		
		//bind(TradeToken.class).to(TradeTokenGenerator.class);
		bind(Token.class).to(TokenGenerator.class).in(Singleton.class);
	}

}
