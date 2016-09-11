/** Neil Edelman -- 110121860 */

package comp557a4;

import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * The scene is constructed from a hierarchy of nodes, where each node contains
 * a transform, a material definition, some amount of geometry, and some number
 * of children nodes. Each node has a unique name so that it can be instanced
 * elsewhere in the hierarchy (provided it does not make loops, or if it does
 * make loops, then you
 */
public class SceneNode implements Intersectable {

	List<Intersectable> children = new LinkedList<Intersectable>();

	String name;

	Matrix4d transform = new Matrix4d();
	Matrix4d fore      = new Matrix4d();
	Matrix4d back      = new Matrix4d();

	@Override
	public boolean intersect(Ray ray, IntersectionResult result) {
		boolean hit = false;
		for(Intersectable child : children) {
			if(child.intersect(ray, result)) {
				if(result == null) return true; /* short circuit */
				hit = true;
			}
		}
		return hit;
	}

	/**
	 * Creates a new scene node from an xml definition Note that all nodes must
	 * have a unique name, and must end up in the nodeMap so that they can used
	 * as an instance later on.
	 * 
	 * @param dataNode
	 * @param nodeMap
	 */
	public SceneNode(Node dataNode, final Scene scene, final SceneNode parent) {
		transform.setIdentity();
		name = dataNode.getAttributes().getNamedItem("name").getNodeValue();

		Node node;
		transform.setIdentity();
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
		System.out.print("New SceneNode: "+this+".\n");

		NodeList nodeList = dataNode.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node n = nodeList.item(i);
			// skip all text, just process the ELEMENT_NODEs
			if (n.getNodeType() != Node.ELEMENT_NODE) continue;
			String nodeName = n.getNodeName();
			if (nodeName.compareToIgnoreCase("node") == 0) {
				SceneNode sceneNode = new SceneNode(n, scene, this);
				children.add(sceneNode);
				scene.nodeMap.put(sceneNode.name, sceneNode);
			}
			/* FIXME: this is ridiculous, hard-coded! have shapes call a
			 * registration function that adds a shape type */
			else if (nodeName.compareToIgnoreCase("sphere") == 0) {
				children.add(new Sphere(n, scene, this));
			} else if (nodeName.compareToIgnoreCase("cube") == 0) {
				children.add(new Cube(n, scene, this));
			} else if (nodeName.compareToIgnoreCase("ground") == 0) {
				children.add(new Ground(n, scene, this));
			} else if (nodeName.compareToIgnoreCase("instance") == 0) {
				children.add(new Instance(n, scene, this));
			} else if (nodeName.compareToIgnoreCase("mesh") == 0) {
				children.add(new PolygonSoup(n, scene, this));
			}
		}
	}

	public Matrix4d matrix() { return back; }
	public Material material() { return null; }

	public String toString() { return name; }
}
