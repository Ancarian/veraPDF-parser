package org.verapdf.cos;

import org.apache.log4j.Logger;
import org.verapdf.cos.visitor.Writer;
import org.verapdf.cos.xref.COSXRefTable;
import org.verapdf.io.IReader;
import org.verapdf.io.Reader;
import org.verapdf.pd.PDDocument;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Timur Kamalov
 */
public class COSDocument {

	private static final Logger LOGGER = Logger.getLogger(COSDocument.class);

	private PDDocument doc;
	private IReader reader;
	private COSHeader header;
	private COSBody body;
	private COSXRefTable xref;
	private COSTrailer trailer;
	private COSTrailer firstTrailer;
	private COSTrailer lastTrailer;
	private boolean linearized;
	private boolean isNew;

	private byte postEOFDataSize;

	private boolean xrefEOLMarkersComplyPDFA;
	private boolean subsectionHeaderSpaceSeparated;

	public COSDocument(final PDDocument document) {
		this.doc = document;
		this.header = new COSHeader();
		this.body = new COSBody();
		this.xref = new COSXRefTable();
		this.trailer = new COSTrailer();
		this.firstTrailer = new COSTrailer();
		this.lastTrailer = new COSTrailer();
		this.linearized = false;
		this.isNew = true;

		this.xrefEOLMarkersComplyPDFA = true;
		this.subsectionHeaderSpaceSeparated = true;
	}

	public COSDocument(final String fileName, final PDDocument document) throws IOException {
		initReader(fileName);

		initCOSDocument(document);
	}

	public COSDocument(final InputStream fileStream, final PDDocument document) throws IOException {
		initReader(fileStream);

		initCOSDocument(document);
	}

	private void initCOSDocument(final PDDocument document) {
		this.doc = document;
		this.body = new COSBody();

		this.header = this.reader.getHeader();
		this.xref = new COSXRefTable();
		this.xref.set(this.reader.getKeys());
		this.trailer = reader.getTrailer();
		this.firstTrailer = reader.getFirstTrailer();
		this.lastTrailer = reader.getLastTrailer();
		this.linearized = reader.isLinearized();

		this.xrefEOLMarkersComplyPDFA = true;
		this.subsectionHeaderSpaceSeparated = true;
	}

	private void initReader(final InputStream fileStream) throws IOException {
		this.reader = new Reader(this, fileStream);
	}

	private void initReader(final String fileName) throws IOException {
		this.reader = new Reader(this, fileName);
	}

	public boolean isNew() {
		return this.isNew;
	}

	public void setHeader(String header) {
		this.header.setHeader(header);
	}

	public List<COSObject> getObjects() {
		List<COSObject> result = new ArrayList<>();
		for (COSKey key : this.xref.getAllKeys()) {
			COSObject obj = this.body.get(key);
			if (!obj.empty()) {
				result.add(obj);
			} else {
				try {
					COSObject newObj = this.reader.getObject(key);

					this.body.set(key, newObj);
					result.add(newObj);
				} catch (IOException e) {
					LOGGER.warn("Error while parsing object : " + key.getNumber() +
							" " + key.getGeneration());
				}
			}
		}
		return result;
	}

	public Map<COSKey, COSObject> getObjectsMap() {
		Map<COSKey, COSObject> result = new HashMap();
		for (COSKey key : this.xref.getAllKeys()) {
			COSObject obj = this.body.get(key);
			if (!obj.empty()) {
				result.put(key, obj);
			} else {
				try {
					COSObject newObj = this.reader.getObject(key);

					this.body.set(key, newObj);
					result.put(key, newObj);
				} catch (IOException e) {
					LOGGER.warn("Error while parsing object : " + key.getNumber() +
							" " + key.getGeneration());
				}
			}
		}
		return result;
	}

	public COSObject getObject(final COSKey key) {
		try {
			COSObject obj = this.body.get(key);
			if (!obj.empty()) {
				return obj;
			}

			COSObject newObj = this.reader.getObject(key);
			if (newObj == null) {
				return null;
			}
			this.body.set(key, newObj);
			return this.body.get(key);
		} catch (IOException e) {
			//TODO : maybe not runtime, maybe no exception at all
			throw new RuntimeException("Error while parsing object : " + key.getNumber() +
									   " " + key.getGeneration());
		}
	}

	public void setObject(final COSKey key, final COSObject obj) {
		this.body.set(key, obj);
		this.xref.newKey(key);
	}

	public COSKey setObject(COSObject obj) {
		COSKey key = obj.getKey();

		//TODO : fix this method for document save
		if (key.getNumber() == 0 && key.getGeneration() == 0) {
			key = this.xref.next();
			this.body.set(key, obj.getDirect());
			obj = COSIndirect.construct(key, this);
		}

		this.xref.newKey(key);
		return key;
	}

	public COSTrailer getTrailer() {
		return this.trailer;
	}

	public COSTrailer getFirstTrailer() {
		return firstTrailer;
	}

	public COSTrailer getLastTrailer() {
		return lastTrailer;
	}

	public boolean isLinearized() {
		return linearized;
	}

	public PDDocument getPDDocument() {
		return this.doc;
	}

	public COSHeader getHeader() {
		return header;
	}

	public void setHeader(COSHeader header) {
		this.header = header;
	}

	public byte getPostEOFDataSize() {
		return postEOFDataSize;
	}

	public void setPostEOFDataSize(byte postEOFDataSize) {
		this.postEOFDataSize = postEOFDataSize;
	}

	public boolean isXrefEOLMarkersComplyPDFA() {
		return xrefEOLMarkersComplyPDFA;
	}

	public void setXrefEOLMarkersComplyPDFA(boolean xrefEOLMarkersComplyPDFA) {
		this.xrefEOLMarkersComplyPDFA = xrefEOLMarkersComplyPDFA;
	}

	public boolean isSubsectionHeaderSpaceSeparated() {
		return subsectionHeaderSpaceSeparated;
	}

	public void setSubsectionHeaderSpaceSeparated(boolean subsectionHeaderSpaceSeparated) {
		this.subsectionHeaderSpaceSeparated = subsectionHeaderSpaceSeparated;
	}

	public void save() {
		//TODO : implement this
	}

	public void saveAs(final Writer writer) {
		writer.writeHeader(this.header.getHeader());

		writer.addToWrite(this.xref.getAllKeys());
		writer.writeBody();

		writer.setTrailer(this.trailer);

		writer.writeXRefInfo();

		writer.clear();
	}

}
