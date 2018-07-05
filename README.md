# RDC Appium Junit4 [![Build Status](https://travis-ci.org/saucelabs/rdc-appium-junit4.svg?branch=master)](https://travis-ci.org/saucelabs/rdc-appium-junit4)

Sauce Labs **R**eal **D**evice **C**loud Appium Junit4 Client Library for:

* Running Appium test suites.
* Updating test status on RDC.

## How to run an Appium test suite?
1. Create a test suite in your project on RDC.
2. Get the project API key and suite Id and put them in the annotation like the following example:

```java
@RunWith(RdcAppiumSuite.class)
@Rdc(apiKey = "Your project API key goes here", suiteId = 123)
public class RdcAppiumSuiteWatcherTest {

	@Rule
	public RdcAppiumSuiteWatcher watcher = new RdcAppiumSuiteWatcher();
	private AppiumDriver driver;

	@Before
	public void setup() {
		// Add these capabilities
		DesiredCapabilities capabilities = new DesiredCapabilities();
		capabilities.setCapability(RdcCapabilities.API_KEY, watcher.getApiKey());
		capabilities.setCapability(RdcCapabilities.TEST_REPORT_ID, watcher.getTestReportId());

		// Initializing Appium driver and setting the watcher
		driver = new AndroidDriver(watcher.getAppiumEndpointUrl(), capabilities);
		watcher.setRemoteWebDriver(driver);
	}

	@Test
	public void testIt() {
		// driver.testAllTheThings();
	}

	// No need to close the driver. The library does that automatically.
}
```

## How to update test status on RDC?
I you want to see *SUCCESS* or *FAILURE* instead of *UNKNOWN* in your test reports on RDC website, you need to use the `RdcTestResultWatcher` like the following:
```java
public class RdcTestResultWatcherTest {

	@Rule
	public RdcTestResultWatcher watcher = new RdcTestResultWatcher();
	private AppiumDriver driver;

	@Before
	public void setup() {
		DesiredCapabilities capabilities = new DesiredCapabilities();
		capabilities.setCapability(RdcCapabilities.API_KEY, "Your project API key");

		// Initializing Appium driver and setting the watcher
		driver = new AndroidDriver(RdcEndpoints.EU_ENDPOINT, capabilities);
		watcher.setRemoteWebDriver(driver);
	}

	@Test
	public void aTestThatUpdatesHisTestResultAfterwards() {
		// driver.testAllTheThings();
	}

	// No need to close the driver. The library does that automatically.
}
```

## Release Guide

#### General information about working with GPG files

You need a gpg key to sign the library in order to deploy it.

First you need to make sure you have `gpg` installed. _GPG behaviours may differ between different versions_.
* `cd to/your/work/directory`.
* Export / get the public key and put it in a file called `public.gpg`
* Export / get the secret key and put it in a file called `secret.gpg`
* Import / make a secret keyring `gpg --no-default-keyring --allow-secret-key-import --keyring=./secring.gpg --import secret.gpg`
* Import / make a public keyring `gpg --no-default-keyring --keyring=./pubring.gpg --import public.gpg`
* Encrypting a private / public keyring file `openssl aes-256-cbc -pass pass:yourPasswordGoesHere -in ./any_keyring.gpg -out ./any_keyring.gpg.enc -e`
* Decrypting a private / public keyring `openssl aes-256-cbc -pass pass:yourPasswordGoesHere -in ./any_keyring.gpg.enc -out ./any_keyring.gpg -d`
* You should only push encrypted files to GitHub.


Afterwards you need to use the decrypted files to sign the library in order to deploy it to Maven central.

### Deploying to Sonatype

Here is the official [documentation](https://central.sonatype.org/pages/apache-maven.html) which shows how to use [nexus-staging-maven-plugin](https://mvnrepository.com/artifact/org.sonatype.plugins/nexus-staging-maven-plugin).

Make sure you increased the library version in the pom file, made a PR and merged it. Once your code is merged you can deploy from your command line:

1. `cd /your/lbrary/location`

2. `brew install gnupg@2.2` and make sure you are really using it `gpg --version`

3. Set Environment variables:
	* `export NEXUS_USERNAME=<sonatype-jira-username-from-password-manager>`
	* `export NEXUS_PASSWORD=<sonatype-jira-password-from-password-manager>`
	* `export GPG_PASSPHRASE=<gpg-passphrase-from-password-manager>`
	* `export ENCRYPTION_PASSWORD=$GPG_PASSPHRASE`
	
4. Decrypt keyrings:
	* `openssl aes-256-cbc -pass pass:$ENCRYPTION_PASSWORD -in ./pubring.gpg.enc -out ./pubring.gpg -d`
	* `openssl aes-256-cbc -pass pass:$ENCRYPTION_PASSWORD -in ./secring.gpg.enc -out ./secring.gpg -d`

5. Deploy `mvn clean deploy --settings settings.xml -Dgpg.keyname=6995350981214615 -Dgpg.passphrase=$GPG_PASSPHRASE -Dgpg.secretKeyring=./secring.gpg -Dgpg.publicKeyring=./pubring.gpg -Dgpg.defaultKeyring=false`
 
