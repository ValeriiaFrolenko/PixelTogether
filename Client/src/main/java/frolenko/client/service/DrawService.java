package frolenko.client.service;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import common.dto.draw.PixelUpdate;
import frolenko.client.core.ServerApi;

import java.util.List;
import java.util.function.Consumer;

@Singleton
public class DrawService {

    private final ServerApi serverApi;

    @Inject
    public DrawService(ServerApi serverApi) {
        this.serverApi = serverApi;
    }

    public void draw(int roomId, List<PixelUpdate> pixels, Consumer<String> onError) {
        serverApi.draw(roomId, pixels, packet -> {
            if (packet.bMsg().cType() != common.protocol.CommandType.OK.getCode()
                    && packet.bMsg().cType() != common.protocol.CommandType.DRAW.getCode()) {
                String error = common.utils.JsonUtil.fromBytes(
                        packet.bMsg().payload(), common.dto.ErrorResponse.class).message();
                onError.accept(error);
            }
        });
    }
}