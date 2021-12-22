// IMyAidlInterface.aidl
package com.ninjuli.bigobject.ipc;

import com.ninjuli.bigobject.ipc.ICallbackInterface;

interface IMyAidlInterface {

    void clientSendserver(in ParcelFileDescriptor pfd);

    void registerCallback(ICallbackInterface callback);

    void unregisterCallback(ICallbackInterface callback);
}