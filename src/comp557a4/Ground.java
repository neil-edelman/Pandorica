/** Neil Edelman -- 110121860 */

package comp557a4;

import java.util.Scanner;

import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import org.w3c.dom.Node;

/* FIXME: normal mapping for the ground? */

/**
 * Example file for a sphere... to be completed
 */
public class Ground implements Intersectable {

	double level;
	Material material;
	Matrix4d transform = new Matrix4d();
	Matrix4d fore      = new Matrix4d();
	Matrix4d back      = new Matrix4d();

	/**
	 * Creates a sphere from an xml definition
	 * 
	 * @param dataNode
	 */
	public Ground(Node dataNode, final Scene scene, final SceneNode parent) {
		Node node;
		transform.setIdentity();
		if((node = dataNode.getAttributes().getNamedItem("material")) != null) {
			material = scene.materialmap.get(node.getNodeValue());
		}
		if((node = dataNode.getAttributes().getNamedItem("level")) != null) {
			level = Double.parseDouble(node.getNodeValue());
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
		if(parent == null) fore.set(transform); else fore.mul(parent.fore, transform);
		back.invert(fore);
		System.out.print("New Ground: "+this+".\n");
	}

	/* static allocation for intersect() */
	static private Ray r = new Ray();

	static private final Vector3d
		nYMin = new Vector3d(0, -1, 0),
		nYMax = new Vector3d(0, 1, 0);

	@Override
	public boolean intersect(final Ray ray, IntersectionResult result) {
		r.transform(ray, back);
		double y;
		if(r.direction.y == 0) return false;
		y = (level - r.point.y) / r.direction.y;
		if(y < Scene.epsilon || y >= ray.getInterval()) return false;
		Vector3d n = (r.point.y > 0) ? nYMax : nYMin;
		if(result == null) return true;
		if(y >= result.rayParameter) return false;
		result.rayParameter = y;
		result.shape = this;
		result.intersection.scale(y, r.direction);
		result.intersection.add(r.point);
		result.n.set(normalMap(result.intersection, 256, n));
		fore.transform(result.intersection);
		return true;
	}

	private static Vector3d n = new Vector3d();
	private Vector3d normalMap(final Point3d p, final double scale, final Vector3d nOld) {
		//n.set(Math.random()-0.5, Math.random()-0.5, Math.random()-0.5);
		/* it's mowed */
		n.set(((byte)(p.x*scale)^(byte)(p.y*scale))/256d-0.5, 0, 0);
		n.scale(0.3);
		n.add(nOld);
		n.normalize();
		return n;
	}

	public Matrix4d matrix() { return back; }
	public Material material() { return material; }

	public String toString() { return "G"+hashCode()+" at "+level; }
}
