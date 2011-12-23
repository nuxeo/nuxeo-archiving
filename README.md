# Content Automation Operations for archiving batch of documents in a repository 

This addon provides some perdefined automation operations for archiving batch of documents in a repository.
The  "archivingChain" chain must be configured with a  pageProvider or query to select the documents to be processed and with an automation chain that contains the archiving logic. 
archivingChain : takes the following  parameters : 
- string "query" : the query to be executed to select the documents 
or - string "providerName" : the provider to be executed
- string : "language"
- stringList "sortInfo"
- stringList "queryParams"

-string "id" -> the automation chain containing the archinving logic to be executed

There are already two preconfigured chains for the archiving logic : 
updateLifeCycleChain : takes the following  parameters : 
 - string "value" -> the transition to follow
copyDocumentsInRepoChain :  takes the following  parameters : 
 - string "targetRepositoryName" -> the repository where the input documents are copied ( assuming the Nuxeo instance has alreday a configured conection to this repository)
-  string "parentPath" 
 
## How to build

You can build it with:

	$ mvn clean install

##How to test
Invoke the archivingChain using NuxeoShell:

run archivingChain -ctx providerName=selectAllWorkspace,pageSize=2,id=updateLifeCycleChain,value=delete
  
## About Nuxeo

Nuxeo provides a modular, extensible Java-based [open source software platform for enterprise content management](http://www.nuxeo.com/en/products/ep) and packaged applications for [document management](http://www.nuxeo.com/en/products/document-management), [digital asset management](http://www.nuxeo.com/en/products/dam) and [case management](http://www.nuxeo.com/en/products/case-management). Designed by developers for developers, the Nuxeo platform offers a modern architecture, a powerful plug-in model and extensive packaging capabilities for building content applications.

More information on: <http://www.nuxeo.com/>
