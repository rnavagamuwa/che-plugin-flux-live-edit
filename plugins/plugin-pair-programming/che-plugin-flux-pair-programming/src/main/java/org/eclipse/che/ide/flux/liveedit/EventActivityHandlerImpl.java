package org.eclipse.che.ide.flux.liveedit;


import com.google.gwt.user.client.Timer;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.editor.document.Document;
import org.eclipse.che.ide.api.editor.document.DocumentHandle;
import org.eclipse.che.ide.api.editor.events.DocumentChangeEvent;
import org.eclipse.che.ide.api.editor.events.DocumentChangeHandler;
import org.eclipse.che.ide.api.editor.events.DocumentReadyEvent;
import org.eclipse.che.ide.api.editor.events.DocumentReadyHandler;
import org.eclipse.che.ide.socketio.Message;
import org.eclipse.che.ide.socketio.SocketOverlay;

import java.util.HashMap;
import java.util.Map;

public class EventActivityHandlerImpl implements EventActivityHandler{

    private EventBus eventBus;
    private String userId;
    private Map<String, Document> liveDocuments   = new HashMap<String, Document>();
    private Document documentMain;
    private CursorHandlerForPairProgramming cursorHandlerForPairProgramming;
    private CursorModelForPairProgramming cursorModelForPairProgramming;
    private SocketOverlay socket;
    private String channelName = "USER";
    private Boolean isDocumentChanged = false;
    private boolean isUpdatingModel = false;
    private EditorAgent editorAgent;


    @Inject
    public EventActivityHandlerImpl(final EventBus eventBus,EditorAgent editorAgent){
        this.eventBus = eventBus;
        this.editorAgent = editorAgent;
    }

    @Override
    public void setMarkerForCursorOffSetChanged() {

    }

    @Override
    public void setMarkerForResourceChanged() {

    }

    @Override
    public void sendFluxMessageOnDocumentModelChanged() {
        cursorHandlerForPairProgramming = new CursorHandlerForPairProgramming();
        eventBus.addHandler(DocumentReadyEvent.TYPE, new DocumentReadyHandler() {
            @Override
            public void onDocumentReady(DocumentReadyEvent event) {
                userId = "user" + Math.random();
                liveDocuments.put(event.getDocument().getFile().getLocation().toString(), event.getDocument());
                documentMain = event.getDocument();
                final DocumentHandle documentHandle = documentMain.getDocumentHandle();
                initCursorHandler();
                /*here withUserName method sets the channel name*/
                Message message = new FluxMessageBuilder().with(documentMain).withChannelName(userId).withUserName(channelName) //
                        .buildResourceRequestMessage();
                socket.emit(message);
                documentHandle.getDocEventBus().addHandler(DocumentChangeEvent.TYPE, new DocumentChangeHandler() {
                    @Override
                    public void onDocumentChange(DocumentChangeEvent event) {
                        if (socket != null) {
                            /*here withUserName method sets the channel name and the withchannelName sets the username*/
                            Message liveResourceChangeMessage = new FluxMessageBuilder().with(event).withUserName(channelName).withChannelName(userId)//
                                    .buildLiveResourceChangeMessage();
                            isDocumentChanged = true;
                            if (isUpdatingModel) {
                                return;
                            }
                            socket.emit(liveResourceChangeMessage);

                        }
                    }
                });
            }
        });
    }

    private void initCursorHandler(){
        if (socket!=null){
            cursorModelForPairProgramming = new CursorModelForPairProgramming(documentMain, socket, editorAgent, channelName, userId);
            return;
        }
        Timer t = new Timer() {
            @Override
            public void run() {
                initCursorHandler();
            }
        };
        t.schedule(1000);
    }
}
