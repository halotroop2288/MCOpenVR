# MCOpenVR
A helper library for implementing Steam's OpenVR API in Minecraft.

![Minecraft Version](https://img.shields.io/badge/Minecraft-1.16.4-green)

## Usage
Use JITPack to get the latest version of this with Gradle.

If no version is available, use the latest commit hash in place of `<version>`.

![GitHub release (latest SemVer)](https://img.shields.io/github/v/release/halotroop2288/MCOpenVR?sort=semver)

```groovy
repositories {
	maven {
		name "HalfOf2" // Needed for GrossFabricHacks (native entrypoint)
		url "https://raw.githubusercontent.com/Devan-Kerman/Devan-Repo/master/"
	}
    maven {
        name = "JITPack"
        url = "https://jitpack.io"
    }
}

dependencies {
    include(modApi("com.github.halotroop2288:MCOpenVR:<version>"))
}
```

### Last Resort
If that doesn't work, clone the repo and publish it to maven local.<br/>
Information about that can be found below.

If you do it this way, you need to change your Gradle config to load
the dependency from `com.halotroop` instead of `com.github.halotroop2288`
and use the correct versioning
(`<mod version>+openVR<OpenVR version>-<MC version>`)

```
dependencies {
    include(modApi("com.halotroop:MCOpenVR:<version>"))
}
```

## Cloning this repo
Clone this repository recursively to download the included version of OpenVR.
It may take quite some time to download, please be patient. The library is required.

Alternatively, you could download the DLLs and place them in the correct places, but
I will provide no support for this method.

After cloning the repo, open it in your favourite IDE, generate runs,
and add this to your VM arguments:

```jvm
-Djna.library.path=<Path to MCOpenVR Workspace>/openvr/bin/<PLATFORM>
```

# Licenses
### Mod
This mod and its source code are released under version 3 of the Lesser GNU Public Licence. <br/>
For more information, see [LICENSE](https://github.com/halotroop2288/MCOpenVR/blob/trunk/LICENSE).

### OpenVR
OpenVR is Copyright (c) 2015, Valve Corporation. All rights reserved. <br/>
Redistribution of the OpenVR API library or its source code requires a full copy
of the provided license to be included.<br/> 
For more information, see [LICENSE.OPENVR](https://github.com/halotroop2288/MCOpenVR/blob/trunk/LICENSE.OPENVR).
