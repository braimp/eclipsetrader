/*******************************************************************************
 * Copyright (c) 2004 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 *******************************************************************************/
package net.sourceforge.eclipsetrader;


/**
 * @author Marco
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public interface IChartDataProvider
{

  /**
   * Method to return the chart data array.<br>
   *
   * @return Returns the IChartData array.
   */
  public abstract IChartData[] getData(IBasicData data);

  /**
   * Method to update the chart data.<br>
   */
  public abstract void update(IBasicData data);
}