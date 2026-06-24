package frolenko.client.service;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import common.dto.ErrorResponse;
import common.dto.work.GalleryItem;
import common.dto.work.GetWorkResponse;
import common.protocol.CommandType;
import common.utils.JsonUtil;
import frolenko.client.core.ServerApi;

import java.util.List;
import java.util.function.Consumer;

@Singleton
public class GalleryService {

    private final ServerApi serverApi;

    @Inject
    public GalleryService(ServerApi serverApi) {
        this.serverApi = serverApi;
    }

    public void getGallery(String title, String ownerUsername,
                           Consumer<List<GalleryItem>> onSuccess, Consumer<String> onError) {
        serverApi.getGallery(title, ownerUsername, packet -> {
            if (packet.bMsg().cType() == CommandType.GALLERY.getCode()) {
                List<GalleryItem> items = List.of(JsonUtil.fromBytes(packet.bMsg().payload(), GalleryItem[].class));
                onSuccess.accept(items);
            } else {
                onError.accept(extractError(packet));
            }
        });
    }

    public void getMyWorks(String token, Consumer<List<GalleryItem>> onSuccess, Consumer<String> onError) {
        serverApi.getMyWorks(token, packet -> {
            if (packet.bMsg().cType() == CommandType.MY_WORKS.getCode()) {
                List<GalleryItem> items = List.of(JsonUtil.fromBytes(packet.bMsg().payload(), GalleryItem[].class));
                onSuccess.accept(items);
            } else {
                onError.accept(extractError(packet));
            }
        });
    }

    public void getWork(int workId, Consumer<GetWorkResponse> onSuccess, Consumer<String> onError) {
        serverApi.getWork(workId, packet -> {
            if (packet.bMsg().cType() == CommandType.WORK.getCode()) {
                GetWorkResponse work = JsonUtil.fromBytes(packet.bMsg().payload(), GetWorkResponse.class);
                onSuccess.accept(work);
            } else {
                onError.accept(extractError(packet));
            }
        });
    }

    public void saveWork(int roomId, String token, String title, boolean isPublic,
                         Runnable onSuccess, Consumer<String> onError) {
        serverApi.saveWork(roomId, token, title, isPublic, packet -> {
            if (packet.bMsg().cType() == CommandType.OK.getCode()) {
                onSuccess.run();
            } else {
                onError.accept(extractError(packet));
            }
        });
    }

    public void deleteWork(String token, int workId, Runnable onSuccess, Consumer<String> onError) {
        serverApi.deleteWork(token, workId, packet -> {
            if (packet.bMsg().cType() == CommandType.OK.getCode()) {
                onSuccess.run();
            } else {
                onError.accept(extractError(packet));
            }
        });
    }

    private String extractError(common.model.Packet packet) {
        return JsonUtil.fromBytes(packet.bMsg().payload(), ErrorResponse.class).message();
    }
}