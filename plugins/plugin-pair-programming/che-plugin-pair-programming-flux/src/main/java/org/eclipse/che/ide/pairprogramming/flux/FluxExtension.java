package org.eclipse.che.ide.pairprogramming.flux;

import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.core.client.ScriptInjector;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.user.client.Timer;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import org.eclipse.che.api.machine.shared.dto.MachineProcessDto;
import org.eclipse.che.api.machine.shared.dto.event.MachineProcessEvent;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.api.editor.document.Document;
import org.eclipse.che.ide.api.editor.text.TextPosition;
import org.eclipse.che.ide.api.editor.text.TextRange;
import org.eclipse.che.ide.api.editor.texteditor.TextEditorPresenter;
import org.eclipse.che.ide.api.machine.MachineServiceClient;
import org.eclipse.che.ide.api.notification.StatusNotification;
import org.eclipse.che.ide.api.workspace.WorkspaceReadyEvent;
import org.eclipse.che.ide.extension.machine.client.command.CommandManager;
import org.eclipse.che.ide.extension.machine.client.command.valueproviders.CommandPropertyValueProvider;
import org.eclipse.che.ide.pairprogramming.socketio.Consumer;
import org.eclipse.che.ide.pairprogramming.socketio.SocketIOOverlay;
import org.eclipse.che.ide.pairprogramming.socketio.SocketIOResources;
import org.eclipse.che.ide.api.extension.Extension;
import org.eclipse.che.ide.extension.machine.client.command.valueproviders.CommandPropertyValueProviderRegistry;
import org.eclipse.che.ide.pairprogramming.socketio.SocketOverlay;
import org.eclipse.che.ide.resource.Path;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;
import org.eclipse.che.ide.util.loging.Log;
import org.eclipse.che.ide.websocket.MessageBus;
import org.eclipse.che.ide.websocket.MessageBusProvider;
import org.eclipse.che.ide.websocket.WebSocketException;
import org.eclipse.che.ide.websocket.events.MessageHandler;
import org.eclipse.che.ide.websocket.rest.SubscriptionHandler;
import org.eclipse.che.ide.websocket.rest.Unmarshallable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Extension(title = "Che Flux extension", version = "1.0.0")
public class FluxExtension {

    private EventBus eventBus;
    private AppContext appContext;
    private MachineServiceClient machineServiceClient;
    private CommandPropertyValueProviderRegistry commandPropertyValueProviderRegistry;
    private CommandManager commandManager;
    private SocketOverlay socket;
    private Map<String, Document> liveDocuments   = new HashMap<String, Document>();
    private boolean   isUpdatingModel = false;
    private Path path;
    private EditorPartPresenter openedEditor;
    private EditorAgent editorAgent;
    private TextEditorPresenter textEditor;
    private static final String channelName = "USER";
    private MessageBus messageBus;
    private DtoUnmarshallerFactory dtoUnmarshallerFactory;


    @Inject
    public FluxExtension(final EventBus eventBus , final AppContext appContext , final MachineServiceClient machineServiceClient, final CommandPropertyValueProviderRegistry commandPropertyValueProviderRegistry, final CommandManager commandManager, EditorAgent editorAgent, final MessageBusProvider messageBusProvider, final DtoUnmarshallerFactory dtoUnmarshallerFactory){

        this.eventBus = eventBus;
        this.appContext = appContext;
        this.machineServiceClient = machineServiceClient;
        this.commandPropertyValueProviderRegistry = commandPropertyValueProviderRegistry;
        this.commandManager = commandManager;
        this.editorAgent = editorAgent;
        this.messageBus = messageBusProvider.getMessageBus();
        this.dtoUnmarshallerFactory = dtoUnmarshallerFactory;
        injectSocketIO();
        connectToFluxOnProjectLoaded();
        connectToFluxOnFluxProcessStarted();
    }

    private void injectSocketIO() {
        SocketIOResources ioresources = GWT.create(SocketIOResources.class);
        ScriptInjector.fromString(ioresources.socketIo().getText()).setWindow(ScriptInjector.TOP_WINDOW).inject();
    }

