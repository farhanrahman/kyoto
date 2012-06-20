package uk.ac.ic.kyoto.services;

import static org.junit.Assert.*;
import java.io.Serializable;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import uk.ac.imperial.presage2.core.environment.EnvironmentSharedStateAccess;
import uk.ac.imperial.presage2.core.environment.ParticipantSharedState;
import uk.ac.imperial.presage2.core.environment.SharedState;
import uk.ac.imperial.presage2.core.environment.StateTransformer;

public class GlobalTimeServiceTest {
	
	public GlobalTimeService gts;
	
	@Before
	public void setUp(){
		gts = new GlobalTimeService(new EnvironmentSharedStateAccess() {
			
			@Override
			public Serializable getGlobal(String name) {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public Serializable get(String name, UUID participantID) {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public void deleteGlobal(String name) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void delete(String name, UUID participantID) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void createGlobal(String name, Serializable value) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void createGlobal(SharedState state) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void create(String name, UUID participantID, Serializable value) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void create(ParticipantSharedState state) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void changeGlobal(String name, Serializable value) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void changeGlobal(String name, StateTransformer change) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void change(String name, UUID participantID, Serializable value) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void change(String name, UUID participantID, StateTransformer change) {
				// TODO Auto-generated method stub
				
			}
		});
	}

	@Test
	public void testGetCurrentYear() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetCurrentSession() {
		gts.getCurrentSession();
		fail("Not yet implemented");
	}

	@Test
	public void testYearToSimTime() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetCurrentTick() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetTicksInYear() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetYearsInSession() {
		fail("Not yet implemented");
	}

}
