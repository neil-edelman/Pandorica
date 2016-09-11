/** Neil Edelman -- 110121860 */

package comp557a4;

import javax.vecmath.Color3f;
import javax.vecmath.Color4f;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Simple material definition. Extend as required (e.g., textures, refraction
 * index for transparent objects, etc.)
 */
public class Material {

	public String name;
	public Color4f color;
	public Color3f mirrorColor;
	public float specular;

	/**
	 * Creates a material from an XML definition
	 * 
	 * @param dataNode
	 */
	public Material(Node dataNode) {
		name = dataNode.getAttributes().getNamedItem("name").getNodeValue();
		NodeList nodeList = dataNode.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node n = nodeList.item(i);
			// skip all text, just process the ELEMENT_NODEs
			if (n.getNodeType() != Node.ELEMENT_NODE)
				continue;
			String nodeName = n.getNodeName();
			NamedNodeMap attrs = n.getAttributes();
			if (nodeName.compareToIgnoreCase("color") == 0) {
				float r = Float.parseFloat(attrs.getNamedItem("r")
						.getNodeValue());
				float g = Float.parseFloat(attrs.getNamedItem("g")
						.getNodeValue());
				float b = Float.parseFloat(attrs.getNamedItem("b")
						.getNodeValue());
				float a = Float.parseFloat(attrs.getNamedItem("a")
						.getNodeValue());
				color = new Color4f(r, g, b, a);
			} else if (nodeName.compareToIgnoreCase("mirror_color") == 0) {
				float r = Float.parseFloat(attrs.getNamedItem("r")
						.getNodeValue());
				float g = Float.parseFloat(attrs.getNamedItem("g")
						.getNodeValue());
				float b = Float.parseFloat(attrs.getNamedItem("b")
						.getNodeValue());
				/*float a = Float.parseFloat(attrs.getNamedItem("a")
						.getNodeValue()); doesn't make sense */
				mirrorColor = new Color3f(r, g, b);
			} else if (nodeName.compareToIgnoreCase("specular_reflect") == 0) {
				specular = Float.parseFloat(attrs.getNamedItem("fval")
						.getNodeValue());
			}
		}
		if(mirrorColor == null) mirrorColor = new Color3f(color.x, color.y, color.z);
		System.out.print("New Material: "+this+".\n");
	}

	public String toString() { return name+" is "+color+" with highlight "+specular; }
}
