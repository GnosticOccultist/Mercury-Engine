package fr.mercury.exempli.gratia.context;

import fr.mercury.nucleus.application.MercuryApplication;
import fr.mercury.nucleus.application.MercuryContext.Type;
import fr.mercury.nucleus.application.service.TaskExecutorService;
import fr.mercury.nucleus.application.service.Window;
import fr.mercury.nucleus.input.BaseInputProcessor;
import fr.mercury.nucleus.renderer.Renderer;
import fr.mercury.nucleus.application.MercurySettings;
import fr.mercury.nucleus.utils.ReadableTimer;

public class TestHeadlessContext extends MercuryApplication {

	/**
	 * Launch method for the <code>TestHeadlessContext</code>, no arguments required.
	 * 
	 * @param args The arguments to pass to the application.
	 */
	public static void main(String[] args) {
		var settings = new MercurySettings(true);
		settings.setContextType(Type.HEADLESS);
		settings.setFrameRate(60);

		TestHeadlessContext app = new TestHeadlessContext();
		app.setSettings(settings);
		app.start();
	}

	@Override
	protected void initialize() {
		TaskExecutorService taskService = new TaskExecutorService();
		linkService(taskService);

		taskService.scheduleAtFixedRate(this::printTPF, 500);
		
		assert getService(Window.class) == null;
		assert getService(Renderer.class) == null;
		assert getService(BaseInputProcessor.class) == null;
	}

	@Override
	protected void update(ReadableTimer timer) {
		super.update(timer);
	}

	/**
	 * Prints to the console the current time per frame.
	 */
	private void printTPF() {
		logger.info("TPF : " + timer.getTimePerFrame() * 1_000 + " ms");
	}
}
