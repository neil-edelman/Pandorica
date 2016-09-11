/** Neil Edelman -- 110121860 */

package comp557a4;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.StringTokenizer;

import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import org.w3c.dom.Node;

/**
 * Simple implementation of a loader for a polygon soup
 */
public class PolygonSoup implements Intersectable {

	String file;
	double radius;
	Material material;
	Matrix4d transform = new Matrix4d();
	Matrix4d fore      = new Matrix4d();
	Matrix4d back      = new Matrix4d();

	private final static boolean replaceByBounding = false;

	private class Vertex {
		private Point3d p = new Point3d();
		private Vector3d n;
	}
	
	private class Face {
		private Vertex a, b, c;
		private Vector3d ab = new Vector3d(), bc = new Vector3d(), ca = new Vector3d();
		private Vector3d n = new Vector3d();
		private double d;
		private double area;

		Face(int[] v) {
			a = vertexList.get(v[0]);
			b = vertexList.get(v[1]);
			c = vertexList.get(v[2]);
			ab.sub(b.p, a.p);
			bc.sub(c.p, b.p);
			ca.sub(a.p, c.p);
			n.cross(ab, bc);
			n.normalize();
			Vector3d vec_a = new Vector3d(a.p); /* no Point3d.dot? that's stupid */
			d = n.dot(vec_a);
			d = -d;
			Vector3d ac = new Vector3d(), cross = new Vector3d();
			ac.sub(c.p, a.p);
			cross.cross(ab, ac);
			area = 1/n.dot(cross);
		}
	}

	public List<Vertex> vertexList = new ArrayList<Vertex>();
	public List<Face> faceList = new ArrayList<Face>();

	public Matrix4d matrix() { return back; }
	public Material material() { return material; }

	private static Ray r = new Ray();
	private static Vector3d vec = new Vector3d(), p = new Vector3d();
	private static Vector3d ap = new Vector3d(), bp = new Vector3d(), cp = new Vector3d();
	private static Vector3d interpolate = new Vector3d(), cross = new Vector3d();
	@Override
	public boolean intersect(final Ray ray, IntersectionResult result) {
		r.transform(ray, back);
		if(!bound(r)) return false; /* quick easy-out */
		if(replaceByBounding) {
			if(result == null) return true;
			p.set(0,0,0);
			result.intersection.set(p);
			return true;
		}
		Face face = null;
		double t = 0, a = 0, b = 0, c = 0;
		for(Face f : faceList) {
			/* backface cull */
			if(f.n.dot(r.direction) >= 0) continue;
			/* intersect plane */
			vec.set(r.point); /* totally unnecessary! */
			t = -(vec.dot(f.n) + f.d)/(r.direction.dot(f.n));
			if(t <= 0) continue;
			/*p.set(eye);p.scaleAdd(t, r.direction); scaleAdd() doesn't do what I thought */
			p.set(r.direction);
			p.scale(t);
			p.add(vec);
			/* are we in the triangle? find out using barycentric coords */
			ap.sub(p, f.a.p);
			bp.sub(p, f.b.p);
			cp.sub(p, f.c.p);
			cross.cross(f.ab, ap);
			if((c = cross.dot(f.n)) < 0) continue;
			cross.cross(f.bc, bp);
			if((a = cross.dot(f.n)) < 0) continue;
			cross.cross(f.ca, cp);
			if((b = cross.dot(f.n)) < 0) continue; /* FIXME: only two need to be computed */
			/* keep track */
			face = f;
			c *= face.area;
			a *= face.area;
			b *= face.area;
			break;
		}
		if(face == null) return false; /* there were no hits */
		if(result == null) return true;
		if(t >= result.rayParameter) return false;
		result.rayParameter = t;
		result.shape = this;
		/* interpolate normals */
		interpolate.scale(a, face.a.n);
		result.n.set(interpolate);
		interpolate.scale(b, face.b.n);
		result.n.add(interpolate);
		interpolate.scale(c, face.c.n);
		result.n.add(interpolate);
		result.n.normalize();
		fore.transform(p);
		result.intersection.set(p);
		return true;
	}

