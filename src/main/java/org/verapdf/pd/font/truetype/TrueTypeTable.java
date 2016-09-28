package org.verapdf.pd.font.truetype;

import org.verapdf.io.SeekableStream;

import java.io.IOException;

/**
 * This is base class for all True Type table parsers.
 *
 * @author Sergey Shemyakov
 */
abstract class TrueTypeTable extends TrueTypeBaseParser {

    protected long offset;

    protected TrueTypeTable(SeekableStream source, long offset) {
        super(source);
        this.offset = offset;
    }

    /**
     * Empty constructor for inherited classes. Should be used to set Table
     * values to default if table is not present in font program.
     */
    protected TrueTypeTable() {}

    /**
     * This method extracts all the data needed from table.
     *
     * @throws IOException if stream-reading error occurs.
     */
    abstract void readTable() throws IOException;
}
