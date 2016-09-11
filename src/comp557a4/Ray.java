/** Neil Edelman -- 110121860 */

package comp557a4;

import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

/**
 * Simple implementation of a Ray. Extend this class if you need to.
 */
public class Ray {

	public Point3d point      = new Point3d(0, 0, 10);
	public Vector3d direction = new Vector3d(0, 0, -1);
	private double interval   = Double.POSITIVE_INFINITY;

	/** this should be called sparingly */
	public Ray() { }

	/** Creates a new ray with the given eye point and view direction */
	public Ray(Point3d eyePoint, Vector3d viewDirection) {
		this.point.set(eyePoint);
		this.direction.set(viewDirection);
	}

	/** this is ray transformed by t */
	public void transform(final Ray ray, final Matrix4d t) {
		point.set(ray.point);
		direction.set(ray.direction);
		t.transform(point);
		t.transform(direction);
	}

	public void set(Point3d eyePoint, Vector3d viewDirection) {
		this.point.set(eyePoint);
		this.direction.set(viewDirection);
		this.interval = Double.POSITIVE_INFINITY;
	}
	public void set(Point3d eyePoint, Vector3d viewDirection, double interval) {
		this.point.set(eyePoint);
		this.direction.set(viewDirection);
		this.interval = interval;
	}

	public double getInterval() { return interval; }

	public String toString() { return "Ray "+point+" going "+direction; }
}
