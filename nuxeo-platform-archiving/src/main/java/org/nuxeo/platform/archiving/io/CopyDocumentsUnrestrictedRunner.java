/*
 * Copyright (c) 2006-2011 Nuxeo SA (http://nuxeo.com/) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Mariana Cedica
 */
package org.nuxeo.platform.archiving.io;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.UnrestrictedSessionRunner;
import org.nuxeo.ecm.core.io.DocumentWriter;
import org.nuxeo.ecm.core.io.ExportedDocument;
import org.nuxeo.ecm.core.io.impl.ExportedDocumentImpl;
import org.nuxeo.ecm.core.io.impl.plugins.DocumentModelWriter;

/**
 * Opens an unrestricted session on a give repository and copies the documents
 * 
 * @since 5.6
 */
public class CopyDocumentsUnrestrictedRunner extends UnrestrictedSessionRunner {

    protected static final Log log = LogFactory.getLog(CopyDocumentsUnrestrictedRunner.class);

    protected ExportedDocument[] exportedDocs;

    protected String parentPath;

    public CopyDocumentsUnrestrictedRunner(String repositoryName) {
        super(repositoryName);
    }

    public void importDocument(DocumentModel doc, String parentPath)
            throws ClientException {
        setParentPath(parentPath);
        detachDocument(doc);
        runUnrestricted();
    }

    public void importDocuments(DocumentModelList docs, String parentPath)
            throws ClientException {
        setParentPath(parentPath);
        detachDocuments(docs);
        runUnrestricted();
    }

    @Override
    public void run() throws ClientException {
        DocumentWriter writer = new DocumentModelWriter(session, parentPath);
        try {
            writer.write(exportedDocs);
        } catch (IOException e) {
            log.error(e);
            throw new ClientException(e);
        }
    }

    protected void detachDocument(DocumentModel doc) throws ClientException {
        try {
            exportedDocs = new ExportedDocument[1];
            exportedDocs[0] = new ExportedDocumentImpl(doc, true);
        } catch (IOException e) {
            log.error(e);
            throw new ClientException();
        }
    }

    protected void detachDocuments(DocumentModelList docs)
            throws ClientException {
        try {
            exportedDocs = new ExportedDocument[docs.size()];
            for (int i = 0; i < docs.size(); i++) {
                exportedDocs[i] = new ExportedDocumentImpl(docs.get(i), true);
            }
        } catch (IOException e) {
            log.error(e);
            throw new ClientException();
        }
    }

    protected void setParentPath(String parentPath) {
        this.parentPath = parentPath;
    }
}