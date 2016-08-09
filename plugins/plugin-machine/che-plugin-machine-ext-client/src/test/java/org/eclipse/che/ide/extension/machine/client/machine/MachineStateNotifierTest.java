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
package org.eclipse.che.ide.extension.machine.client.machine;

import com.google.gwt.event.shared.SimpleEventBus;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.machine.shared.dto.MachineConfigDto;
import org.eclipse.che.api.machine.shared.dto.MachineDto;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.ide.api.machine.MachineServiceClient;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.notification.StatusNotification;
import org.eclipse.che.ide.api.workspace.event.EnvironmentStatusChangedEvent;
import org.eclipse.che.ide.extension.machine.client.MachineLocalizationConstant;
import org.eclipse.che.ide.ui.loaders.initialization.InitialLoadingInfo;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.eclipse.che.api.machine.shared.dto.event.MachineStatusEvent.EventType.CREATING;
import static org.eclipse.che.api.machine.shared.dto.event.MachineStatusEvent.EventType.DESTROYED;
import static org.eclipse.che.api.machine.shared.dto.event.MachineStatusEvent.EventType.ERROR;
import static org.eclipse.che.api.machine.shared.dto.event.MachineStatusEvent.EventType.RUNNING;
import static org.eclipse.che.ide.ui.loaders.initialization.InitialLoadingInfo.Operations.MACHINE_BOOTING;
import static org.eclipse.che.ide.ui.loaders.initialization.OperationInfo.Status.IN_PROGRESS;
import static org.eclipse.che.ide.ui.loaders.initialization.OperationInfo.Status.SUCCESS;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Roman Nikitenko
 */
@RunWith(MockitoJUnitRunner.class)
public class MachineStateNotifierTest {
    private static final String MACHINE_NAME = "machineName";
    private static final String MACHINE_ID   = "machineId";

    //constructor mocks
    @Mock
    private InitialLoadingInfo          initialLoadingInfo;
    @Mock
    private NotificationManager         notificationManager;
    @Mock
    private MachineLocalizationConstant locale;
    @Mock
    private MachineServiceClient        machineServiceClient;

    //additional mocks
    @Mock
    private MachineDto                            machine;
    @Mock
    private MachineConfigDto                      machineConfig;
    @Mock
    private MachineStateEvent.Handler             handler;
    @Mock
    private EnvironmentStatusChangedEvent         environmentStatusChangedEvent;
    @Mock
    private Promise<MachineDto>                   machinePromise;
    @Captor
    private ArgumentCaptor<Operation<MachineDto>> machineCaptor;

    private EventBus eventBus = new SimpleEventBus();
    private MachineStatusNotifier statusNotifier;

    @Before
    public void setUp() {
        statusNotifier = new MachineStatusNotifier(eventBus, initialLoadingInfo, machineServiceClient, notificationManager, locale);
        eventBus.addHandler(MachineStateEvent.TYPE, handler);

        when(machine.getConfig()).thenReturn(machineConfig);
        when(machineConfig.getName()).thenReturn(MACHINE_NAME);
        when(environmentStatusChangedEvent.getMachineId()).thenReturn(MACHINE_ID);
        when(environmentStatusChangedEvent.getMachineName()).thenReturn(MACHINE_NAME);
        when(machineServiceClient.getMachine(MACHINE_ID)).thenReturn(machinePromise);
        when(machinePromise.then(Matchers.<Operation<MachineDto>>anyObject())).thenReturn(machinePromise);
        when(machinePromise.catchError(Matchers.<Operation<PromiseError>>anyObject())).thenReturn(machinePromise);
    }

