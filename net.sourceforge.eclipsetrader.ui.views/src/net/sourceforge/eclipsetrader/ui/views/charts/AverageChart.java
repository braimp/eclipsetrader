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
package net.sourceforge.eclipsetrader.ui.views.charts;

import net.sourceforge.eclipsetrader.ui.internal.views.Messages;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * Moving Average chart.
 * <p></p>
 * 
 * @author Marco Maccaferri
 */
public class AverageChart extends ChartPlotter implements IChartConfigurer
{
  public static final int SIMPLE = 0;
  public static final int EXPONENTIAL = 1;
  public static String PLUGIN_ID = "net.sourceforge.eclipsetrader.charts.average"; //$NON-NLS-1$
  private int period = 7;
  private int type = SIMPLE;
  
  public AverageChart()
  {
    setName(Messages.getString("AverageChart.label")); //$NON-NLS-1$
  }

  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.ui.views.charts.IChartPlotter#getId()
   */
  public String getId()
  {
    return PLUGIN_ID;
  }

  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.ui.views.charts.IChartPlotter#getDescription()
   */
  public String getDescription()
  {
    return getName() + " (" + period + ")"; //$NON-NLS-1$ //$NON-NLS-2$
  }
  
  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.ui.views.charts.IChartPlotter#paintChart(GC gc, int width, int height)
   */
  public void paintChart(GC gc, int width, int height)
  {
    super.paintChart(gc, width, height);
    if (chartData != null && getMax() > getMin())
    {
      // Determina il rapporto tra l'altezza del canvas e l'intervallo min-max
      double pixelRatio = height / (getMax() - getMin());

      // Tipo di line e colore
      gc.setLineStyle(SWT.LINE_SOLID);
      gc.setForeground(getColor());

      // Computa i punti
      if (chartData.length >= period)
      {
        double[] value = new double[chartData.length - period];
        for (int i = 0; i < value.length; i++)
        {
          for (int m = 0; m < period; m++)
            value[i] += chartData[i + m].getClosePrice();
          value[i] /= period;
        }
        if (type == EXPONENTIAL)
        {
          double percentage = 2.0 / (period + 1);
          for (int i = 1; i < value.length; i++)
          {
            value[i] = (chartData[i + period].getClosePrice() * percentage) + (value[i - 1] * (1.0 - percentage));
          }
        }
        this.drawLine(value, gc, height, period);
      }
    }
  }

  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.ui.views.charts.IChartPlotter#paintScale(GC gc, int width, int height)
   */
  public void paintScale(GC gc, int width, int height)
  {
  }

  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.ui.views.charts.IChartPlotter#setParameter(String name, String value)
   */
  public void setParameter(String name, String value)
  {
    if (name.equalsIgnoreCase("period") == true) //$NON-NLS-1$
      period = Integer.parseInt(value);
    else if (name.equalsIgnoreCase("type") == true) //$NON-NLS-1$
      type = Integer.parseInt(value);
    super.setParameter(name, value);
  }

  
  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.ui.views.charts.IChartPlotter#createContents(org.eclipse.swt.widgets.Composite)
   */
  public Control createContents(Composite parent)
  {
    Label label = new Label(parent, SWT.NONE);
    label.setText(Messages.getString("AverageChart.periods")); //$NON-NLS-1$
    label.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL|GridData.HORIZONTAL_ALIGN_FILL));
    Text text = new Text(parent, SWT.BORDER);
    text.setData("period"); //$NON-NLS-1$
    text.setText(String.valueOf(period));
    GridData gridData = new GridData();
    gridData.widthHint = 25;
    text.setLayoutData(gridData);

    label = new Label(parent, SWT.NONE);
    label.setText(Messages.getString("AverageChart.type")); //$NON-NLS-1$
    label.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL|GridData.HORIZONTAL_ALIGN_FILL));
    Combo combo = new Combo(parent, SWT.READ_ONLY);
    combo.setData("type"); //$NON-NLS-1$
    combo.add(Messages.getString("AverageChart.simple")); //$NON-NLS-1$
    combo.add(Messages.getString("AverageChart.exponential")); //$NON-NLS-1$
    combo.setText(combo.getItem(type));
    combo.setLayoutData(new GridData());

    return parent;
  }
}
