package ch.usi.dag.disl.cbloader;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import ch.usi.dag.disl.exception.ManifestInfoException;

public class ManifestHelper {

	private static final String MANIFEST = "META-INF/MANIFEST.MF";
	
	public static final String ATTR_DISL_CLASSES = "DiSL-Classes";
	public static final String ATTR_DISL_TRANSFORMER = "DiSL-Transformer";
	
	public static class ManifestInfo {
		
		private URL resource;
		private Manifest manifest;
		private String dislClasses;
		private String dislTransformer;
		
		public ManifestInfo(URL resource, Manifest manifest, String dislClasses,
				String dislTransformer) {
			super();
			this.resource = resource;
			this.manifest = manifest;
			this.dislClasses = dislClasses;
			this.dislTransformer = dislTransformer;
		}

		public URL getResource() {
			return resource;
		}

		public Manifest getManifest() {
			return manifest;
		}

		public String getDislClasses() {
			return dislClasses;
		}
		
		public String getDislTransformer() {
			return dislTransformer;
		}
	}
	
	private static ManifestInfo cachedMI = null;
	private static boolean cacheValid = false;
	
	// caching layer
	public static ManifestInfo getDiSLManifestInfo()
			throws ManifestInfoException {
		
		// resolve manifest info
		if(! cacheValid) {
			cachedMI = resolveDiSLManifestInfo();
			cacheValid = true;
		}
		
		// return manifest info
		return cachedMI;
	}
	
	private static ManifestInfo resolveDiSLManifestInfo()
			throws ManifestInfoException {
		
		try {
		
			ClassLoader cl = ManifestInfo.class.getClassLoader();
		
			// get all manifests...
			Enumeration<URL> resources = cl.getResources(MANIFEST);
			
			// and find ours...
			while (resources.hasMoreElements()) {
	
				URL resource = resources.nextElement();
				
				Manifest manifest = new Manifest(resource.openStream());
				
				Attributes attrs = manifest.getMainAttributes();
				
				// contains disl classes
				if(attrs != null) {
					
					String dislClasses = attrs.getValue(ATTR_DISL_CLASSES);
					String dislTrans = attrs.getValue(ATTR_DISL_TRANSFORMER);
					
					if(dislClasses != null) {
						return new ManifestInfo(resource, manifest,
								dislClasses, dislTrans);
					}
				}
			}
		}
		catch (IOException e) {
			
			throw new ManifestInfoException(e);
		}
		
		return null;
	}
}
