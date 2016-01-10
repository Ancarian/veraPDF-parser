package org.verapdf.pd;

import org.verapdf.as.ASAtom;
import org.verapdf.cos.COSObject;

/**
 * @author Timur Kamalov
 */
public class PDPageTreeNode extends PDObject {

	private PDPageTreeBranch parent;

	public PDPageTreeNode() {
		this.parent = new PDPageTreeBranch();
	}

	public PDPageTreeNode(final COSObject obj) {
		super();
		parent = new PDPageTreeBranch();
		setObject(obj);
	}

	public PDPageTreeBranch getParent() {
		return this.parent;
	}

	public void setParent(final PDPageTreeBranch parent) {
		this.parent = parent;
		if (parent != null) {
			getObject().setKey(ASAtom.PARENT, parent.getObject());
		}
	}

	public int getLeafCount() {
		return 1;
	}

	public PDPageTreeBranch findTerminal(int index) {
		if (parent == null) {
			return null;
		}

		index += this.parent.getIndex(this);
		return parent;
	}
}
