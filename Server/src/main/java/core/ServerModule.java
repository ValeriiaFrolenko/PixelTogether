package core;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import database.ConnectionManager;
import database.ConnectionProvider;

public class ServerModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(ConnectionProvider.class).to(ConnectionManager.class).in(Singleton.class);
    }

    @Provides
    @Singleton
    @Named("aesKey")
    byte[] provideAesKey() {
        String key = System.getenv("PIXEL_AES_KEY");
        if (key == null || key.length() != 16) {
            throw new IllegalStateException("PIXEL_AES_KEY env variable must be set and 16 chars");
        }
        return key.getBytes();
    }
}