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
package org.nuxeo.platform.archiving.operation;

import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.PathRef;
import org.nuxeo.ecm.core.api.impl.DocumentModelListImpl;
import org.nuxeo.platform.archiving.io.CopyDocumentsUnrestrictedRunner;

/**
 * Operation to copy documents in target Repository. The parentPath is the path
 * where to write the tree , if no parentPath is specified then the root is used
 * 
 * @since 5.6
 */
@Operation(id = CopyDocumentsInTargetRepository.ID, category = Constants.CAT_DOCUMENT, label = "Import in new repository", description = "Import the input document into the target repository.")
public class CopyDocumentsInTargetRepository {

    public static final String ID = "Document.CopyInRepository";

    protected static final PathRef EMPTY_PATH = new PathRef("");

    @Context
    protected CoreSession session;

    @Param(name = "targetRepositoryName", required = true)
    protected String targetRepositoryName;

    @Param(name = "parentPath", required = false)
    protected String parentPath;

    @OperationMethod
    public DocumentModel run(DocumentModel doc) throws Exception {
        new CopyDocumentsUnrestrictedRunner(targetRepositoryName).importDocument(
                doc,
                parentPath == null || EMPTY_PATH.equals(parentPath) ? session.getRootDocument().getPathAsString()
                        : parentPath);
        return session.getDocument(doc.getRef());
    }

    @OperationMethod
    public DocumentModelList run(DocumentModelList docs) throws Exception {
        new CopyDocumentsUnrestrictedRunner(targetRepositoryName).importDocuments(
                docs,
                parentPath == null || EMPTY_PATH.equals(parentPath) ? session.getRootDocument().getPathAsString()
                        : parentPath);
        DocumentModelList importedDocs = new DocumentModelListImpl();
        for (DocumentModel documentModel : docs) {
            importedDocs.add(session.getDocument(documentModel.getRef()));
        }
        return importedDocs;
    }
}
