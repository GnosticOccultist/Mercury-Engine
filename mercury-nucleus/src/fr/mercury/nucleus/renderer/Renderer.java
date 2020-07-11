package fr.mercury.nucleus.renderer;

import fr.mercury.nucleus.application.service.ApplicationService;
import fr.mercury.nucleus.scenegraph.NucleusMundi;

public interface Renderer extends ApplicationService {

	void resize(int width, int height);
	
	void setCamera(Camera camera);

	void renderScene(NucleusMundi scene);
}
