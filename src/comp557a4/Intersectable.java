/** Neil Edelman -- 110121860 */

package comp557a4;

import javax.vecmath.Matrix4d;

/**
 * Interface for performing intersections between rays and geometry
 */
public interface Intersectable {

	/**
	 * Intersects the given ray with the geometry, and modifies the result.
	 * 
	 * @param ray
	 * @param result
	 */
	public boolean intersect(final Ray ray, IntersectionResult result);
	public Matrix4d matrix();
	public Material material();
}
