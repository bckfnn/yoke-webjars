yoke-webjars
============

webjars.org middleware for yoke (https://github.com/pmlopes/yoke) framework

The **yoke-webjars** middleware will server static content from [webjars](www.webjars.org). 
The webjars must be added to the classpath in the usual way.

Download
--------

**yoke-webjars** is at the moment available in bintray's jcenter:

    <repositories>
        <repository>
            <id>jcenter</id>
            <url>http://jcenter.bintray.com/</url>
        </repository>
    </repositories>
    
    <dependencies>   
        <dependency>
            <groupId>io.github.bckfnn</groupId>
            <artifactId>yoke-webjars</artifactId>
            <version>1.0.0</version>
        </dependency>
    </dependencies>

Usage
-----

Add the webjars to the build system, f.ex jquery:

    <dependency>
        <groupId>org.webjars</groupId>
        <artifactId>jquery</artifactId>
        <version>2.1.1</version>
    </dependency>
         
Add the WebJars as a middleware, with the path where the resources will be served.

    yoke.use(new WebJars("/assets/webjars"));
 
Access the resources from the web jars in the html page:

    <script src="/assets/webjars/jquery/2.1.1/jquery.min.js" type="text/javascript"></script>
    
    