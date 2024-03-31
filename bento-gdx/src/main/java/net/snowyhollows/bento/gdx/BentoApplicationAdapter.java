package net.snowyhollows.bento.gdx;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import net.snowyhollows.bento.Bento;
import net.snowyhollows.bento.BentoFactory;
import net.snowyhollows.bento.config.io.ReaderProvider;
import net.snowyhollows.bento.config.io.ReaderProviderFactory;

import java.io.Reader;
import java.util.function.Consumer;

public final class BentoApplicationAdapter implements ApplicationListener {

    private final BentoFactory<? extends ApplicationListener> factory;
    private final Bento bento;
    private final Consumer<Bento> configurer;
    ApplicationListener applicationListener;

    public BentoApplicationAdapter(BentoFactory<? extends ApplicationListener> factory) {
        this(factory, bento -> {});
    }

    public BentoApplicationAdapter(BentoFactory<? extends ApplicationListener> factory, Consumer<Bento> configurer) {
        this(Bento.createRoot(), factory, configurer);
    }

    public BentoApplicationAdapter(Bento bento, BentoFactory<? extends ApplicationListener> factory, Consumer<Bento> configurer) {
        this.bento = bento;
        this.factory = factory;
        this.configurer = configurer;
    }

    @Override
    public void create() {
        bento.register(ReaderProviderFactory.IT, new ReaderProvider() {
            @Override
            public Reader readerForFile(String path) {
                Gdx.app.log("Main", "Reading " + path);
                if (path.startsWith("./")) {
                    path = path.substring(2);
                }
                return Gdx.files.internal(path).reader();
            }
        });
        configurer.accept(bento);
        applicationListener = bento.get(factory);
        applicationListener.create();
    }

    @Override
    public void resize(int width, int height) {
        applicationListener.resize(width, height);
    }

    @Override
    public void render() {
        applicationListener.render();
    }

    @Override
    public void pause() {
        applicationListener.pause();
    }

    @Override
    public void resume() {
        applicationListener.resume();
    }

    @Override
    public void dispose() {
        applicationListener.dispose();
    }
}
