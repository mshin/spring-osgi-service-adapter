# spring-osgi-service-adapter

Library to make it easier to use OSGi services with Spring on Fuse

		<dependency>
			<groupId>com.github.mshin</groupId>
			<artifactId>spring-osgi-service-adapter</artifactId>
			<version>1.0</version>
		</dependency>

see: https://github.com/mshin/fuse-example-projects/tree/master/osgi-service/osgi-service-root
for examples of how to use the library to expose and consume an OSGi service from a Spring application context.

see: https://github.com/mshin/spring-osgi-service-adapter for source code and the most up-to-date version of the library.

Expose a service:

	<bean id="osgiServiceRegistrator" class="com.github.mshin.sosa.OsgiServiceRegistrator"
		init-method="init" destroy-method="destroy">
		<property name="serviceInstance">
			<bean class="com.example.osgi.service.spring.MyNoInterfaceSpringOsgiService" />
		</property>
	</bean>
(If no properties, and serviceInstance will be retrieved using its same class). See Javadoc for more details.

or:

	<bean id="osgiServiceRegistrator" class="com.github.mshin.sosa.OsgiServiceRegistrator"
		init-method="init" destroy-method="destroy">
		<property name="bundleClass">
			<value type="java.lang.Class">com.example.osgi.service.spring.impl.ClassInOsgiServiceSpringBundle
			</value>
		</property>
		<property name="osgiServiceDefinitionList">
			<list>
				<bean class="com.github.mshin.sosa.OsgiServiceDefinition">
					<property name="classList">
						<list>
							<value>com.example.osgi.service.spring.MySpringOsgiService</value>
						</list>
					</property>
					<property name="serviceInstance">
						<bean class="com.example.osgi.service.spring.impl.MySpringOsgiServiceImpl" />
					</property>
					<property name="properties">
						<map>
							<entry key="property1" value="value1" />
							<entry key="property2" value="value2" />
						</map>
					</property>
				</bean>
			</list>
		</property>
	</bean>

Where bundleClass is a class in the bundle, the OSGi service is registered under classList index 0, serviceInstance is the exposed object, and properties are properties associated with the service. See Javadoc for more details.

Consume a service (example shows consuming 2 services):

	<bean id="cachingOsgiServiceGetter" class="com.github.mshin.sosa.CachingOsgiServiceGetter"
		init-method="init" destroy-method="destroy">
		<property name="bundleClass">
			<value type="java.lang.Class">com.example.osgi.service.use.spring.UseServices
			</value>
		</property>
		<property name="findOsgiServiceTimeout" value="10000" /><!-- default is 30000 (30 seconds) -->
	</bean>

	<bean id="mySpringOsgiService" class="com.github.mshin.sosa.OsgiService" factory-method="getService">
		<constructor-arg index="0"
			value="com.example.osgi.service.spring.MySpringOsgiService" />
		<constructor-arg index="1"
			value="(&amp;(objectclass=com.example.osgi.service.spring.MySpringOsgiService)(property1=value1))" />
		<constructor-arg index="2" ref="cachingOsgiServiceGetter" />
	</bean>

	<bean id="myNoInterfaceSpringOsgiService" class="com.github.mshin.sosa.OsgiService" factory-method="getService">
		<constructor-arg value="com.example.osgi.service.spring.MyNoInterfaceSpringOsgiService" />
		<constructor-arg ref="cachingOsgiServiceGetter" />
	</bean>

	<bean id="useServices" class="com.example.osgi.service.use.spring.UseServices">
		<property name="mySpringOsgiService" ref="mySpringOsgiService" />
		<property name="myNoInterfaceSpringOsgiService" ref="myNoInterfaceSpringOsgiService" />
	</bean>

Where bundleClass is a class in the bundle, userServices is a class in the bundle that needs the OSGi services injected, myNoInterfaceSpringOsgiService is an OSGi service that is found using it's class name, and mySpringOsgiService is a bundle found using an LDAP style filter. If the filter was not present, it would be found using its interface, MySpringOsgiService.

The init and destroy methods for CachingOsgiServiceGetter and OsgiServiceRegistrator are annotated, so the init-method and destroy-method fields may be omitted if using annotations and package com.github.mshin.sosa is scanned appropriately.



For questions, comments and requests you can contact MunChul Shin at mshin@redhat.com.



2016 October
