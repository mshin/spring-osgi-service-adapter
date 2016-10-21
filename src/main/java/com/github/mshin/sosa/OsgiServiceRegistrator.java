package com.github.mshin.sosa;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class responsible for registering and unregistering class instances as OSGi
 * services.
 * 
 * @author MunChul Shin
 */
public class OsgiServiceRegistrator {

	private Class<?> bundleClass;
	private BundleContext context;
	private List<ServiceRegistration<?>> registrationList;

	private List<OsgiServiceDefinition> osgiServiceDefinitionList;
	private Object serviceInstance;

	private static final Logger LOGGER = LoggerFactory.getLogger(OsgiServiceRegistrator.class);

	/**
	 * Initialization method to be called from the Spring context. Accesses the
	 * Bundle containing <b>bunleClass</b>. Registers as OSGi services
	 * <code>Object</code> instances defined in <b>serviceInstance</b> and
	 * <b>osgiServiceDefinitionList</b>.
	 */
	@PostConstruct
	public void init() {
		LOGGER.info("initializing {}...", this.getClass().getName());

		Bundle bundle = null;
		Class<?> classUsedToRetrieveBundle = bundleClass;

		if (null == classUsedToRetrieveBundle) {
			if (null != serviceInstance) {
				LOGGER.info("bundleClass is null. Using serviceInstance to retrieve bundle.");
				classUsedToRetrieveBundle = serviceInstance.getClass();
			} else if (null != osgiServiceDefinitionList && osgiServiceDefinitionList.size() > 0
					&& null != osgiServiceDefinitionList.get(0).getServiceInstance()) {
				LOGGER.info(
						"bundleClass is null. Using osgiServiceDefinitionList.0.serviceInstance to retrieve bundle.");
				classUsedToRetrieveBundle = osgiServiceDefinitionList.get(0).getServiceInstance().getClass();
			} else {
				LOGGER.info("bundleClass is null. Using OsgiServiceRegistrator.class to retrieve bundle.");
				classUsedToRetrieveBundle = OsgiServiceRegistrator.class;
			}
		}

		LOGGER.info("Class used to find bundle: {}", String.valueOf(classUsedToRetrieveBundle));

		bundle = FrameworkUtil.getBundle(classUsedToRetrieveBundle);

		LOGGER.info("bundle: {}", String.valueOf(bundle));

		this.context = bundle.getBundleContext();
		this.registrationList = new ArrayList<ServiceRegistration<?>>();

		LOGGER.info("context: {}", String.valueOf(this.context));

		if (null != this.serviceInstance) {

			LOGGER.info("Registering serviceInstance: {}...", this.serviceInstance);

			registerService(this.serviceInstance.getClass(), this.serviceInstance, new Hashtable<String, String>());

			LOGGER.info("Registered serviceInstance {}.", this.serviceInstance);
		}
		if (null != this.osgiServiceDefinitionList) {
			for (OsgiServiceDefinition osd : this.osgiServiceDefinitionList) {

				LOGGER.info("Registering osgiServiceDefinition service: {}...", osd.getServiceInstance());

				registerService(osd.getClassList(), osd.getServiceInstance(), osd.getProperties());

				LOGGER.info("Registered service: {} under classes: {} and properties: {}.", osd.getServiceInstance(),
						osd.getClassList(), osd.getProperties());
			}
		}

		LOGGER.info("Finished initializing {}.", this.getClass().getName());
	}

	/**
	 * Registers <b>service</b> as an OSGi service under the name <b>clazz</b>,
	 * with the specified <b>dictionary</b>.
	 */
	public void registerService(Class<?> clazz, Object service, Dictionary<String, String> dictionary) {

		this.registrationList.add(this.context.registerService(clazz.getName(), service, dictionary));
	}

	/**
	 * Registers <b>service</b> as an OSGi service under the class names
	 * <b>list</b>, with the specified <b>dictionary</b>.
	 */
	public void registerService(List<String> list, Object service, Dictionary<String, String> dictionary) {

		this.registrationList.add(this.context.registerService(list.toArray(new String[0]), service, dictionary));
	}

	/**
	 * Shutdown method to be called from Spring context. Will unregister
	 * services.
	 */
	@PreDestroy
	public void destroy() {

		LOGGER.info("destroying {}...", this.getClass().getName());

		for (ServiceRegistration<?> registration : this.registrationList) {
			registration.unregister();
		}
		this.registrationList.clear();

		LOGGER.info("Finished destroying {}.", this.getClass().getName());
	}

	/**
	 * This is provided as a convenience method. This property however should be
	 * accessed with caution as it is advised that OSGi code and your
	 * application code should not be mixed.
	 * 
	 * @return
	 */
	public BundleContext getBundleContext() {
		return context;
	}

	/**
	 * Set this property to control the bundleContext used. The bundle that
	 * declares this class will expose the OSGi services. If a non-bunle class
	 * is used here, a <code>NullPointerException</code> is thrown. If a
	 * <code>org.osgi.framework.BundleActivator</code> is declared in the bundle
	 * you wish to expose the OSGi services, it is advised that it be used for
	 * this property.
	 * <p>
	 * If this property is not set,
	 * <code>OsgiServiceRegistrator</code>.<b>serviceInstance</b> will be used.
	 * If that property is not set, the first
	 * <code>OsgiServiceDefinition</code>'s <b>serviceInstance</b> in
	 * <b>osgiServiceDefinitionList</b> will be used. If that property is null,
	 * <code>OsgiServiceRegistrator.class</code> will be used. Note that
	 * <code>OsgiServiceRegistrator.class</code> will result in <b>this</b>
	 * bundle (<code>spring-osgi-service-adapter</code>) exposing the service.
	 * 
	 * @param bundleClass
	 */
	public void setBundleClass(Class<?> bundleClass) {
		this.bundleClass = bundleClass;
	}

	/**
	 * Set this property to register the defined OSGi services under the given
	 * list of class names and the given properties.
	 * 
	 * @param osgiServiceDefinitionList
	 */
	public void setOsgiServiceDefinitionList(List<OsgiServiceDefinition> osgiServiceDefinitionList) {
		this.osgiServiceDefinitionList = osgiServiceDefinitionList;
	}

	/**
	 * Set this property to register the <b>serviceInstance</b> as an OSGi
	 * service under it's class name with no associated properties.
	 * 
	 * @param serviceInstance
	 */
	public void setServiceInstance(Object serviceInstance) {
		this.serviceInstance = serviceInstance;
	}

}
