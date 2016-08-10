package org.eclipse.che.ide.flux.liveedit;

/**
 * Created by rnavagamuwa on 8/10/16.
 */
public interface EventActivityHandler {

    public void setMarkerForCursorOffSetChanged();
    public void setMarkerForResourceChanged();
    public void sendFluxMessageOnDocumentModelChanged();
}
