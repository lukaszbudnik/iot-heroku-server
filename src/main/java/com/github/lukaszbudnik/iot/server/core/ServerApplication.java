package com.github.lukaszbudnik.iot.server.core;

import com.github.lukaszbudnik.gpe.PropertiesElResolverModule;
import com.github.lukaszbudnik.iot.server.service.HomeService;
import com.github.lukaszbudnik.iot.server.service.RegistryService;
import com.github.lukaszbudnik.iot.server.service.TelemetryService;
import com.google.inject.Guice;
import com.google.inject.Injector;
import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

import java.util.stream.Stream;

public class ServerApplication extends Application<Configuration> {
    public static void main(String[] args) throws Exception {
        new ServerApplication().run(args);
    }

    @Override
    public void run(Configuration configuration, Environment environment) throws Exception {
        Injector injector = Guice.createInjector(new PropertiesElResolverModule(configuration.getProperties()));

        Stream.of(TelemetryService.class, RegistryService.class, HomeService.class)
                .forEach((c) -> {
                    Object s = injector.getInstance(c);
                    environment.jersey().register(s);
                });
    }

    @Override
    public void initialize(Bootstrap<Configuration> bootstrap) {
        bootstrap.addBundle(new AssetsBundle("/web", "/web", null, "web"));
        bootstrap.addBundle(new AssetsBundle("/js", "/js", null, "js"));
    }
}
