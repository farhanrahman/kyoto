package uk.ac.ic.kyoto;

import java.util.Collection;
import java.util.UUID;

import org.drools.runtime.ObjectFilter;
import org.drools.runtime.StatefulKnowledgeSession;

import uk.ac.imperial.presage2.core.environment.EnvironmentService;
import uk.ac.imperial.presage2.core.environment.EnvironmentSharedStateAccess;
import uk.ac.imperial.presage2.core.simulator.SimTime;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

/**
 * May no longer be needed. Consult Sam before deletion
 * 
 * @author sam
 */
@Deprecated
@Singleton
public class KnowledgeBaseService extends EnvironmentService {

	final StatefulKnowledgeSession session;
	int currentTurn = 0;
	final int turnLength;

	@Inject
	protected KnowledgeBaseService(StatefulKnowledgeSession session,
			EnvironmentSharedStateAccess sharedState,
			@Named("params.turnlength") int turnLength) {
		super(sharedState);
		this.session = session;
		this.turnLength = turnLength;
	}

	public CountryFile getCountry(final UUID aid) {
		Collection<Object> matches = session.getObjects(new ObjectFilter() {
			@Override
			public boolean accept(Object object) {
				if (object instanceof CountryFile) {
					CountryFile c = (CountryFile) object;
					return c.getAgent().getAid().equals(aid);
				}
				return false;
			}
		});
		for (Object country : matches) {
			return (CountryFile) country;
		}
		return null;
	}

	public int nextTurn() {
		int time = SimTime.get().intValue();
		return ((int) Math.floor(((double) time / (double) turnLength)))
				* turnLength + turnLength;
	}

}

