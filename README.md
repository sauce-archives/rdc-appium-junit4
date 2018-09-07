# RDC Appium Support for JUnit 4

[![Build Status](https://travis-ci.org/saucelabs/rdc-appium-junit4.svg?branch=master)](https://travis-ci.org/saucelabs/rdc-appium-junit4)

RDC Appium Support for JUnit 4 is an open source library for running tests against Sauce Labs **R**eal **D**evice **C**loud. This includes:

* Running Appium test suites.
* Updating test status on RDC (automatically set failure or success icons on your test reports list on RDC website).
* Use some RDC custom capabilities as constants.

**Note** that this library is not needed for running tests against Sauce Labs Real Device Cloud. It is only for the above mentioned features.

RDC Appium Support for JUnit 4 is published under the
[Apache Software License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0).
It requires at least Java 8.

## Installation

RDC Appium Support for JUnit 4 is available on
[Maven Central](https://search.maven.org/search?q=g:com.saucelabs%20AND%20a:rdc-appium-junit4).

```xml
<dependency>
  <groupId>com.saucelabs</groupId>
  <artifactId>rdc-appium-junit4</artifactId>
  <version>1.0.0</version>
  <scope>test</scope>
</dependency>
```


## Usage

#### How to update a test status on RDC?
If you want to see *SUCCESS* or *FAILURE* instead of *UNKNOWN* in your test reports on RDC, you need to use the `RdcTestResultWatcher` like the following:
```java
public class RdcTestResultWatcherTest {

	@Rule
	public final RdcTestResultWatcher watcher = new RdcTestResultWatcher();
	private AppiumDriver driver;

	@Before
	public void setup() {
		DesiredCapabilities capabilities = new DesiredCapabilities();
		capabilities.setCapability(RdcCapabilities.API_KEY, "Your project API key");

		// Initializing Appium driver and setting the watcher
		driver = new AndroidDriver(RdcDataCenter.EU, capabilities);
		watcher.setRemoteWebDriver(driver);
	}

	@Test
	public void aTestThatUpdatesHisTestResultAfterwards() {
		// driver.testAllTheThings();
	}

	// No need to close the driver. The library does that automatically.
}
```

#### How to run an Appium test suite?
First, Create a test suite in your project on RDC.

Then, get the project **API key** and **suite Id** from your RDC project setup and put them in the annotation like the following example:

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

## Release Guide

Travis CI deploys a release to Maven Central for each commit to master. Usually
these are snapshot releases. If you want to create a new full release then
please execute the following steps.

* Select a new version according to the
  [Semantic Versioning 2.0.0 Standard](http://semver.org/).
* Set the new version in `pom.xml` and in the `Installation` section of
  this readme.
* Commit the modified `pom.xml` and `README.md`.
* Push/merge to master so that Travis CI deploys the release.
* Add a tag for the release: `git tag rdc-appium-junit4-X.X.X`

#### Generate GPG keys

This step is not needed to be done anymore since we already have keys.

**`gpg --version`** is `gpg (GnuPG) 2.2.9` at the time this documentation was created.

Artifacts that are uploaded to Maven Central must be signed. Therefore we need
GPG keys. 

##### Generate GPG keys

    gpg --generate-key
    
##### List keys
	
	gpg -k

    
##### List secret keys
	
	gpg -K



##### Export public and private keys

Now, we can get the the *id* of the key we want to use and use it to export our keys e.g. `8C9E96A025C6B3218A4D79B86995350981214615`: 

    gpg --export --armor 8C9E96A025C6B3218A4D79B86995350981214615 > deployment/signingkey.asc
    gpg --export-secret-keys --armor 8C9E96A025C6B3218A4D79B86995350981214615 >> deployment/signingkey.asc


#### Store Secrets

Travis needs to know the passphrase which has been used when the gpg key is generated, and Sonatype credentials for deploying to Maven Central.
These are secrets and therefore we can't push them to Github. Therefore Travis provides [support for encrypting
secrets.](https://docs.travis-ci.com/user/encryption-keys/) The ciphertexts can
be stored in the repository. To do that we need first to login to Travis (with github credentials)

    travis login

Then, encrypt our secrets and add them to you **.travis.yml** file as encrypted environment variables

    travis encrypt PASSPHRASE=... --add
    travis encrypt SONATYPE_USERNAME=... --add
    travis encrypt SONATYPE_PASSWORD=... --add

After that, we need to encrypt our exported gpg keys file to be able to push it Github. Later, travis will be able to decrypt the file securely again. 

    travis encrypt-file deployment/signingkey.asc deployment/signingkey.asc.enc --add

Delete the unencrypted file afterwards and **never push it to Github**

    rm deployment/signingkey.asc


#### Note:

The flag `--pro` should be added after each travis command if the project is activated on
**travis-ci.com** and not on **travis-ci.org** (all new projects are built on travis-ci.com). 

Examples:
* `travis login --pro`
* `travis encrypt PASSPHRASE=... --pro --add`
* `travis encrypt-file deployment/signingkey.asc deployment/signingkey.asc.enc --pro --add`
