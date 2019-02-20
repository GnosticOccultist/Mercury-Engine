package fr.mercury.nucleus;

import fr.mercury.nucleus.application.Application;
import fr.mercury.nucleus.application.MercuryApplication;
import fr.mercury.nucleus.application.module.AbstractApplicationModule;

public class TestApplicationModule extends MercuryApplication {
	
	public static void main(String[] args) {
		TestApplicationModule app = new TestApplicationModule();
		app.start();
	}
	
	@Override
	protected void initialize() {
		linkModule(new DummyModule(false));
	}
	
	public class DummyModule extends AbstractApplicationModule {

		public DummyModule(boolean enabled) {
			super(enabled);
		}
		
		@Override
		public void initialize(Application application) {
			super.initialize(application);
			
			logger.info("Module has been initialized successfully!");
		}

		@Override
		public void update(float tpf) {
			logger.info("Module is being updated!");
		}

		@Override
		public void cleanup() {
			logger.info("Module has been cleaned up successfully!");
		}
	}
}
