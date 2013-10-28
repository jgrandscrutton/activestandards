ActiveStandards QuickCheck
========

This a content package project generated using the multimodule-content-package-archetype.

Building
--------

This project uses Maven for building. Common commands:

From the root directory, run ``mvn -PautoInstallPackage clean install`` to build the bundle and content package and install to a CQ instance.

From the bundle directory, run ``mvn -PautoInstallBundle clean install`` to build *just* the bundle and install to a CQ instance.

Using with VLT
--------------

To use vlt with this project, first build and install the package to your local CQ instance as described above. Then cd to `content/src/main/content/jcr_root` and run

    vlt --credentials admin:admin checkout -f ../META-INF/vault/filter.xml --force http://localhost:4502/crx

Once the working copy is created, you can use the normal ``vlt up`` and ``vlt ci`` commands.

Specifying CRX Host/Port
------------------------

The CRX host and port can be specified on the command line with:
`mvn -Dcrx.host=otherhost -Dcrx.port=5502 <goals>`

Using the integration
---------------------

To use this integration the package must be built and installed (see Building above).

The integration is enabled by setting the `sling:resourceSuperType` for the page component of any template where validation is required. For example, in the Geometrixx Outdoors
site the `page` component is a supertype for all other page components, and the default supertype for this component is `foundation/components/page`. To enable the integration for
all Geometrixx Outdoors templates simply change this to `activestandards/components/page`.

The API key used to authenticate requests to the ActiveStandards API is set as a Cloud Service configuration. If you don't already have an API Key you will need to request one from
ActiveStandards. If an ActiveStandards Service node doesn't exist under Cloud Services Configurations then one will need to be created. To do this create a page in the root of Cloud
Services Configurations using the Service template. Any meaningful value can be entered as the Title, but the Name must be `activestandards`. Create a new page under this node using
the 'ActiveStandards Cloud Service Config' template. Open the page and set the API Key value. If required it's also possible to define the host name and port of a proxy server to
use for requests to ActiveStandards. Under the configuration node create a new page using the 'ActiveStandards QuickCheck Framework' template. Open the page and set the Website Id
(this value is also provided by ActiveStandards). Finally, associate the Framework with the root page of your website. Different Frameworks can be associated with different sites,
or can be used to override the default Website Id by associating it with a page somewhere below the root of a site.

An `ActiveStandards Quick Check` button is added to the Sidekick in the Page tab. Clicking this button will open the Quick Check results page in a new browser window (or tab)

Outstanding tasks
-----------------

While the integration is usable in its current state there are still a number of outstanding functional elements:

- Improve the styling of the results presentation page
- Add the display of the failed checkpoint description to the results presentation page