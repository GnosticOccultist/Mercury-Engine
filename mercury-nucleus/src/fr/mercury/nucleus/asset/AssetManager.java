package fr.mercury.nucleus.asset;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import fr.mercury.nucleus.renderer.opengl.shader.ShaderSource;
import fr.mercury.nucleus.renderer.opengl.shader.ShaderSource.ShaderType;

// TODO: Redo the entire class.
public class AssetManager {
	
	public ShaderSource getShaderSource(String name, ShaderType type)  {
		ShaderSource source = new ShaderSource(type, getShaderSource(name).toString());
		return source;
	}
	
	public StringBuilder getShaderSource(String name)  {
		StringBuilder shaderSource = new StringBuilder();
		try {
			for(String line : this.readAllLines(name)) {
				shaderSource.append(line).append("//\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return shaderSource;
	}
	
	public List<String> readAllLines(String name) throws IOException {
		List<String> list = new ArrayList<>();
		try (BufferedReader br = new BufferedReader(new InputStreamReader(stream(name)))) {
			String line = null;
			while ((line = br.readLine()) != null) {
				list.add(line);
			}
		}
		return list;
	}
	
	private InputStream stream(String name) throws IOException {
		final URL url = locateInternal(name);
		if(url != null) {
			final URLConnection connection = url.openConnection();
			connection.setUseCaches(false);
			return connection.getInputStream();
		}
		return null;
	}

	private URL locateInternal(String name) {
		if (name.startsWith("/")) {
            name = name.substring(1);
        }
        
        final URL url = getClass().getResource("/" + name);

        if (url == null) {
            return null;
        }
        
        if (url.getProtocol().equals("file")) {
        	try {
        		String path = new File(url.toURI()).getCanonicalPath();
        		
            	// In Windows, convert '\' to '/'.
            	if (File.separatorChar == '\\') {
                    path = path.replace('\\', '/');
                }
        	} catch (URISyntaxException | IOException e) {
        		e.printStackTrace();
        	}
        }
        return url;
	}
}