	private boolean bound(final Ray r) {
		vec.set(r.point);
		final Vector3d dir = r.direction;
		final double dp = vec.dot(dir);
		return (dp*dp - dir.dot(dir)*(vec.dot(vec) - radius) >= 0) ? true : false;
	}

	public PolygonSoup(Node dataNode, final Scene scene, final SceneNode parent) {
		Node node;
		Vector3d v = new Vector3d();
		transform.setIdentity();
		file = dataNode.getAttributes().getNamedItem("file").getNodeValue();
		load(A4App.directory + file);
		if((node = dataNode.getAttributes().getNamedItem("material")) != null) {
			material = scene.materialmap.get(node.getNodeValue());
		}
		if((node = dataNode.getAttributes().getNamedItem("position")) != null) {
			String pos = node.getNodeValue();
			Scanner s = new Scanner(pos);
			v.set(s.nextDouble(), s.nextDouble(), s.nextDouble());
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
		} else { /* cow pointing */
			Matrix4d T = new Matrix4d();
			double a = Math.atan2(v.x, v.z);
			T.rotY(a);
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
		System.out.print("New Mesh: "+this+".\n");
	}

	/** Creates a polygon soup by loading an OBJ file */
	private void load(String file) {
		try {
			FileInputStream fis = new FileInputStream(file);
			InputStreamReader isr = new InputStreamReader(fis);
			BufferedReader reader = new BufferedReader(isr);
			String line;
			Vertex v;
			int i = 0;
			while ((line = reader.readLine()) != null) {
				if (line.startsWith("v ")) {
					vertexList.add(parseVertex(line));
				} else if (line.startsWith("f ")) {
					faceList.add(new Face(parseFace(line)));
				} else if(line.startsWith("vn ") && (v = vertexList.get(i++)) != null) {
					parseNormal(line, v);
				}
			}
			reader.close();
			isr.close();
			fis.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Parses a vertex definition from a line in an obj file. Assumes that there
	 * are three components.
	 * 
	 * @param newline
	 * @return a new vertex object
	 */
	private Vertex parseVertex(String newline) {
		// Remove the tag "v "
		newline = newline.substring(2, newline.length());
		StringTokenizer st = new StringTokenizer(newline, " ");
		Vertex v = new Vertex();
		v.p.x = Double.parseDouble(st.nextToken());
		v.p.y = Double.parseDouble(st.nextToken());
		v.p.z = Double.parseDouble(st.nextToken());
		/* very crude! */
		vec.set(v.p);
		double r = vec.length()*1.414;
		if(r > radius) radius = r;
		return v;
	}

	private void parseNormal(String newline, Vertex v) {
		// Remove the tag "vn "
		newline = newline.substring(3, newline.length());
		StringTokenizer st = new StringTokenizer(newline, " ");
		v.n = new Vector3d();
		v.n.x = Double.parseDouble(st.nextToken());
		v.n.y = Double.parseDouble(st.nextToken());
		v.n.z = Double.parseDouble(st.nextToken());
	}

	/**
	 * Gets the list of indices for a face from a string in an obj file. Simply
	 * ignores texture and normal information for simplicity
	 * 
	 * @param newline
	 * @return list of indices
	 */
	private int[] parseFace(String newline) {
		// Remove the tag "f "
		newline = newline.substring(2, newline.length());
		// vertex/texture/normal tuples are separated by a spaces.
		StringTokenizer st = new StringTokenizer(newline, " ");
		int count = st.countTokens();
		/* FIXME: divide into triangles! */
		if(count != 3) System.err.print("Not triangle! will probably crash.\n");
		int v[] = new int[count];
		for (int i = 0; i < count; i++) {
			// first token is vertex index... we'll ignore the rest
			StringTokenizer st2 = new StringTokenizer(st.nextToken(), "/");
			v[i] = Integer.parseInt(st2.nextToken()) - 1; // want zero indexed
															// vertices!
		}
		return v;
	}

	public String toString() { return "M"+file+hashCode()+"r"+radius; }
}
