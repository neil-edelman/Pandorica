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
public class Cube implements Intersectable {

	Material material;
	Matrix4d transform = new Matrix4d();
	Matrix4d fore      = new Matrix4d();
	Matrix4d back      = new Matrix4d();
	Point3d min = new Point3d(), max = new Point3d();	

	/**
	 * Creates a sphere from an xml definition
	 * 
	 * @param dataNode
	 */
	public Cube(Node dataNode, final Scene scene, final SceneNode parent) {
		Node node;
		transform.setIdentity();
		if((node = dataNode.getAttributes().getNamedItem("material")) != null) {
			material = scene.materialmap.get(node.getNodeValue());
		}
		{
			node = dataNode.getAttributes().getNamedItem("min");
			String pos = node.getNodeValue();
			Scanner s = new Scanner(pos);
			min.set(s.nextDouble(), s.nextDouble(), s.nextDouble());
		}
		{
			node = dataNode.getAttributes().getNamedItem("max");
			String pos = node.getNodeValue();
			Scanner s = new Scanner(pos);
			max.set(s.nextDouble(), s.nextDouble(), s.nextDouble());
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
		/* assert(min > max); */
		if(parent == null) fore.set(transform); else fore.mul(parent.fore, transform);
		back.invert(fore);
		System.out.print("New Cube: "+this+".\n");
	}

	/* static allocation for intersect() */
	static private Point3d intersection = new Point3d();
	static private Ray r = new Ray();

	static private final Vector3d
	nXMin = new Vector3d(-1, 0, 0),
	nYMin = new Vector3d(0, -1, 0),
	nZMin = new Vector3d(0, 0, -1),
	nXMax = new Vector3d(1, 0, 0),
	nYMax = new Vector3d(0, 1, 0),
	nZMax = new Vector3d(0, 0, 1);

	/** intersects: sometimes I have Java */
	@Override
	public boolean intersect(final Ray ray, IntersectionResult result) {
		r.transform(ray, back);

		/* slab normal {-x, x } */
		double x = Double.NaN, xFar = Double.NaN;
		Vector3d nx = null;
		if(r.point.x < min.x) {
			if(r.direction.x > 0) {
				x    = (min.x - r.point.x) / r.direction.x;
				xFar = (max.x - r.point.x) / r.direction.x;
				nx = nXMin;
			} else return false; /* r going away from cube */
		} else if(r.point.x > max.x) {
			if(r.direction.x < 0) {
				x    = (max.x - r.point.x) / r.direction.x;
				xFar = (min.x - r.point.x) / r.direction.x;
				nx = nXMax;
			} else return false;
		} else if(r.direction.x < 0) { /* in the middle */
			xFar = (min.x - r.point.x) / r.direction.x;
		} else {
			/* I heard a rumour that double divide-by-zero is not
			 * an error in Java, I guess we'll see */
			xFar = (max.x - r.point.x) / r.direction.x;
		}

		/* slab normal {-y, y } */
		double y = Double.NaN, yFar = Double.NaN;
		Vector3d ny = null;
		if(r.point.y < min.y) {
			if(r.direction.y > 0) {
				y    = (min.y - r.point.y) / r.direction.y;
				yFar = (max.y - r.point.y) / r.direction.y;
				ny = nYMin;
			} else return false;
		} else if(r.point.y > max.y) {
			if(r.direction.y < 0) {
				y    = (max.y - r.point.y) / r.direction.y;
				yFar = (min.y - r.point.y) / r.direction.y;
				ny = nYMax;
			} else return false;
		} else if(r.direction.y < 0) {
			yFar = (min.y - r.point.y) / r.direction.y;
		} else {
			yFar = (max.y - r.point.y) / r.direction.y;
		}

		/* slab normal {-z, z } */
		double z = Double.NaN, zFar = Double.NaN;
		Vector3d nz = null;
		if(r.point.z < min.z) {
			if(r.direction.z > 0) {
				z    = (min.z - r.point.z) / r.direction.z;
				zFar = (max.z - r.point.z) / r.direction.z;
				nz = nZMin;
			} else return false;
		} else if(r.point.z > max.z) {
			if(r.direction.z < 0) {
				z    = (max.z - r.point.z) / r.direction.z;
				zFar = (min.z - r.point.z) / r.direction.z;
				nz = nZMax;
			} else return false;
		} else if(r.direction.z < 0) {
			zFar = (min.z - r.point.z) / r.direction.z;
		} else {
			zFar = (max.z - r.point.z) / r.direction.z;
		}

		/* combine max(mins) */
		double a;
		Vector3d n;
		if(Double.isNaN(x)) {
			if(Double.isNaN(y)) {
				if(Double.isNaN(z)) return false; /* eye inside cube! */
				if(z >= xFar || z >= yFar) return false;
				a = z;
				n = nz;
			} else if(Double.isNaN(z) || y >= z) {
				if(y >= xFar || y >= zFar) return false;
				a = y;
				n = ny;
			} else {
				if(z >= xFar || z >= yFar) return false;
				a = z;
				n = nz;
			}
		} else if(Double.isNaN(y)) {
			if(Double.isNaN(z) || x >= z) {
				if(x >= yFar || x >= zFar) return false;
				a = x;
				n = nx;
			} else {
				if(z >= xFar || z >= yFar) return false;
				a = z;
				n = nz;
			}
		} else if(Double.isNaN(z)) {
			if(x >= y) {
				if(x >= yFar || x >= zFar) return false;
				a = x;
				n = nx;
			} else {
				if(y >= xFar || y >= zFar) return false;
				a = y;
				n = ny;
			}
		} else if(x >= y) {
			if(x >= z) {
				if(x >= yFar || x >= zFar) return false;
				a = x;
				n = nx;
			} else {
				if(z >= xFar || z >= yFar) return false;
				a = z;
				n = nz;
			}
		} else {
			if(y >= z) {
				if(y >= xFar || y >= zFar) return false;
				a = y;
				n = ny;
			} else {
				if(z >= xFar || z >= yFar) return false;
				a = z;
				n = nz;
			}
		}

		if(a < Scene.epsilon || a > ray.getInterval()) return false; /* I don't think the first one's possible */
		if(result == null) return true;
		if(a >= result.rayParameter) return false;
		result.rayParameter = a;
		result.shape = this;
		result.n.set(n);
		intersection.scale(a, r.direction);
		intersection.add(r.point);
		result.intersection.set(intersection);
		fore.transform(result.intersection);
		return true;
	}

	public Matrix4d matrix() { return back; }
	public Material material() { return material; }

	public String toString() { return "C"+hashCode()+" "+min+"-"+max; }
}
