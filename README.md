# MCOpenVR
A helper library for implementing Steam's OpenVR API in Minecraft.

![Minecraft Version](https://img.shields.io/badge/Minecraft-1.16.4-green)

## Usage
Use JITPack to get the latest version of this with Gradle.

If no version is available, use the latest commit hash in place of `<version>`.

![GitHub All Releases](https://img.shields.io/github/downloads/halotroop2288/MCOpenVR/total)

```groovy
repositories {
    maven {
        name = "JITPack"
        url = "https://jitpack.io"
    }
}

dependencies {
    include(modApi("com.github.halotroop2288:MCOpenVR:<version>"))
}
```

## Cloning this repo
Clone this repository recursively to download the included version of OpenVR.
It may take quite some time to download, please be patient. The library is required.

Alternatively, you could download the DLLs and place them in the correct places, but
I will provide no support for this method.

# License
This mod and its source code are released under the MIT licence. <br/>
For more information, see [LICENSE](https://github.com/halotroop2288/MCOpenVR/blob/trunk/LICENSE).

# OpenVR License
OpenVR is Copyright (c) 2015, Valve Corporation. All rights reserved. <br/>
Redistribution of the OpenVR API library or its source code requires a full copy
of the provided license to be included.<br/> 
For more information, see [LICENSE.OPENVR](https://github.com/halotroop2288/MCOpenVR/blob/trunk/LICENSE.OPENVR).
