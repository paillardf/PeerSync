
package com.peersync.network.content.model;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import net.jxta.document.Document;
import net.jxta.document.MimeMediaType;
import net.jxta.document.StructuredDocumentFactory;

/**
 * This class presents a Document interface for a specific file on disk.
 */
public class FolderDocument implements Document {

    private final static int BUFFER_SIZE = 4096;


    /**
     *  MIME media type of this document.
     **/
    private final MimeMediaType type;

    /**
     * Create a new File Document.
     **/
    public FolderDocument() {
        this(StructuredDocumentFactory.getMimeTypeForFileExtension("syncFolder"));
    }

    /**
     * Create a new File Document.
     */
    public FolderDocument(MimeMediaType type) {
     
        this.type = type.intern();
    }

    @Deprecated
    public String getFileExtension() {
       return "syncFolder";
    }

    
    /**
     *  {@inheritDoc}
     **/
    public MimeMediaType getMimeType() {
        return type;
    }

    /**
     *  {@inheritDoc}
     **/
    public InputStream getStream() throws IOException {
        return new FileInputStream("syncFolder");
    }

    /**
     *  {@inheritDoc}
     **/
    public void sendToStream(OutputStream sink) throws IOException {
        InputStream source = getStream();
        int c;
        byte[] buf = new byte[BUFFER_SIZE];

        do {
            c = source.read(buf);

            if (-1 == c) {
                break;
            }

            sink.write(buf, 0, c);
        } while (true);
    }
}
