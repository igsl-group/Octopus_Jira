# Config Migration Plugin

## Environment Setup
1. You must manually install Insight artifacts.
1. Download all .jar and .pom files from Atlassian repository for the following modules: 
	1. Insight-core-model: https://packages.atlassian.com/repository/public/com/riadalabs/insight-core-model/
	1. Insight: https://packages.atlassian.com/repository/public/com/riadalabs/jira/plugins/insight/
1. Run Maven to import them to local repository.
1. atlas-mvn org.apache.maven.plugins:maven-install-plugin:2.5.2:install-file -DpomFile="..." -Dfile="..."
1. You will find them installed under %USERPROFILE%\\.m2\\repository folder.
1. Add these to pom.xml: 
>		<!-- Insight -->
>		<dependency>
>			<groupId>com.riadalabs.jira.plugins</groupId>
>			<artifactId>insight</artifactId>
>			<version>9.0.7</version>
>			<scope>provided</scope>
>		</dependency>
>		<dependency>
>			<groupId>com.riadalabs</groupId>
>			<artifactId>insight-core-model</artifactId>
>			<version>0.3.9</version>
>			<scope>provided</scope>
>		</dependency>

1. Rebuild. 
