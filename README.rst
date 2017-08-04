Maven Python Distribute Plugin
==============================

**version**: 0.2.0

This plugin integrates the Python **distribute** module into the Maven build:

http://packages.python.org/distribute/

This allows you to build and package Python code together with your Java code,
which is useful for IT shops that develop in both of these languages.
  
Functionality
-------------

* keeps the *setup.py* version in sync with the Maven project version by updating setup.py in the **process-sources** phase
* packages the Python module during the Maven **package** phase
* allows specifying which format should the Python module be distributed as: source, RPM, egg, tar, zip, etc.

Configuration
-------------

Add the following to your *pom.xml* build section:
::

	<plugin>
		<groupId>maven-python-mojos</groupId>
		<artifactId>maven-python-distribute-plugin</artifactId>
		<version>..</version>
		<executions>
			<execution>
				<id>package</id>
				<goals>
					<goal>package</goal>
				</goals>
			</execution>
			<execution>
				<id>process</id>
				<goals>
					<goal>process-sources</goal>
				</goals>
			</execution>
		</executions>
	</plugin>

setup.py
--------

To make the code runnable outside maven you can have a setup.py. If a setup-template.py is there in 
your source root setup.py will be replaced.

setup-template.py
--------

setup template allows for using maven controlled variables in your setup.py file.
Set the *version* field in your *setup-template.py* to a hardcoded constant of **${VERSION}**, e.g.
Set the *name* field in your *setup-template.py* to a hardcoded constant of **${PROJECT_NAME}**, e.g.
::
	from setuptools import setup, find_packages
	
	setup(
	      install_requires=['distribute'],
	      name = '${PROJECT_NAME}',
	      version = '${VERSION}',
	      packages = find_packages('.')
	)


Maven Repository
----------------

Add the following plugin repository to your *pom.xml* in order to use this plugin:

::

	<pluginRepositories>
		<pluginRepository>
			<id>jitpack.io</id>
			<url>https://jitpack.io</url>
		</pluginRepository>
	</pluginRepositories>





