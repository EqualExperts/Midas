package com.ee.midas

import com.ee.midas.transform.TransformType

class Configuration {
    private def apps

    public Configuration(URL configUrl) {
        apps = new ConfigSlurper().parse(configUrl).apps
    }

    def TransformType mode() {
        def mode = apps.mode as String
        TransformType.valueOf(mode.toUpperCase())
    }
}
