package org.verapdf.pd;

import org.verapdf.as.ASAtom;
import org.verapdf.cos.COSObject;

/**
 * @author Timur Kamalov
 */
public class PDObject {

	private COSObject object;

	public PDObject() {
		this.object = new COSObject();
	}

	public PDObject(final COSObject obj) {
		this.setObject(obj);
	}

	public boolean empty() {
		return object.empty();
	}

	public void clear() {
		object.clear();
	}

	public COSObject getObject() {
		return object;
	}

	public void setObject(final COSObject object) {
		this.setObject(object, true);
	}

	public void setObject(final COSObject object, final boolean update) {
		this.object = object;
		if (update) {
			updateFromObject();
		}
	}

	public boolean knownKey(final ASAtom key) {
		return object.knownKey(key);
	}

	public COSObject getKey(final ASAtom key) {
		return object.getKey(key);
	}

	public void setKey(final ASAtom key, final COSObject value) {
		object.setKey(key, value);
	}

	public String getStringKey(final ASAtom key) {
		return object.getStringKey(key);
	}

	public void setStringKey(final ASAtom key, final String value) {
		object.setStringKey(key, value);
	}

	public ASAtom getNameKey(final ASAtom key) {
		return object.getNameKey(key);
	}

	public void setNameKey(final ASAtom key, final ASAtom value) {
		object.setNameKey(key, value);
	}

	public Long getIntegerKey(final ASAtom key) {
		return object.getIntegerKey(key);
	}

	public void setIntegerKey(final ASAtom key, final Long value) {
		object.setIntegerKey(key, value);
	}

	public Boolean getBooleanKey(final ASAtom key) {
		return object.getBooleanKey(key);
	}

	public void setBooleanKey(final ASAtom key, final Boolean value) {
		object.setBooleanKey(key, value);
	}

	public void removeKey(final ASAtom key) {
		object.removeKey(key);
	}

	// VIRTUAL METHODS
	protected void updateToObject() {}
	protected void updateFromObject() {}

}
