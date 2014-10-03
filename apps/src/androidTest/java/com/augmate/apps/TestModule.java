package com.augmate.apps;

import com.augmate.apps.factories.BluetoothConnectorFactory;
import com.augmate.sdk.logger.Log;
import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;

public class TestModule extends AbstractModule {
    @Override
    protected void configure() {
        Log.debug("Loading guice test app module");

        install(new FactoryModuleBuilder().build(BluetoothConnectorFactory.class));
    }
}
