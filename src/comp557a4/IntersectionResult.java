/** Neil Edelman -- 110121860 */

package comp557a4;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

/**
 * Use this class to store the result of an intersection, or modify it to suit
 * your needs!
 */
public class IntersectionResult {

	public Vector3d n = new Vector3d();	/* The normal at the intersection */
	public double rayParameter;         /* Parameter on the ray giving the position of the intersection */
	public Point3d intersection = new Point3d(); /* the intersection point */
	public Intersectable shape;         /* the shape */
	public Point3d eye;                 /* for doing Phong lighting, the camera */

	/** reset the ray */
	void reset(Ray ray) {
		rayParameter = Double.POSITIVE_INFINITY;
		shape = null;
		eye = ray.point;
	}

	public String toString() { return "Result "+shape+" at "+rayParameter; }
}
