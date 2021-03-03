# MinecraftPluginDevelopment

MinecraftPluginDevelopment is a utility plugin with the core idea of __Compiling to the same bytecode as a normal
plugin.__

Any plugins compiled using MinecraftPluginDevelopment are completely standalone from MinecraftPluginDevelopment.

# Adding Gradle Plugin

Add the plugin:
```
plugins {
    id 'com.bigbade.minecraftplugindevelopment' version "{VERSION}"
}
```

# Adding Maven Plugin

TODO

# Tasks

This plugin currently adds two tasks: setupServer and runServer. runServer depends on setupServer. 
It moves the plugin jar and runs the server.
setupServer downloads the latest build for the local version.
