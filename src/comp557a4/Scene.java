/** Neil Edelman -- 110121860 */

package comp557a4;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.vecmath.Color3f;
import javax.vecmath.Vector3d;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Simple scene loader based very loosely on Yafaray xml format this seemed to
 * be the simplest (i.e., least verbose) choice, in comparison to the more wide
 * spread xml standards (COLLADA and X3D)
 */
public class Scene {

	/* FIXME: include Light in SceneNode! */
	Map<String, Material> materialmap = new HashMap<String, Material>();
	Map<String, Light> lightmap = new HashMap<String, Light>();
	Map<String, Camera> cameramap = new HashMap<String, Camera>();
	Map<String, SceneNode> nodeMap = new HashMap<String, SceneNode>();

	List<Render> renderList = new LinkedList<Render>();

	private static final int progressBarChars = 32;
	public static final double epsilon = 1e-8;

	/** creates a scene from an xml file */
	public Scene(Node dataNode) {
		NodeList nodeList = dataNode.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node n = nodeList.item(i);
			// skip all text, just process the ELEMENT_NODEs
			if (n.getNodeType() != Node.ELEMENT_NODE)
				continue;
			String nodeName = n.getNodeName();
			if (nodeName.compareToIgnoreCase("material") == 0) {
				Material material = new Material(n);
				materialmap.put(material.name, material);
			} else if (nodeName.compareToIgnoreCase("light") == 0) {
				Light light = Light.magic(n);
				if(light != null) lightmap.put(light.getName(), light);
			} else if (nodeName.compareToIgnoreCase("camera") == 0) {
				Camera camera = new Camera(n);
				cameramap.put(camera.name, camera);
			} else if (nodeName.compareToIgnoreCase("render") == 0) {
				renderList.add(new Render(n));
			} else if (nodeName.compareToIgnoreCase("node") == 0) {
				SceneNode sceneNode = new SceneNode(n, this, null);
				nodeMap.put(sceneNode.name, sceneNode);
			}
		}
	}

	/** renders the scene */
	public void render() {
		SceneNode root = nodeMap.get("root");
		IntersectionResult result = new IntersectionResult();
		Color3f colour = new Color3f();

		for (Render render : renderList) {
			Camera cam = cameramap.get(render.cameraName);
			int w = cam.imageSize.width;
			int h = cam.imageSize.height;
			BufferedImage image = new BufferedImage(w, h,
					BufferedImage.TYPE_4BYTE_ABGR);
			System.out.print("Render from '"+render.cameraName+":'\n");

			Bresenham progress = new Bresenham(w*h, progressBarChars);
			for(int i = 0; i < progressBarChars; i++) System.out.print('_');
			System.out.print('\n');

			for (int i = 0; i < h; i++) {
				for (int j = 0; j < w; j++) {
					/* generate the ray for the pixel */
					Ray ray = generate(cam, j, i);
					/* intersect the geometry */
					result.reset(ray);
					root.intersect(ray, result);
					/* FIXME: background hard-coded */
					if(result.shape == null) colour.set(j^i/*(float)j/w/2+0.1f*/,0.1f,0.1f);
					else colour = colour(result);
					/* put it on the image */
					int r = (int)(255 * colour.x);
					int g = (int)(255 * colour.y);
					int b = (int)(255 * colour.z);
					int a = 255;
					int argb = a << 24 | r << 16 | g << 8 | b;
					image.setRGB(j, i, argb);
					/* progress update */
					if(progress.update()) System.out.print(/*'\u0219'*//*(char)219*/'\170');
				}
			}
			System.out.print(" [done.]\n");

			File file = new File(render.output);
			try {
				if(!ImageIO.write(image, "png", file))
					System.err.println("Error writing file using ImageIO (unsupported file format?)");
			} catch(IOException e) {
				System.err.println("trouble writing " + file);
				e.printStackTrace();
			}
		}
	}

	/** do lighting + materials to colour the ray;
	 * FIXME: this is laughably unoptimised, esp for a static scene!
	 * maybe if we had more time . . . */
	private Color3f colour(IntersectionResult result) {
		Intersectable root = nodeMap.get("root");
		Color3f lightColour = new Color3f(), be;
		/* the lighting */
		for(Map.Entry<String,Light> lightHash : lightmap.entrySet()) {
			if((be = lightHash.getValue().manifest(root, result)) == null) continue;
			lightColour.add(be);
		}
		/* FIXME: bleed over */
		lightColour.clampMax(1);
		Material m = result.shape.material();
		if(m != null) {
			/* lightColour.mul(materialColour); no? really */
			lightColour.x *= m.color.x;
			lightColour.y *= m.color.y;
			lightColour.z *= m.color.z;
		}
		return lightColour;
	}

	/* static vars reused for generate() */
	private static Vector3d d = new Vector3d(), u = new Vector3d(), v = new Vector3d();

	/** generate rays from 'cam' in (x,y) pixels */
	private static Ray generate(final Camera cam, final int x, final int y) {
		final int w = cam.imageSize.width, h = cam.imageSize.height;
		u.set(cam.getAxis(0));
		u.scale(cam.getSize().x/2 * (x-w/2+0.5) / (w/2));
		v.negate(cam.getAxis(1)); /* left-handed image space */
		v.scale(cam.getSize().y/2 * (y-h/2+0.5) / (h/2));
		d.negate(cam.getAxis(2)); /* points in -z */
		d.add(u);
		d.add(v);
		return new Ray(cam.from, d);
	}
}
