/** Neil Edelman -- 110121860 */

package comp557a4;

/** Bresenham */
public class Bresenham {
	int y0, dy, x0, dx, error, ystep;
	int x, y;
	/** draws a line (0,0) to (xMax,yMax) assuming yMax >= xMax xMax,yMax >=0*/
	public Bresenham(final int xMax, final int yMax) {
		x0 = 0;
		dx = xMax;
		y0 = 0;
		dy = yMax;
		error = dx >> 1;
		ystep = xMax < 0 ? -1 : 1;
		x = x0;
		y = y0;
	}
	public boolean update() {
		error += dy;
		if(error > dx) {
			y += ystep;
			error -= dx;
			return true;
		}
		return false;
	}
}