    @Test
    public void shouldNotifyWhenDevMachineStateIsCreating() throws Exception {
        when(machineConfig.isDev()).thenReturn(true);

        when(environmentStatusChangedEvent.getEventType()).thenReturn(CREATING);
        statusNotifier.onEnvironmentStatusChanged(environmentStatusChangedEvent);

        verify(machinePromise).then(machineCaptor.capture());
        machineCaptor.getValue().apply(machine);

        verify(machine).getConfig();
        verify(machineConfig).isDev();
        verify(handler).onMachineCreating(Matchers.<MachineStateEvent>anyObject());
        verify(initialLoadingInfo).setOperationStatus(eq(MACHINE_BOOTING.getValue()), eq(IN_PROGRESS));
    }

    @Test
    public void shouldNotifyWhenNonDevMachineStateIsCreating() throws Exception {
        when(machineConfig.isDev()).thenReturn(false);

        when(environmentStatusChangedEvent.getEventType()).thenReturn(CREATING);
        statusNotifier.onEnvironmentStatusChanged(environmentStatusChangedEvent);

        verify(machinePromise).then(machineCaptor.capture());
        machineCaptor.getValue().apply(machine);

        verify(machine).getConfig();
        verify(machineConfig).isDev();
        verify(handler).onMachineCreating(Matchers.<MachineStateEvent>anyObject());
        verify(initialLoadingInfo, never()).setOperationStatus(eq(MACHINE_BOOTING.getValue()), eq(IN_PROGRESS));
    }

    @Test
    public void shouldNotifyWhenDevMachineStateIsRunning() throws Exception {
        when(machineConfig.isDev()).thenReturn(true);

        when(environmentStatusChangedEvent.getEventType()).thenReturn(RUNNING);
        statusNotifier.onEnvironmentStatusChanged(environmentStatusChangedEvent);

        verify(machinePromise).then(machineCaptor.capture());
        machineCaptor.getValue().apply(machine);

        verify(machine).getConfig();
        verify(machineConfig).isDev();
        verify(handler).onMachineRunning(Matchers.<MachineStateEvent>anyObject());
        verify(initialLoadingInfo).setOperationStatus(eq(MACHINE_BOOTING.getValue()), eq(SUCCESS));
        verify(locale).notificationMachineIsRunning(MACHINE_NAME);
        verify(notificationManager).notify(anyString(), (StatusNotification.Status)anyObject(), anyObject());
    }

    @Test
    public void shouldNotifyWhenNonDevMachineStateIsRunning() throws Exception {
        when(machineConfig.isDev()).thenReturn(false);

        when(environmentStatusChangedEvent.getEventType()).thenReturn(RUNNING);
        statusNotifier.onEnvironmentStatusChanged(environmentStatusChangedEvent);

        verify(machinePromise).then(machineCaptor.capture());
        machineCaptor.getValue().apply(machine);

        verify(machine).getConfig();
        verify(machineConfig).isDev();
        verify(handler).onMachineRunning(Matchers.<MachineStateEvent>anyObject());
        verify(initialLoadingInfo, never()).setOperationStatus(eq(MACHINE_BOOTING.getValue()), eq(SUCCESS));
        verify(locale).notificationMachineIsRunning(MACHINE_NAME);
        verify(notificationManager).notify(anyString(), (StatusNotification.Status)anyObject(), anyObject());
    }

    @Test
    public void shouldNotifyWhenMachineStateIsDestroyed() throws Exception {
        when(environmentStatusChangedEvent.getEventType()).thenReturn(DESTROYED);
        statusNotifier.onEnvironmentStatusChanged(environmentStatusChangedEvent);

        verify(locale).notificationMachineDestroyed(MACHINE_NAME);
        verify(notificationManager).notify(anyString(), (StatusNotification.Status)anyObject(), anyObject());
    }

    @Test
    public void shouldNotifyWhenMachineStateIsError() throws Exception {
        when(environmentStatusChangedEvent.getEventType()).thenReturn(ERROR);
        statusNotifier.onEnvironmentStatusChanged(environmentStatusChangedEvent);

        verify(environmentStatusChangedEvent).getError();
        verify(notificationManager).notify(anyString(), (StatusNotification.Status)anyObject(), anyObject());
    }
}
