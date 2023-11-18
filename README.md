# cedar-bridge-server
Microservice to access outside resources. Currently, it serves as the backend for requesting Digital Object Identifiers (DOIs) for templates and instances.
It accepts a CEDAR DataCite template, transforms it into a DataCite-conformant format, and then submits it to the DataCite REST APIs.

## Two essential API calls
### Request a new DOI. 
Generate a new DOI by providing the CEDAR DataCite instance.
Endpoint URL:  https://bridge.metadatacenter.orgx/datacite/create-doi
### Get metadata for an existing DOI
Retrieve metadata information as a CEDAR DataCite instance for an existing DOI.  
Endpoint URL: https://bridge.metadatacenter.orgx/datacite/get-doi-metadata/{id}

## Prerequisites
1. Build and install [cedar-parent](https://github.com/metadatacenter/cedar-parent)
2. Build and install [cedar-libraries](https://github.com/metadatacenter/cedar-libraries)

## Getting started
Clone the project:

    git clone https://github.com/metadatacenter/cedar-bridge-server.git

Navigate to the cedar-bridge-server directory:


    cd cedar-bridge-server

Then build it with Maven:

    mcit

## Questions

If you have questions about this repository, please subscribe to the [CEDAR Developer Support
mailing list](https://mailman.stanford.edu/mailman/listinfo/cedar-developers).
After subscribing, send messages to cedar-developers at lists.stanford.edu.
