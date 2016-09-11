/** Neil Edelman -- 110121860 */

package comp557a4;

import java.util.Scanner;

import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import org.w3c.dom.Node;

/**
 * Example file for a sphere... to be completed
 */
public class Sphere implements Intersectable {

	/*double radius;*/ /* encoded in transform */
	Material material;
	Matrix4d transform = new Matrix4d();
	Matrix4d fore      = new Matrix4d();
	Matrix4d back      = new Matrix4d();

	/**
	 * Creates a sphere from an xml definition
	 * 
	 * @param dataNode
	 */
	public Sphere(Node dataNode, final Scene scene, final SceneNode parent) {
		/* FIXME: 'Intersectable' should really be an abstract class,
		 * really, code reuse! */
		Node node;
		transform.setIdentity();
		if((node = dataNode.getAttributes().getNamedItem("material")) != null) {
			material = scene.materialmap.get(node.getNodeValue());
		}
		if((node = dataNode.getAttributes().getNamedItem("position")) != null) {
			String pos = node.getNodeValue();
			Scanner s = new Scanner(pos);
			Vector3d v = new Vector3d(s.nextDouble(), s.nextDouble(), s.nextDouble());
			Matrix4d T = new Matrix4d();
			T.set(v);
			transform.mul(T);
		}
		if((node = dataNode.getAttributes().getNamedItem("rotx")) != null) {
			Scanner s = new Scanner(node.getNodeValue());
			double angle = s.nextDouble();
			Matrix4d T = new Matrix4d();
			T.rotX(angle*Math.PI/180.0);
			transform.mul(T);
		}
		if((node = dataNode.getAttributes().getNamedItem("roty")) != null) {
			Scanner s = new Scanner(node.getNodeValue());
			double angle = s.nextDouble();
			Matrix4d T = new Matrix4d();
			T.rotY(angle*Math.PI/180.0);
			transform.mul(T);
		}
		if((node = dataNode.getAttributes().getNamedItem("rotz")) != null) {
			Scanner s = new Scanner(node.getNodeValue());
			double angle = s.nextDouble();
			Matrix4d T = new Matrix4d();
			T.rotZ(angle*Math.PI/180.0);
			transform.mul(T);
		}
		if((node = dataNode.getAttributes().getNamedItem("radius")) != null) {
			double radius = Double.parseDouble(node.getNodeValue());
			Matrix4d T = new Matrix4d();
			T.set(radius);
			transform.mul(T);
		}
		if((node = dataNode.getAttributes().getNamedItem("scale")) != null) {
			String Ts = node.getNodeValue();
			Scanner s = new Scanner(Ts);
			Matrix4d T = new Matrix4d();
			T.setIdentity();
			T.setElement(0, 0, s.nextDouble());
			T.setElement(1, 1, s.nextDouble());
			T.setElement(2, 2, s.nextDouble());
			transform.mul(T);
		}
		if(parent == null) fore.set(transform); else fore.mul(parent.fore, transform);
		back.invert(fore);
		System.out.print("New Sphere: "+this+".\n");
	}

	/* static allocation for intersect() */
	static private Point3d point = new Point3d();
	static private Vector3d dir = new Vector3d(), vec = new Vector3d();

	/** r(t) = p + td, x^2 = 1 => t = \frac{-dp +/- sqrt{(dp)^2 - (dd)(pp-1)}}{dd} */
	@Override
	public boolean intersect(final Ray ray, IntersectionResult result) {
		point.set(ray.point);
		dir.set(ray.direction);
		back.transform(point);
		back.transform(dir);
		vec.set(point); /* FIXME: wasteful */
		final double a2 = dir.dot(dir);
		final double b  = dir.dot(vec);
		final double c2 = vec.dot(vec) - 1;
		final double discriminant = b*b - a2*c2;
		if(discriminant < 0) return false;
		final double parametre = (-b - Math.sqrt(discriminant)) / a2;
		if(parametre < Scene.epsilon || parametre > ray.getInterval()) return false;
		/* !result -> shadow ray: occlusion true, doesn't matter what
		 * FIXME: boolean should be Color4f */
		if(result == null) return true;
		/* we hit it, but it's occluded by another hit */
		if(parametre >= result.rayParameter) return false;
		result.rayParameter = parametre;
		result.shape = this;
		result.n.set(dir);
		result.n.scale(parametre);
		result.n.add(vec);
		result.intersection.set(result.n);
		/*fore.transform(result.n); <- lighting in object space */
		fore.transform(result.intersection);
		return true;
	}

	public Matrix4d matrix() { return back; }
	public Material material() { return material; }

	public String toString() { return "S"+hashCode(); }
}
