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

To use this integration the package must be built and installed (see Building above). Before building ensure that your API Key is set in `QuickCheck.java` (in the bundle project):

	private final String API_KEY = "your_api_key_here"; // replace with ActiveStandards API Key

Once installed the page component needs to be set as the `sling:resourceSuperType` for any existing page components you wish to be able to validate with ActiveStandards.
An `ActiveStandards Quick Check` button is added to the Sidekick in the Page tab. Clicking this button will open the Quick Check results page in a new browser window (or tab)

Outstanding tasks
-----------------

While the integration is usable in its current state there are still a number of outstanding functional elements:

- Add toggle link to switch between source and in-context view where failed checkpoint supports highlighting for both
- Update `QuickCheck.java` to read API key from Cloud Service config
- Update `QuickCheck.java` to read website ID from Cloud Service framework
- Improve the styling of the results presentation page
- Add the display of the failed checkpoint description to the results presentation page
- Get the normal rendered page source to display when highlighting is not supports (currently the HTML is set but it doesn't display)
- Implement QuickCheck.java as a Sling service rather than a POJO