/** Neil Edelman -- 110121860 */

package comp557a4;

import java.util.Scanner;

import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;

import org.w3c.dom.Node;

/**
 * Example file for a sphere... to be completed
 */
public class Instance implements Intersectable {

	String name;
	SceneNode sn;
	Matrix4d transform = new Matrix4d();
	Matrix4d fore      = new Matrix4d();
	Matrix4d back      = new Matrix4d();

	/**
	 * Creates a sphere from an xml definition
	 * 
	 * @param dataNode
	 */
	public Instance(Node dataNode, final Scene scene, final SceneNode parent) {
		Node node;
		transform.setIdentity();
		name = dataNode.getAttributes().getNamedItem("name").getNodeValue();
		sn = scene.nodeMap.get(name);
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
		if(parent == null) fore.set(transform); else fore.mul(parent.fore, transform);
		back.invert(fore);
		System.out.print("New Instance: "+this+".\n");
	}

	/* static allocation for intersect() */
	static private Ray r = new Ray();

	/** this calls all the other ones;
	 * FIXME: sn.intersect returns true on shadow rays
	 * (result == null) sometimes when it's not supposed
	 * to */
	@Override
	public boolean intersect(final Ray ray, IntersectionResult result) {
		r.transform(ray, back);
		if(!sn.intersect(r, result)) return false;
		if(result == null) return true;
		fore.transform(result.intersection);
		return true;
	}

	public Matrix4d matrix() { return back; }
	/* "Do something reasonable with the material definition of an instance
	 * (i.e., replace the material of the intersection, or modulate
	 * the colours)." the ONLY thing that makes sense is to not have
	 * the Instance have a Material (a blue cow? really) */
	public Material material() { return null; }

	public String toString() { return name+hashCode()+"("+sn+")"; }
}
