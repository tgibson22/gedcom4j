/*
 * Copyright (c) 2009-2016 Matthew R. Harrah
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package org.gedcom4j.io.reader;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.gedcom4j.exception.ParserCancelledException;
import org.gedcom4j.io.event.FileProgressEvent;
import org.gedcom4j.parser.GedcomParser;

/**
 * A reader that loads from an input stream and gives back a collection of strings representing the data therein. This
 * implementation handles little-endian Unicode data.
 * 
 * @author frizbog
 */
class UnicodeLittleEndianReader extends AbstractEncodingSpecificReader {

    /**
     * Constructor
     * 
     * @param parser
     *            the {@link GedcomParser} which is using this object to read files
     * 
     * @param byteStream
     *            the stream of data to be read
     */
    public UnicodeLittleEndianReader(GedcomParser parser, InputStream byteStream) {
        super(parser, byteStream);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<String> load() throws IOException, ParserCancelledException {
        List<String> result = new ArrayList<String>();

        StringBuilder lineBuffer = new StringBuilder();

        boolean eof = false;
        int b1 = -1;
        int b2 = -1;

        int lastB1;
        int lastB2;

        boolean beginningOfFile = true;

        while (!eof) {
            lastB1 = b1;
            lastB2 = b2;
            b1 = byteStream.read();
            b2 = byteStream.read();

            // Check for EOF
            if (b1 < 0 || b2 < 0) {
                // hit EOF - add final line buffer (last line) and get out
                addNonBlankLine(result, lineBuffer);
                break;
            }

            // If it's a byte order marker at the beginning of the file, discard it
            if (beginningOfFile && (b1 == 0xFF && b2 == 0xFE)) {
                beginningOfFile = false;
                lineBuffer.setLength(0);
                continue;
            }

            beginningOfFile = false;

            // Check for carriage returns - signify EOL
            if (b1 == 0x0D && b2 == 0x00) {
                addNonBlankLine(result, lineBuffer);
                lineBuffer.setLength(0);
                continue;
            }

            // Check for line feeds - signify EOL (unless prev char was a
            // CR)
            if (b1 == 0x0A && b2 == 0x00) {
                if (lastB1 != 0x0D || lastB2 != 0x00) {
                    addNonBlankLine(result, lineBuffer);
                    lineBuffer.setLength(0);
                }
                continue;
            }

            int unicodeChar = b2 << 8 | b1;
            lineBuffer.append(Character.valueOf((char) unicodeChar));
        }
        parser.notifyFileObservers(new FileProgressEvent(this, linesRead, true));
        return result;
    }

    /**
     * Add line to result if it is not blank
     * 
     * @param result
     *            the resulting list of lines
     * @param lineBuffer
     *            the line buffer
     * @throws ParserCancelledException
     *             if the file load is cancelled
     */
    private void addNonBlankLine(List<String> result, StringBuilder lineBuffer) throws ParserCancelledException {
        if (parser.isCancelled()) {
            throw new ParserCancelledException("File load is cancelled");
        }
        if (lineBuffer.length() > 0) {
            String s = lineBuffer.toString();
            if (STRINGS_TO_INTERN.contains(s)) {
                result.add(s.intern());
            } else {
                result.add(s);
            }
        }
        linesRead++;
        if (linesRead % parser.getReadNotificationRate() == 0) {
            parser.notifyFileObservers(new FileProgressEvent(this, linesRead, false));
        }
    }

}