package org.verapdf.pd.colors;

import org.verapdf.cos.COSObject;
import org.verapdf.factory.colors.ColorSpaceFactory;
import org.verapdf.pd.PDResources;

/**
 * Base class for special color spaces, see 8.6.6 of PDF-1.7 specification.
 *
 * @author Sergey Shemyakov
 */
public abstract class PDSpecialColorSpace extends PDColorSpace {

    private final PDResources resources;

    /**
     * Constructor from colorspace COSObject and resources.
     */
    public PDSpecialColorSpace(COSObject obj, PDResources resources) {
        super(obj);
        this.resources = resources;
    }

    protected PDColorSpace getBaseColorSpace() {
        return ColorSpaceFactory.getColorSpace(
                getBaseColorSpaceObject(), this.resources);
    }

    abstract COSObject getBaseColorSpaceObject();
}
