/*
 * Copyright (c) 2004-2008 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 */

package org.eclipsetrader.core.internal.trading;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipsetrader.core.ILauncher;
import org.eclipsetrader.core.trading.IBroker;
import org.eclipsetrader.core.trading.ITradingService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class TradingServiceLauncher implements ILauncher, IExecutableExtension {
	private String id;
	private String name;

	public TradingServiceLauncher() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IExecutableExtension#setInitializationData(org.eclipse.core.runtime.IConfigurationElement, java.lang.String, java.lang.Object)
	 */
	public void setInitializationData(IConfigurationElement config, String propertyName, Object data) throws CoreException {
		id = config.getAttribute("id");
		name = config.getAttribute("name");
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.ILauncher#getId()
	 */
	public String getId() {
		return id;
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.ILauncher#getName()
	 */
	public String getName() {
		return name;
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.ILauncher#launch(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void launch(IProgressMonitor monitor) {
		IBroker[] brokerConnector = getTradingService().getBrokers();
		for (int i = 0; i < brokerConnector.length; i++)
			brokerConnector[i].connect();
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.ILauncher#terminate(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void terminate(IProgressMonitor monitor) {
		IBroker[] brokerConnector = getTradingService().getBrokers();
		for (int i = 0; i < brokerConnector.length; i++)
			brokerConnector[i].disconnect();
	}

	protected ITradingService getTradingService() {
		try {
			BundleContext context = Activator.getDefault().getBundle().getBundleContext();
			ServiceReference serviceReference = context.getServiceReference(ITradingService.class.getName());
			ITradingService service = (ITradingService) context.getService(serviceReference);
			context.ungetService(serviceReference);
			return service;
		} catch (Exception e) {
			Status status = new Status(Status.ERROR, Activator.PLUGIN_ID, 0, "Error reading trading service", e);
			Activator.getDefault().getLog().log(status);
		}
		return null;
	}
}
