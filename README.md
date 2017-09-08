Serial
======

Sample to demonstrate serial communication using CpcCore lib from Coppernic

:bangbang: On Coppernic's devices running Android **4.4** and above CpcSystemServices app from Coppernic need to be installed. :bangbang:

Build
-----

**build.gradle**

```groovy
allprojects {
    repositories {
        jcenter()
        maven { url "https://artifactory.coppernic.fr/artifactory/libs-release" }
    }
}

dependencies {
    compile(group: 'fr.coppernic.sdk.core', name: 'CpcCore', version: '1.0.1', ext: 'aar') {
        transitive = true
    }
}
```

Code
----

Two kinds of serial communication are supported :

  * Serial called `Direct` that comes from linux kernel
  * FTDI chip

### Getting instance to use kernel tty

```java
    private final InstanceListener<SerialCom> directListener = new InstanceListener<SerialCom>() {
        @Override
        public void onCreated(SerialCom serialCom) {
            direct = serialCom;
        }

        @Override
        public void onDisposed(SerialCom serialCom) {
            if (direct == serialCom) {
                direct = null;
            }
        }
    };

    SerialFactory.getDirectInstance(getActivity(), directListener);
```

### Getting instance to use FTDI

[USB permissions need to be handled !](https://developer.android.com/guide/topics/connectivity/usb/host.html)

```java
    private final InstanceListener<SerialCom> ftdiListener = new InstanceListener<SerialCom>() {
        @Override
        public void onCreated(SerialCom serialCom) {
            ftdi = serialCom;
        }

        @Override
        public void onDisposed(SerialCom serialCom) {
            if (ftdi == serialCom) {
                ftdi = null;
            }
        }
    };

    private BroadcastReceiver usbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                SerialFactory.getFtdiInstance(getContext(), ftdiListener);
            }
        }
    };


```

### Use SerialCom instance

```java
// First list devices
List<String> directList = Arrays.asList(direct.listDevices());

int ret = serial.open(directList[0], 9600);

byte[] tx = "hello".getBytes();
serial.send(tx, tx.length);

int nb = serial.getQueueStatus();
if(nb > 0){
    byte[] res = new byte[nb];
    int ret = this.serial.receive(1000, nb, res);
}

serial.close();
```

Contribute
----------

This project use versioning model from [Nemerosa](https://github.com/nemerosa/versioning)

Licence
-------

Copyright 2017 Coppernic

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.