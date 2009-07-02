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

package org.eclipsetrader.internal.brokers.paper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Status;
import org.eclipsetrader.core.feed.IFeedIdentifier;
import org.eclipsetrader.core.feed.IPricingListener;
import org.eclipsetrader.core.feed.IQuote;
import org.eclipsetrader.core.feed.ITrade;
import org.eclipsetrader.core.feed.PricingDelta;
import org.eclipsetrader.core.feed.PricingEvent;
import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.core.markets.IMarketService;
import org.eclipsetrader.core.markets.MarketPricingEnvironment;
import org.eclipsetrader.core.repositories.IRepositoryService;
import org.eclipsetrader.core.trading.BrokerException;
import org.eclipsetrader.core.trading.IAccount;
import org.eclipsetrader.core.trading.IBroker;
import org.eclipsetrader.core.trading.IOrder;
import org.eclipsetrader.core.trading.IOrderChangeListener;
import org.eclipsetrader.core.trading.IOrderMonitor;
import org.eclipsetrader.core.trading.IOrderRoute;
import org.eclipsetrader.core.trading.IOrderSide;
import org.eclipsetrader.core.trading.IOrderStatus;
import org.eclipsetrader.core.trading.IOrderType;
import org.eclipsetrader.core.trading.IOrderValidity;
import org.eclipsetrader.core.trading.OrderChangeEvent;
import org.eclipsetrader.core.trading.OrderDelta;
import org.eclipsetrader.internal.brokers.paper.transactions.StockTransaction;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class PaperBroker implements IBroker, IExecutableExtension {
	private String id;
	private String name;
	private MarketPricingEnvironment pricingEnvironment;

	private List<OrderMonitor> pendingOrders = new ArrayList<OrderMonitor>();
	private ListenerList listeners = new ListenerList(ListenerList.IDENTITY);

	private IPricingListener pricingListener = new IPricingListener() {
		public void pricingUpdate(PricingEvent event) {
			for (PricingDelta delta : event.getDelta()) {
				if (delta.getNewValue() instanceof ITrade)
					processTrade(event.getSecurity(), (ITrade) delta.getNewValue());
			}
		}
	};

	public PaperBroker() {
	}

	public PaperBroker(MarketPricingEnvironment pricingEnvironment) {
		this.pricingEnvironment = pricingEnvironment;
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.trading.IBrokerConnector#getId()
	 */
	public String getId() {
		return id;
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.trading.IBrokerConnector#getName()
	 */
	public String getName() {
		return name;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IExecutableExtension#setInitializationData(org.eclipse.core.runtime.IConfigurationElement, java.lang.String, java.lang.Object)
	 */
	public void setInitializationData(IConfigurationElement config, String propertyName, Object data) throws CoreException {
		id = config.getAttribute("id");
		name = config.getAttribute("name");
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.trading.IBrokerConnector#connect()
	 */
	public void connect() {
		if (pricingEnvironment == null) {
			BundleContext context = Activator.getDefault().getBundle().getBundleContext();
			ServiceReference serviceReference = context.getServiceReference(IMarketService.class.getName());
			if (serviceReference != null) {
				IMarketService marketService = (IMarketService) context.getService(serviceReference);
				pricingEnvironment = new MarketPricingEnvironment(marketService);
				context.ungetService(serviceReference);
			}
		}
		if (pricingEnvironment != null)
			pricingEnvironment.addPricingListener(pricingListener);
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.trading.IBrokerConnector#disconnect()
	 */
	public void disconnect() {
		if (pricingEnvironment != null)
			pricingEnvironment.removePricingListener(pricingListener);
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.trading.IBrokerConnector#canTrade(org.eclipsetrader.core.instruments.ISecurity)
	 */
	public boolean canTrade(ISecurity security) {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.trading.IBrokerConnector#getSecurityFromSymbol(java.lang.String)
	 */
	public ISecurity getSecurityFromSymbol(String symbol) {
		ISecurity security = null;

		if (Activator.getDefault() != null) {
			BundleContext context = Activator.getDefault().getBundle().getBundleContext();
			ServiceReference serviceReference = context.getServiceReference(IRepositoryService.class.getName());
			if (serviceReference != null) {
				IRepositoryService service = (IRepositoryService) context.getService(serviceReference);

				ISecurity[] securities = service.getSecurities();
				for (int i = 0; i < securities.length; i++) {
					IFeedIdentifier identifier = securities[i].getIdentifier();
					if (identifier != null && symbol.equals(identifier.getSymbol())) {
						security = securities[i];
						break;
					}
				}

				context.ungetService(serviceReference);
			}
		}

		return security;
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.trading.IBroker#getSymbolFromSecurity(org.eclipsetrader.core.instruments.ISecurity)
	 */
	public String getSymbolFromSecurity(ISecurity security) {
		return security.getIdentifier() != null ? security.getIdentifier().getSymbol() : null;
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.trading.IBrokerConnector#getAllowedSides()
	 */
	public IOrderSide[] getAllowedSides() {
		return new IOrderSide[] {
		    IOrderSide.Buy, IOrderSide.Sell,
		};
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.trading.IBrokerConnector#getAllowedTypes()
	 */
	public IOrderType[] getAllowedTypes() {
		return new IOrderType[] {
		    IOrderType.Limit, IOrderType.Market,
		};
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.trading.IBrokerConnector#getAllowedValidity()
	 */
	public IOrderValidity[] getAllowedValidity() {
		return new IOrderValidity[] {
			IOrderValidity.Day,
		};
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.trading.IBrokerConnector#getAllowedRoutes()
	 */
	public IOrderRoute[] getAllowedRoutes() {
		return new IOrderRoute[0];
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.trading.IBrokerConnector#getOrders()
	 */
	public IOrderMonitor[] getOrders() {
		synchronized (pendingOrders) {
			return pendingOrders.toArray(new IOrderMonitor[pendingOrders.size()]);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.trading.IBrokerConnector#prepareOrder(org.eclipsetrader.core.trading.IOrder)
	 */
	public IOrderMonitor prepareOrder(IOrder order) throws BrokerException {
		if (order.getAccount() != null && !(order.getAccount() instanceof Account))
			throw new BrokerException("Invalid account");

		OrderMonitor monitor = new OrderMonitor(this, order) {
			@Override
			public void cancel() throws BrokerException {
				synchronized (pendingOrders) {
					pendingOrders.remove(this);
				}
				setStatus(IOrderStatus.Canceled);
				fireUpdateNotifications(new OrderDelta[] {
					new OrderDelta(OrderDelta.KIND_UPDATED, this),
				});
			}

			@Override
			public void submit() throws BrokerException {
				synchronized (pendingOrders) {
					pendingOrders.add(this);
				}

				SimpleDateFormat idFormatter = new SimpleDateFormat("yyMMddHHmmssSSS");
				setId(idFormatter.format(new Date()));
				setStatus(IOrderStatus.PendingNew);

				fireUpdateNotifications(new OrderDelta[] {
					new OrderDelta(OrderDelta.KIND_UPDATED, this),
				});

				if (getOrder().getType() == IOrderType.Market) {
					IQuote quote = pricingEnvironment.getQuote(getOrder().getSecurity());
					if (quote != null)
						processMarketOrder(this, quote);
				}
			}
		};

		if (pricingEnvironment != null)
			pricingEnvironment.addSecurity(order.getSecurity());

		fireUpdateNotifications(new OrderDelta[] {
			new OrderDelta(OrderDelta.KIND_ADDED, monitor),
		});

		return monitor;
	}

	protected void processMarketOrder(OrderMonitor monitor, IQuote quote) {
		List<OrderDelta> deltas = new ArrayList<OrderDelta>();

		IOrder order = monitor.getOrder();
		if (order.getSide() == IOrderSide.Buy) {
			if (quote.getAsk() != null) {
				fillOrder(monitor, monitor.getOrder(), null, quote.getAsk());
				deltas.add(new OrderDelta(OrderDelta.KIND_UPDATED, monitor));
			}
		}
		else if (order.getSide() == IOrderSide.Sell) {
			if (quote.getBid() != null) {
				fillOrder(monitor, monitor.getOrder(), null, quote.getBid());
				deltas.add(new OrderDelta(OrderDelta.KIND_UPDATED, monitor));
			}
		}

		if (deltas.size() != 0)
			fireUpdateNotifications(deltas.toArray(new OrderDelta[deltas.size()]));
	}

	protected void processTrade(ISecurity security, ITrade trade) {
		List<OrderDelta> deltas = new ArrayList<OrderDelta>();

		OrderMonitor[] monitors;
		synchronized (pendingOrders) {
			monitors = pendingOrders.toArray(new OrderMonitor[pendingOrders.size()]);
		}
		for (int i = 0; i < monitors.length; i++) {
			IOrder order = monitors[i].getOrder();
			if (order.getSecurity() == security) {
				if (order.getType() == IOrderType.Market) {
					fillOrder(monitors[i], monitors[i].getOrder(), trade.getSize(), trade.getPrice());
					deltas.add(new OrderDelta(OrderDelta.KIND_UPDATED, monitors[i]));
				}
				else if (order.getType() == IOrderType.Limit) {
					if (order.getSide() == IOrderSide.Buy && trade.getPrice() <= order.getPrice()) {
						fillOrder(monitors[i], monitors[i].getOrder(), trade.getSize(), trade.getPrice());
						deltas.add(new OrderDelta(OrderDelta.KIND_UPDATED, monitors[i]));
					}
					else if (order.getSide() == IOrderSide.Sell && trade.getPrice() >= order.getPrice()) {
						fillOrder(monitors[i], monitors[i].getOrder(), trade.getSize(), trade.getPrice());
						deltas.add(new OrderDelta(OrderDelta.KIND_UPDATED, monitors[i]));
					}
				}
			}
		}

		if (deltas.size() != 0)
			fireUpdateNotifications(deltas.toArray(new OrderDelta[deltas.size()]));
	}

	protected void fillOrder(OrderMonitor monitor, IOrder order, Long size, Double price) {
		double totalPrice = monitor.getFilledQuantity() != null ? monitor.getFilledQuantity() * monitor.getAveragePrice() : 0.0;
		long filledQuantity = monitor.getFilledQuantity() != null ? monitor.getFilledQuantity() : 0L;
		long remainQuantity = order.getQuantity() - filledQuantity;

		long quantity = size != null && size < remainQuantity ? size : remainQuantity;
		filledQuantity += quantity;
		totalPrice += quantity * price;

		monitor.setFilledQuantity(filledQuantity);
		monitor.setAveragePrice(totalPrice / filledQuantity);

		if (quantity != 0) {
			if (order.getSide() == IOrderSide.Buy || order.getSide() == IOrderSide.BuyCover)
				monitor.addTransaction(new StockTransaction(monitor.getOrder().getSecurity(), quantity, price));
			if (order.getSide() == IOrderSide.Sell || order.getSide() == IOrderSide.SellShort)
				monitor.addTransaction(new StockTransaction(monitor.getOrder().getSecurity(), -quantity, price));
		}

		if (monitor.getFilledQuantity().equals(order.getQuantity())) {
			monitor.setStatus(IOrderStatus.Filled);
			monitor.fireOrderCompletedEvent();
			synchronized (pendingOrders) {
				pendingOrders.remove(monitor);
			}

			Account account = (Account) monitor.getOrder().getAccount();
			if (account != null)
				account.processCompletedOrder(monitor);
		}
		else
			monitor.setStatus(IOrderStatus.Partial);
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.trading.IBroker#addOrderChangeListener(org.eclipsetrader.core.trading.IOrderChangeListener)
	 */
	public void addOrderChangeListener(IOrderChangeListener listener) {
		listeners.add(listener);
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.trading.IBroker#removeOrderChangeListener(org.eclipsetrader.core.trading.IOrderChangeListener)
	 */
	public void removeOrderChangeListener(IOrderChangeListener listener) {
		listeners.remove(listener);
	}

	protected void fireUpdateNotifications(OrderDelta[] deltas) {
		if (deltas.length != 0) {
			OrderChangeEvent event = new OrderChangeEvent(this, deltas);
			Object[] l = listeners.getListeners();
			for (int i = 0; i < l.length; i++) {
				try {
					((IOrderChangeListener) l[i]).orderChanged(event);
				} catch (Throwable e) {
					Status status = new Status(Status.ERROR, Activator.PLUGIN_ID, 0, "Error running listener", e); //$NON-NLS-1$
					Activator.log(status);
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.trading.IBroker#getAccounts()
	 */
	public IAccount[] getAccounts() {
		return Activator.getDefault().getRepository().getAccounts();
	}
}
