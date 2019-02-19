- **dev** : [![pipeline status](https://gitlab.coppernic.fr/projectmngt/AndroidAppTemplate/badges/dev/pipeline.svg)](https://gitlab.coppernic.fr/projectmngt/AndroidAppTemplate/commits/dev)
- **master** : [![pipeline status](https://gitlab.coppernic.fr/projectmngt/AndroidAppTemplate/badges/master/pipeline.svg)](https://gitlab.coppernic.fr/projectmngt/AndroidAppTemplate/commits/master)

Android Template Project
========================

This template provides a starting point for Android project at Coppernic.

 ## Checklist

 - [ ] Clone this repository into a folder of your project's name `git clone git@gitlab-01.coppernic.local:projectmngt/AndroidAppTemplate.git MY_PROJECT`. Or if you're copying the folder, don't forget hidden files!
 - [ ] Reinitialize git
     - [ ] Delete the `.git` folder
     - [ ] Start a git repo with `git init`
     - [ ] Make initial git commit with all files
 - [ ] Update this template with new features if suitable
 - [ ] Change sources folder
     - [ ] Replace `template` by your own package name in main and tests sources
     - [ ] Change Application ID in `manifest.xml`
     - [ ] Change all path pointing to template package in `manifest.xml`
     - [ ] Change App's name
     - [ ] Update `settings.gradle` to point to the modules you added
     - [ ] Update `dependencies.gradle` and respective `build.gradle` files to make sure dependencies are hooked up and compiling properly
     - [ ] Update all fabric api keys if not public project
 - [ ] Update this `README.md` file to reflect your project.
     - [ ] Update the Travis Build Status badge to reflect your project
     - [ ] Delete everything above including these checkboxes


# Project Name

Replace this text with a synopsis of the library.

## Motivation

Explain why this library exists and what problems it solves.

## Download

Include instructions on how to integrate the library into your projects. For instance install in your build.gradle:

```
dependencies {

}
```

## Usage

Provide instructions on how to use and integrate the library into a project.

If there's some special pieces for testing (ie Mocks) explain those here as well.

## License

    Copyright (C) 2018 Coppernic

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

