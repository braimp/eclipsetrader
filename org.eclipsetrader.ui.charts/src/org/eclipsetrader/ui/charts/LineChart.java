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

package org.eclipsetrader.ui.charts;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipsetrader.core.charts.IDataSeries;

/**
 * Draws a line chart.
 *
 * @since 1.0
 */
public class LineChart implements IChartObject {
	private IDataSeries dataSeries;
	private IChartObject parent;

	private LineStyle style;
	private RGB color;

	private IAdaptable[] values;
	private Point[] pointArray;

	public static enum LineStyle {
		Solid,
		Dot,
		Dash,
		Invisible
	}

	public LineChart(IDataSeries dataSeries, LineStyle style, RGB color) {
	    this.dataSeries = dataSeries;
	    this.style = style;
	    this.color = color;
    }

	public RGB getColor() {
    	return color;
    }

	public void setColor(RGB color) {
    	this.color = color;
    }

	/* (non-Javadoc)
     * @see org.eclipsetrader.ui.charts.IChartObject#setDataBounds(org.eclipsetrader.ui.charts.DataBounds)
     */
    public void setDataBounds(DataBounds dataBounds) {
    	List<IAdaptable> l = new ArrayList<IAdaptable>(2048);
    	for (IAdaptable value : dataSeries.getValues()) {
        	Date date = (Date) value.getAdapter(Date.class);
        	if ((dataBounds.first == null || !date.before(dataBounds.first)) && (dataBounds.last == null || !date.after(dataBounds.last)))
        		l.add(value);
    	}
    	this.values = l.toArray(new IAdaptable[l.size()]);
    	this.pointArray = null;
    }

	/* (non-Javadoc)
     * @see org.eclipsetrader.ui.charts.IChartObject#paint(org.eclipsetrader.ui.charts.IGraphics)
     */
    public void paint(IGraphics graphics) {
    	if (pointArray == null && values != null && style != LineStyle.Invisible) {
    		pointArray = new Point[values.length];
    		for (int i = 0; i < values.length; i++) {
    			Date date = (Date) values[i].getAdapter(Date.class);
    			Number value = (Number) values[i].getAdapter(Number.class);
    			pointArray[i] = graphics.mapToPoint(date, value);
    		}
    	}

    	if (pointArray != null && style != LineStyle.Invisible) {
        	switch(style) {
        		case Dash:
        			graphics.setLineStyle(SWT.LINE_DASH);
        			break;
        		case Dot:
        			graphics.setLineStyle(SWT.LINE_DOT);
        			break;
        		default:
        			graphics.setLineStyle(SWT.LINE_SOLID);
    				break;
        	}

        	graphics.pushState();
        	graphics.setForegroundColor(color);
        	graphics.drawPolyline(pointArray);
        	graphics.popState();
    	}
    }

	/* (non-Javadoc)
	 * @see org.eclipsetrader.ui.charts.IChartObject#containsPoint(int, int)
	 */
	public boolean containsPoint(int x, int y) {
		if (pointArray != null)
			return PixelTools.isPointOnLine(x, y, pointArray);
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.ui.charts.IChartObject#getDataSeries()
	 */
	public IDataSeries getDataSeries() {
		return dataSeries;
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.ui.charts.IChartObject#getToolTip()
	 */
	public String getToolTip() {
		return dataSeries.getName();
	}

	/* (non-Javadoc)
     * @see org.eclipsetrader.ui.charts.IChartObject#getToolTip(int, int)
     */
    public String getToolTip(int x, int y) {
		if (pointArray != null) {
			for (int i = 1; i < pointArray.length; i++) {
				if (PixelTools.isPointOnLine(x, y, pointArray[i - 1].x, pointArray[i - 1].y, pointArray[i].x, pointArray[i].y))
					return dataSeries.getName() + " " + String.valueOf(values[i - 1].getAdapter(Number.class));
			}
		}
	    return null;
    }

	/* (non-Javadoc)
     * @see org.eclipsetrader.ui.charts.IChartObject#add(org.eclipsetrader.ui.charts.IChartObject)
     */
    public void add(IChartObject object) {
    }

	/* (non-Javadoc)
     * @see org.eclipsetrader.ui.charts.IChartObject#remove(org.eclipsetrader.ui.charts.IChartObject)
     */
    public void remove(IChartObject object) {
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.ui.charts.IChartObject#getChildren()
     */
    public IChartObject[] getChildren() {
	    return null;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.ui.charts.IChartObject#getParent()
     */
    public IChartObject getParent() {
	    return parent;
    }

	/* (non-Javadoc)
     * @see org.eclipsetrader.ui.charts.IChartObject#setParent(org.eclipsetrader.ui.charts.IChartObject)
     */
    public void setParent(IChartObject parent) {
    	this.parent = parent;
    }

	/* (non-Javadoc)
     * @see org.eclipsetrader.ui.charts.IChartObject#accept(org.eclipsetrader.ui.charts.IChartObjectVisitor)
     */
    public void accept(IChartObjectVisitor visitor) {
    	visitor.visit(this);
    }
}
