/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.ui.dialogs.choice;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

import org.eclipse.che.ide.ui.WidgetFocusTracker;
import org.eclipse.che.ide.ui.window.Window;

/**
 * The footer show on choice dialogs.
 *
 * @author Mickaël Leduque
 * @author Artem Zatsarynnyi
 */
public class ChoiceDialogFooter implements IsWidget {

    private static final Window.Resources           resources = GWT.create(Window.Resources.class);
    /** The UI binder instance. */
    private static       ChoiceDialogFooterUiBinder uiBinder  = GWT.create(ChoiceDialogFooterUiBinder.class);

    @UiField
    Button firstChoiceButton;
    @UiField
    Button secondChoiceButton;
    @UiField
    Button thirdChoiceButton;
    HTMLPanel rootPanel;

    /** The action delegate. */
    private       ChoiceDialogView.ActionDelegate actionDelegate;
    private final WidgetFocusTracker              widgetFocusTracker;

    @Inject
    public ChoiceDialogFooter(WidgetFocusTracker widgetFocusTracker) {
        this.widgetFocusTracker = widgetFocusTracker;
        rootPanel = uiBinder.createAndBindUi(this);

        trackFocusForWidgets();

        firstChoiceButton.addStyleName(resources.windowCss().primaryButton());
        firstChoiceButton.getElement().setId("ask-dialog-first");

        secondChoiceButton.addStyleName(resources.windowCss().button());
        secondChoiceButton.getElement().setId("ask-dialog-second");

        thirdChoiceButton.addStyleName(resources.windowCss().button());
        thirdChoiceButton.getElement().setId("ask-dialog-third");
    }

    /**
     * Sets the action delegate.
     *
     * @param delegate
     *         the new value
     */
    public void setDelegate(final ChoiceDialogView.ActionDelegate delegate) {
        this.actionDelegate = delegate;
    }

    /**
     * Handler set on the first button.
     *
     * @param event
     *         the event that triggers the handler call
     */
    @UiHandler("firstChoiceButton")
    public void handleFirstChoiceClick(final ClickEvent event) {
        this.actionDelegate.firstChoiceClicked();
    }

    /**
     * Handler set on the second button.
     *
     * @param event
     *         the event that triggers the handler call
     */
    @UiHandler("secondChoiceButton")
    public void handleSecondChoiceClick(final ClickEvent event) {
        this.actionDelegate.secondChoiceClicked();
    }

    /**
     * Handler set on the third button.
     *
     * @param event
     *         the event that triggers the handler call
     */
    @UiHandler("thirdChoiceButton")
    public void handleThirdChoiceClick(final ClickEvent event) {
        this.actionDelegate.thirdChoiceClicked();
    }

    /** Returns {@code true} if first button is in the focus and {@code false} - otherwise. */
    boolean isFirstButtonInFocus() {
        return widgetFocusTracker.isWidgetFocused(firstChoiceButton);
    }

    /** Returns {@code true} if second button is in the focus and {@code false} - otherwise. */
    boolean isSecondButtonInFocus() {
        return widgetFocusTracker.isWidgetFocused(secondChoiceButton);
    }

    /** Returns {@code true} if third button is in the focus and {@code false} - otherwise. */
    boolean isThirdButtonInFocus() {
        return widgetFocusTracker.isWidgetFocused(thirdChoiceButton);
    }

    private void trackFocusForWidgets() {
        widgetFocusTracker.subscribe(firstChoiceButton);
        widgetFocusTracker.subscribe(secondChoiceButton);
        widgetFocusTracker.subscribe(thirdChoiceButton);
    }

    private void unTrackFocusForWidgets() {
        widgetFocusTracker.unSubscribe(firstChoiceButton);
        widgetFocusTracker.unSubscribe(secondChoiceButton);
        widgetFocusTracker.unSubscribe(thirdChoiceButton);
    }

    public void onClose() {
        unTrackFocusForWidgets();
    }

    @Override
    public Widget asWidget() {
        return rootPanel;
    }

    /** The UI binder interface for this component. */
    interface ChoiceDialogFooterUiBinder extends UiBinder<HTMLPanel, ChoiceDialogFooter> {
    }
}