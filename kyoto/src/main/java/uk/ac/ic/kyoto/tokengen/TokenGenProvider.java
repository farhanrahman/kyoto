/**
 * 
 */
package uk.ac.ic.kyoto.tokengen;


import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.Singleton;

/**
 * @author farhanrahman
 *
 */
public class TokenGenProvider {
	
	private static Injector inj = Guice.createInjector(new TokenModule());
	@Provides @Singleton
	public static Token get(){
		return TokenGenProvider.inj.getInstance(Token.class);
	}
}
