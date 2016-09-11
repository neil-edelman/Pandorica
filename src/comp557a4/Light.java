/** Neil Edelman -- 110121860 */

package comp557a4;

import javax.vecmath.Color3f;
import javax.vecmath.Vector3d;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/** ambient light is all over */
class AmbientLight extends Light {
	static private Color3f l = new Color3f();

	public Color3f manifest(Intersectable root, IntersectionResult result) {
		l.set(colour);
		l.scale(power);
		return l;
	}
}

/** directional light */
class DirectionalLight extends Light {
	/* static vars reused for manifesting lights */
	static private Color3f  l = new Color3f(), specular = new Color3f();
	static private Vector3d dir = new Vector3d(), half = new Vector3d(), eye = new Vector3d();
	static private Ray shadow = new Ray();

	public Color3f manifest(final Intersectable root, final IntersectionResult result) {
		/* diffuse */
		dir.set(from); /* already normalised */
		double diff = result.n.dot(dir);
		if(diff <= 0) return null;
		shadow.set(result.intersection, dir);
		if(root.intersect(shadow, null)) return null;
		/* not specular */
		Material m = result.shape.material();
		if(m == null || m.specular < minSpecular) {
			/* FIXME: look up double / float (conversion) speeds */
			l.scale((float)(diff * power), colour);
			return l;
		}
		/* specular */
		eye.sub(result.eye, result.intersection);
		eye.normalize(); /* FIXME: gah! */
		half.add(eye, dir);
		half.normalize();
		double spec = half.dot(result.n);
		spec = Math.pow(spec, result.shape.material().specular);
		l.scale((float)(diff * power), colour);
		specular.scale((float)(spec * power), m.mirrorColor);
		l.add(specular);
		return l;
	}
}

/** yay, point lights */
class PointLight extends Light {
	/* static vars reused for manifesting lights */
	static private Color3f  l = new Color3f(), specular = new Color3f();
	static private Vector3d dir = new Vector3d(), half = new Vector3d(), eye = new Vector3d();
	static private Ray shadow = new Ray();

	public Color3f manifest(final Intersectable root, final IntersectionResult result) {
		/* diffuse */
		dir.sub(from, result.intersection);      /* light -> surface */
		/* result.shape.matrix().transform(norm); <- lighting in object space */
		double diff = dir.dot(result.n);
		if(diff <= 0) return null;               /* diff is going the other way? */
		shadow.set(result.intersection, dir, 1); /* shadow */
		if(root.intersect(shadow, null)) return null;
		double distance2 = dir.lengthSquared();  /*   distance^2 (fall-off) */
		Material m = result.shape.material();
		/* not specular */
		if(m == null || m.specular < minSpecular) {
			double distance3 = distance2 * Math.sqrt(distance2);
			l.scale((float)(diff * power / distance3), colour);
			return l;
		}
		/* specular */
		diff /= Math.sqrt(distance2);            /* dir.dot(norm) normalisation */
		eye.sub(result.eye, result.intersection);
		eye.normalize();
		dir.normalize();                         /* FIXME: ow, gah! */
		half.add(eye, dir);
		half.normalize();
		double spec = half.dot(result.n);
		/*if(spec <= 0) return null; <- is it even possible? no it isn't */
		spec = Math.pow(spec, result.shape.material().specular);
		final double attenuated = power / distance2;
		l.scale((float)(diff * attenuated), colour);
		specular.scale((float)(spec * attenuated), m.mirrorColor);
		l.add(specular);
		return l;
	}
}

/** "Yehiy 'or." -God */
public abstract class Light {

	private enum LightType { UNKNOWN, POINT, DIRECTIONAL, SPOT, AMBIENT }

	private   String   name;
	protected Color3f  colour;
	protected Vector3d from;
	protected float    power;

	protected final static double minSpecular = 5;

	/* do not allow instantiation */
	protected Light() { }

	/** sometimes I love Java */
	public abstract Color3f manifest(final Intersectable root, final IntersectionResult result);

	/** Creates a light from an xml definition */
	public static Light magic(Node dataNode) {
		String name;
		Color3f colour = null;
		Vector3d from  = null;
		float power    = 0;
		LightType type = LightType.UNKNOWN;
		Light light;

		name = dataNode.getAttributes().getNamedItem("name").getNodeValue();
		NodeList nodeList = dataNode.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node n = nodeList.item(i);
			// skip all text, just process the ELEMENT_NODEs
			if (n.getNodeType() != Node.ELEMENT_NODE) continue;
			String nodeName = n.getNodeName();
			NamedNodeMap attrs = n.getAttributes();
			if (nodeName.compareToIgnoreCase("color") == 0) {
				float r = Float.parseFloat(attrs.getNamedItem("r").getNodeValue());
				float g = Float.parseFloat(attrs.getNamedItem("g").getNodeValue());
				float b = Float.parseFloat(attrs.getNamedItem("b").getNodeValue());
				/* light with transparency? what does that mean?
				float a = Float.parseFloat(attrs.getNamedItem("a").getNodeValue()); */
				colour = new Color3f(r, g, b);
			} else if (nodeName.compareToIgnoreCase("from") == 0) {
				double x = Double.parseDouble(attrs.getNamedItem("x").getNodeValue());
				double y = Double.parseDouble(attrs.getNamedItem("y").getNodeValue());
				double z = Double.parseDouble(attrs.getNamedItem("z").getNodeValue());
				from = new Vector3d(x, y, z);
			} else if (nodeName.compareToIgnoreCase("power") == 0) {
				power = Float.parseFloat(attrs.getNamedItem("fval").getNodeValue());
			} else if (nodeName.compareToIgnoreCase("type") == 0) {
				String typeString = attrs.getNamedItem("sval").getNodeValue();
				if(typeString.compareToIgnoreCase("pointlight") == 0)       type = LightType.POINT;
				else if(typeString.compareToIgnoreCase("directional") == 0) type = LightType.DIRECTIONAL;
				else if(typeString.compareToIgnoreCase("spot") == 0)        type = LightType.SPOT;
				else if(typeString.compareToIgnoreCase("ambient") == 0)     type = LightType.AMBIENT;
				else                                                        type = LightType.UNKNOWN;
			}
		}
		switch(type) {
		case AMBIENT:     light = new AmbientLight(); break;
		case DIRECTIONAL: light = new DirectionalLight(); break;
		case POINT:       light = new PointLight(); break;
		default:
			System.err.print("Unsuppoted light type '"+name+"' ("+type+".)\n");
			return null;
		}
		light.name   = name;
		light.colour = colour;
		light.from   = from;
		if(type == LightType.DIRECTIONAL) light.from.normalize();
		light.power  = power;
		System.out.print("New Light: "+light+".\n");
		return light;
	}

	public String getName() { return name; }

	public String toString() { return name+" is "+this.getClass()+" from "+from; }
}
