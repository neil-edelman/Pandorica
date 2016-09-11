/** Neil Edelman -- 110121860 */

package comp557a4;

import org.w3c.dom.Node;

/**
 * An object to define the image and parameters specific to this render job. For
 * instance, in addition to a camera, this could also include parameters to
 * specify how to perform anti aliasing, the maximum recursion depth for
 * reflection and refraction rays, a gamma value for creating the image, or any
 * other parameter that influences image generation at this level.
 */
public class Render {
	public String cameraName;
	public String output;

	/** Creates a rendered image definition */
	public Render(Node dataNode) {
		output = dataNode.getAttributes().getNamedItem("output").getNodeValue();
		cameraName = dataNode.getAttributes().getNamedItem("camera").getNodeValue();
	}
}