    private void connectToFluxOnProjectLoaded() {
        eventBus.addHandler(WorkspaceReadyEvent.getType(), new WorkspaceReadyEvent.WorkspaceReadyHandler() {
            @Override
            public void onWorkspaceReady(WorkspaceReadyEvent workspaceReadyEvent) {
                String machineId = appContext.getDevMachine().getId();
                Promise<List<MachineProcessDto>> processesPromise = machineServiceClient.getProcesses(machineId);
                processesPromise.then(new Operation<List<MachineProcessDto>>() {
                    @Override
                    public void apply(final List<MachineProcessDto> descriptors) throws OperationException {
                        if (descriptors.isEmpty()) {
                            return;
                        }
                        for (MachineProcessDto machineProcessDto : descriptors) {
                            if (connectIfFluxMicroservice(machineProcessDto)) {
                                break;
                            }
                        }
                    }
                });
            }
        });
    }

    private boolean connectIfFluxMicroservice(MachineProcessDto descriptor) {
        if (descriptor == null) {
            return false;
        }
        if ("flux".equals(descriptor.getName())) {
            String urlToSubstitute = "http://${server.port.3000}";
            if (commandPropertyValueProviderRegistry == null) {
                return false;
            }
            substituteAndConnect(urlToSubstitute);
            return true;
        }
        return false;
    }

    int trySubstitude = 10;

    public void substituteAndConnect(final String previewUrl) {
        commandManager.substituteProperties(previewUrl).then(new Operation<String>() {
            @Override
            public void apply(final String url) throws OperationException {
                if (url.contains("$")){
                    Timer t = new Timer() {
                        @Override
                        public void run() {
                            substituteAndConnect(url);
                        }
                    };
                    Log.info(FluxExtension.class,"Retrieving the preview url for " + url);
                    t.schedule(1000);
                    return;
                }
                connectToFlux(url);


            }
        });
    }

    public static native SocketIOOverlay getSocketIO()/*-{
        return $wnd.io;
    }-*/;

    protected void connectToFlux(final String url) {

        final SocketIOOverlay io = getSocketIO();
        Log.info(getClass(), "connecting to " + url);

        socket = io.connect(url);
        socket.on("error", new Runnable() {
            @Override
            public void run() {
                Log.info(getClass(), "error connecting to " + url);
            }
        });

        socket.on("liveResourceChanged", new Consumer<FluxResourceChangedEventDataOverlay>() {
            @Override
            public void accept(FluxResourceChangedEventDataOverlay event) {

            }
        });

        socket.on("liveCursorOffsetChanged", new Consumer<FluxResourceChangedEventDataOverlay>() {
            @Override
            public void accept(FluxResourceChangedEventDataOverlay event) {

            }
        });

        socket.emit("connectToChannel", JsonUtils.safeEval("{\"channel\" : \""+channelName+"\"}"));
    }

    private void connectToFluxOnFluxProcessStarted() {
        eventBus.addHandler(WorkspaceReadyEvent.getType(), new WorkspaceReadyEvent.WorkspaceReadyHandler() {
            @Override
            public void onWorkspaceReady(WorkspaceReadyEvent workspaceReadyEvent) {
                String machineId = appContext.getDevMachine().getId();
                final Unmarshallable<MachineProcessEvent> unmarshaller = dtoUnmarshallerFactory.newWSUnmarshaller(MachineProcessEvent.class);
                final String processStateChannel = "machine:process:" + machineId;
                final MessageHandler handler = new SubscriptionHandler<MachineProcessEvent>(unmarshaller) {
                    @Override
                    protected void onMessageReceived(MachineProcessEvent result) {

                    }

                    @Override
                    protected void onErrorReceived(Throwable exception) {
                        Log.error(getClass(), exception);
                    }
                };
                wsSubscribe(processStateChannel, handler);
            }
        });
    }

    private void wsSubscribe(String wsChannel, MessageHandler handler) {
        try {
            messageBus.subscribe(wsChannel, handler);
        } catch (WebSocketException e) {
            Log.error(getClass(), e);
        }
    }
}
