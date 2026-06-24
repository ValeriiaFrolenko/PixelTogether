package frolenko.client.core;

import com.google.inject.Singleton;
import common.dto.room.RoomInfo;
import common.dto.work.GalleryItem;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.security.SecureRandom;

@Singleton
public class AppState {

    private final long sessionId = new SecureRandom().nextLong();

    private final StringProperty token = new SimpleStringProperty(null);
    private final StringProperty nickname = new SimpleStringProperty(null);
    private final ObjectProperty<RoomState> currentRoom = new SimpleObjectProperty<>(null);
    private final ObservableList<RoomInfo> rooms = FXCollections.observableArrayList();
    private final ObservableList<GalleryItem> gallery = FXCollections.observableArrayList();

    private final BooleanProperty serverAvailable =
            new SimpleBooleanProperty(false);

    public long getSessionId() { return sessionId; }

    public String getToken() { return token.get(); }
    public void setToken(String token) { this.token.set(token); }
    public StringProperty tokenProperty() { return token; }

    public String getNickname() { return nickname.get(); }
    public void setNickname(String nickname) { this.nickname.set(nickname); }
    public StringProperty nicknameProperty() { return nickname; }

    public RoomState getCurrentRoom() { return currentRoom.get(); }
    public void setCurrentRoom(RoomState room) { this.currentRoom.set(room); }
    public ObjectProperty<RoomState> currentRoomProperty() { return currentRoom; }

    public ObservableList<RoomInfo> getRooms() { return rooms; }
    public ObservableList<GalleryItem> getGallery() { return gallery; }

    public boolean isLoggedIn() { return token.get() != null; }

    public boolean isServerAvailable() { return serverAvailable.get(); }
    public void setServerAvailable(boolean available) { serverAvailable.set(available); }
    public BooleanProperty serverAvailableProperty() { return serverAvailable; }

    public void clear() {
        token.set(null);
        nickname.set(null);
        currentRoom.set(null);
        rooms.clear();
        gallery.clear();
    }
}