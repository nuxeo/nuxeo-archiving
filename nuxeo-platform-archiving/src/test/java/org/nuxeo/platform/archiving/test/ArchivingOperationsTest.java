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
package org.nuxeo.platform.archiving.test;

import static org.junit.Assert.assertEquals;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.automation.AutomationService;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreInstance;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.NuxeoPrincipal;
import org.nuxeo.ecm.core.api.SortInfo;
import org.nuxeo.ecm.core.test.CoreFeature;
import org.nuxeo.ecm.platform.query.api.PageProvider;
import org.nuxeo.ecm.platform.query.api.PageProviderService;
import org.nuxeo.ecm.platform.query.nxql.CoreQueryDocumentPageProvider;
import org.nuxeo.ecm.platform.query.nxql.NXQLQueryBuilder;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;
import org.nuxeo.runtime.test.runner.LocalDeploy;

import com.google.inject.Inject;

@RunWith(FeaturesRunner.class)
@Features( { CoreFeature.class })
@Deploy( { "org.nuxeo.ecm.platform.query.api",
        "org.nuxeo.ecm.core.storage.sql.ra", "org.nuxeo.ecm.automation.core",
        "org.nuxeo.ecm.automation.features", "org.nuxeo.ecm.archiving" })
@LocalDeploy( { "org.nuxeo.ecm.archiving:test-providers.xml",
        "org.nuxeo.ecm.archiving:test-repo-repository-h2-contrib.xml" })
public class ArchivingOperationsTest {

    @Inject
    AutomationService service;

    @Inject
    CoreSession session;

    @Inject
    PageProviderService pps;

    @Before
    public void initRepo() throws Exception {
        session.removeChildren(session.getRootDocument().getRef());
        session.save();
        DocumentModel fileDoc = null;

        for (int i = 0; i < 10; i++) {
            fileDoc = session.createDocumentModel("/", "f" + i, "Workspace");
            fileDoc.setPropertyValue("dc:title", "F" + i);
            fileDoc = session.createDocument(fileDoc);
        }
        session.save();
    }

    @Test
    public void testDeleteAllDocuments() throws Exception {
        // execute archiving chain to delete all documents
        OperationContext context = new OperationContext(session);
        context.put("providerName", "selectAllDocsProvider1");
        context.put("queryParams", "project");
        context.put("value", "delete");
        context.put("id", "updateLifeCycleChain");
        service.run(context, "archivingChain");
        session.save();
        // verify that all the docs are deleted

        String[] params = new String[1];
        params[0] = "deleted";
        PageProvider<DocumentModel> pp = getProvider(session,
                "selectAllDocsProvider1", new ArrayList<SortInfo>(), null,
                null, params);
        pp.getCurrentPage();
        assertEquals(10, pp.getResultsCount());

    }

    PageProvider<DocumentModel> getProvider(CoreSession session,
            String providerName, List<SortInfo> sortInfos, Long pageSize,
            Long currentPage, Object... parameters) throws ClientException {
        Map<String, Serializable> props = new HashMap<String, Serializable>();
        props.put(CoreQueryDocumentPageProvider.CORE_SESSION_PROPERTY,
                (Serializable) session);
        return (PageProvider<DocumentModel>) pps.getPageProvider(providerName,
                sortInfos, pageSize, currentPage, props, parameters);
    }

    @Test
    public void importAllDocsInNewRepository() throws Exception {
        // initRopo no2
        CoreSession session2 = openSessionAs("testRepo2",
                (NuxeoPrincipal) session.getPrincipal());

        // execute archiving chain to copy all the docs in this repo
        OperationContext context = new OperationContext(session);
        context.put("providerName", "selectAllDocsProvider1");
        context.put("queryParams", "project");
        context.put("targetRepositoryName", "testRepo2");
        context.put("id", "copyDocumentsInRepoChain");
        service.run(context, "archivingChain");
        session.save();

        // execute the same provider against to compare the no of docs found
        String[] params = new String[1];
        params[0] = "project";
        PageProvider<DocumentModel> pp = getProvider(session2,
                "selectAllDocsProvider1", new ArrayList<SortInfo>(), null,
                null, params);
        pp.getCurrentPage();
        assertEquals(10, pp.getResultsCount());

        session2.removeChildren(session2.getRootDocument().getRef());
        CoreInstance.getInstance().close(session2);
    }

    @Test
    public void importAllDocsInInNewLocationNewRepository() throws Exception {
        // initRopo no2
        CoreSession session2 = openSessionAs("testRepo2",
                (NuxeoPrincipal) session.getPrincipal());
        // create root rootDoc
        // create root rootDoc
        DocumentModel workspace = createDocument(session2, "/", "workspace",
                "Workspace");
        session2.save();

        // execute archiving chain to copy all the docs in this repo
        OperationContext context = new OperationContext(session);
        context.put("providerName", "selectAllDocsProvider1");
        context.put("queryParams", "project");
        context.put("targetRepositoryName", "testRepo2");
        context.put("parentPath", workspace.getPathAsString());
        context.put("id", "copyDocumentsInRepoChain");
        service.run(context, "archivingChain");
        session.save();

        // get all the children for the workspace in the new repo
        DocumentModelList importedDocs = session2.query(String.format(
                "Select * from Document where ecm:path STARTSWITH '%s'",
                NXQLQueryBuilder.prepareStringLiteral(
                        workspace.getPathAsString(), false, true)));
        assertEquals(10, importedDocs.size());

        session2.removeChildren(session2.getRootDocument().getRef());
        CoreInstance.getInstance().close(session2);
    }

    protected CoreSession openSessionAs(String repositoryName,
            NuxeoPrincipal principal) throws ClientException {
        Map<String, Serializable> context = new HashMap<String, Serializable>();
        context.put("principal", principal);
        return CoreInstance.getInstance().open(repositoryName, context);
    }

    protected DocumentModel createDocument(CoreSession session,
            String parentPath, String id, String typeName)
            throws ClientException {
        DocumentModel doc = session.createDocumentModel(parentPath, id,
                typeName);
        doc = session.createDocument(doc);
        return session.saveDocument(doc);
    }
}