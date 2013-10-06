# Multinodes mode


## Node configuration

* web.xml: 
 * define list of nodes

```
<servlet>
        <servlet-name>gn-servlet</servlet-name>
        <init-param>
             <param-name>nodes</param-name>
             <param-value>node1,node2</param-value>
        </init-param>
```

 * define servlet mapping
```

    <servlet-mapping>
        <servlet-name>gn-servlet</servlet-name>
        <url-pattern>/node1/srv/*</url-pattern>
    </servlet-mapping>
    <servlet-mapping>
        <servlet-name>gn-servlet</servlet-name>
        <url-pattern>/node2/srv/*</url-pattern>
    </servlet-mapping>
```

* register security config

```
	<bean id="filterChainProxy" class="org.springframework.security.web.FilterChainProxy">
		<constructor-arg>
			<list>
                ...
			    <!--<ref bean="coreFilterChain"/>-->
			    <ref bean="node1FilterChain"/>
			    <ref bean="node2FilterChain"/>
```

* WEB-INF-xyz to define a new node configuration
 * Create node security config

TODO


## Node access

URLs:
 * http://localhost:8080/geonetwork/apps/search/?node=node1 for widget app
 * http://localhost:8080/geonetwork/node1/srv/eng/<service_name> for Jeeves services


