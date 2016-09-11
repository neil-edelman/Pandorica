/** Neil Edelman -- 110121860 */

package comp557a4;

import java.awt.Dimension;

import javax.vecmath.Point2d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Simple camera object, which could be extended to handle a variety of
 * different camera settings (e.g., aperature size, lens, shutter)
 */
class Camera {

	public String name;
	public Point3d from;
	public double fovy;
	public Dimension imageSize = new Dimension();
	private Point2d size = new Point2d();
	private Vector3d axis[] = new Vector3d[3];

	/**
	 * Creates a camera from an xml definition
	 * 
	 * @param dataNode
	 */
	public Camera(Node dataNode) {
		Point3d to = new Point3d();
		Vector3d up = new Vector3d();
		name = dataNode.getAttributes().getNamedItem("name").getNodeValue();
		NodeList nodeList = dataNode.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node n = nodeList.item(i);
			// skip all text, just process the ELEMENT_NODEs
			if (n.getNodeType() != Node.ELEMENT_NODE)
				continue;
			String nodeName = n.getNodeName();
			NamedNodeMap attrs = n.getAttributes();
			if (nodeName.compareToIgnoreCase("from") == 0) {
				double x = Double.parseDouble(attrs.getNamedItem("x")
						.getNodeValue());
				double y = Double.parseDouble(attrs.getNamedItem("y")
						.getNodeValue());
				double z = Double.parseDouble(attrs.getNamedItem("z")
						.getNodeValue());
				from = new Point3d(x, y, z);
			} else if (nodeName.compareToIgnoreCase("to") == 0) {
				double x = Double.parseDouble(attrs.getNamedItem("x")
						.getNodeValue());
				double y = Double.parseDouble(attrs.getNamedItem("y")
						.getNodeValue());
				double z = Double.parseDouble(attrs.getNamedItem("z")
						.getNodeValue());
				to = new Point3d(x, y, z);
			} else if (nodeName.compareToIgnoreCase("up") == 0) {
				double x = Double.parseDouble(attrs.getNamedItem("x")
						.getNodeValue());
				double y = Double.parseDouble(attrs.getNamedItem("y")
						.getNodeValue());
				double z = Double.parseDouble(attrs.getNamedItem("z")
						.getNodeValue());
				up = new Vector3d(x, y, z);
			} else if (nodeName.compareToIgnoreCase("fovy") == 0) {
				fovy = Double.parseDouble(attrs.getNamedItem("angle")
						.getNodeValue());
			} else if (nodeName.compareToIgnoreCase("width") == 0) {
				imageSize.width = Integer.parseInt(attrs.getNamedItem("ival")
						.getNodeValue());
			} else if (nodeName.compareToIgnoreCase("height") == 0) {
				imageSize.height = Integer.parseInt(attrs.getNamedItem("ival")
						.getNodeValue());
			}
		}
		/* precompute */
		size.y = Math.tan(fovy*Math.PI/180.0);
		size.x = size.y * imageSize.width / imageSize.height;
		axis[2] = new Vector3d(from);
		axis[2].sub(to); /* the camera points in -z, like in OpenGL */
		axis[2].normalize();
		axis[1] = new Vector3d();
		axis[1].normalize(up);
		axis[0] = new Vector3d();
		axis[0].cross(axis[1], axis[2]);
		/* make then orthonormal */
		axis[0].normalize();
		axis[1].cross(axis[2], axis[0]);
		System.out.print("New Camera: "+this+".\n");
	}

	Vector3d getAxis(int i) { return axis[i]; }
	Point2d getSize() { return size; }

	public String toString() { return name+" from "+from+" axes "+axis[0]+axis[1]+axis[2]; }
}
