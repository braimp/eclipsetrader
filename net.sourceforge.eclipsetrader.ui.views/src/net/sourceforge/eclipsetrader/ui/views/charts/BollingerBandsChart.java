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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * @author Marco
 */
public class BollingerBandsChart extends ChartPlotter implements IChartConfigurer
{
  private static final String PLUGIN_ID = "net.sourceforge.eclipsetrader.charts.bollinger"; //$NON-NLS-1$
  private int period = 15;
  private int deviations = 2;
  
  public BollingerBandsChart()
  {
    setName(Messages.getString("BollingerBandsChart.label")); //$NON-NLS-1$
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
    return getName() + " (" + period + "," + deviations + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
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

      // Colore della linea
      gc.setForeground(getColor());

      // Computa i punti
      if (chartData.length >= period)
      {
        // Calcola la media mobile
        double[] average = new double[chartData.length - period];
        for (int i = 0; i < average.length; i++)
        {
          for (int m = 0; m < period; m++)
            average[i] += chartData[i + m].getClosePrice();
          average[i] /= period;
        }
        
        // Calcola la deviazione standard
        double[] deviation = new double[chartData.length - period];
        for (int i = 0; i < deviation.length; i++)
        {
          for (int m = 0; m < period; m++)
            deviation[i] += Math.pow(chartData[i + m].getClosePrice() - average[i], 2);
          deviation[i] /= period;
          deviation[i] = Math.sqrt(deviation[i]);
        }
        
        // Calcola la banda superiore
        double[] value = new double[chartData.length - period];
        for (int i = 0; i < value.length; i++)
          value[i] = average[i] + deviation[i] * deviations;
        gc.setLineStyle(SWT.LINE_SOLID);
        this.drawLine(value, gc, height, period);

        // Banda intermedia (media mobile)
        gc.setLineStyle(SWT.LINE_DOT);
        this.drawLine(average, gc, height, period);
        
        // Calcola la banda inferiore
        for (int i = 0; i < value.length; i++)
          value[i] = average[i] - deviation[i] * deviations;
        gc.setLineStyle(SWT.LINE_SOLID);
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
    else if (name.equalsIgnoreCase("deviations") == true) //$NON-NLS-1$
      deviations = Integer.parseInt(value);
    super.setParameter(name, value);
  }

  
  /* (non-Javadoc)
   * @see net.sourceforge.eclipsetrader.ui.views.charts.IChartPlotter#createContents(org.eclipse.swt.widgets.Composite)
   */
  public Control createContents(Composite parent)
  {
    Label label = new Label(parent, SWT.NONE);
    label.setText(Messages.getString("BollingerBandsChart.periods")); //$NON-NLS-1$
    label.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL|GridData.HORIZONTAL_ALIGN_FILL));
    Text text = new Text(parent, SWT.BORDER);
    text.setData("period"); //$NON-NLS-1$
    text.setText(String.valueOf(period));
    GridData gridData = new GridData();
    gridData.widthHint = 25;
    text.setLayoutData(gridData);
    
    label = new Label(parent, SWT.NONE);
    label.setText(Messages.getString("BollingerBandsChart.deviations")); //$NON-NLS-1$
    label.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL|GridData.HORIZONTAL_ALIGN_FILL));
    text = new Text(parent, SWT.BORDER);
    text.setData("deviations"); //$NON-NLS-1$
    text.setText(String.valueOf(deviations));
    gridData = new GridData();
    gridData.widthHint = 25;
    text.setLayoutData(gridData);

    return parent;
  }
}
