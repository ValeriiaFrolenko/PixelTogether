package frolenko.client.service;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import common.dto.ErrorResponse;
import common.dto.user.AuthResponse;
import common.protocol.CommandType;
import common.utils.JsonUtil;
import frolenko.client.core.ServerApi;

import java.util.function.Consumer;

@Singleton
public class AuthService {

    private final ServerApi serverApi;

    @Inject
    public AuthService(ServerApi serverApi) {
        this.serverApi = serverApi;
    }

    public void login(String username, String password,
                      Consumer<String> onSuccess, Consumer<String> onError) {
        serverApi.login(username, password, packet -> {
            if (packet.bMsg().cType() == CommandType.OK.getCode()) {
                AuthResponse auth = JsonUtil.fromBytes(packet.bMsg().payload(), AuthResponse.class);
                onSuccess.accept(auth.token());
            } else {
                ErrorResponse error = JsonUtil.fromBytes(packet.bMsg().payload(), ErrorResponse.class);
                onError.accept(error.message());
            }
        });
    }

    public void register(String username, String password,
                         Consumer<String> onSuccess, Consumer<String> onError) {
        serverApi.register(username, password, packet -> {
            if (packet.bMsg().cType() == CommandType.OK.getCode()) {
                AuthResponse auth = JsonUtil.fromBytes(packet.bMsg().payload(), AuthResponse.class);
                onSuccess.accept(auth.token());
            } else {
                ErrorResponse error = JsonUtil.fromBytes(packet.bMsg().payload(), ErrorResponse.class);
                onError.accept(error.message());
            }
        });
    }

    public void logout(String token, Runnable onSuccess, Consumer<String> onError) {
        serverApi.logout(token, packet -> {
            if (packet.bMsg().cType() == CommandType.OK.getCode()) {
                onSuccess.run();
            } else {
                ErrorResponse error = JsonUtil.fromBytes(packet.bMsg().payload(), ErrorResponse.class);
                onError.accept(error.message());
            }
        });
    }
}