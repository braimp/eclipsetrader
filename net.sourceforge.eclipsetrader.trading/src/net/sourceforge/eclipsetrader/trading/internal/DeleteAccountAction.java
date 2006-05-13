/*
 * Copyright (c) 2004-2006 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 */

package net.sourceforge.eclipsetrader.trading.internal;

import net.sourceforge.eclipsetrader.core.CorePlugin;
import net.sourceforge.eclipsetrader.core.db.PersistentObject;
import net.sourceforge.eclipsetrader.trading.views.AccountsView;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.TreeItem;

public class DeleteAccountAction extends DeleteAction
{
    private AccountsView view;

    public DeleteAccountAction(AccountsView view)
    {
        this.view = view;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.action.Action#run()
     */
    public void run()
    {
        if (MessageDialog.openConfirm(view.getViewSite().getShell(), view.getPartName(), "Do you really want to delete the selected account(s) ?"))
        {
            TreeItem[] items = view.getTree().getSelection();
            for (int i = 0; i < items.length; i++)
                CorePlugin.getRepository().delete((PersistentObject)items[i].getData());
        }
    }
}
