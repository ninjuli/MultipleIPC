// ICallbackInterface.aidl
package com.ninjuli.bigobject.ipc;

interface ICallbackInterface {
    void serverSendclient(in ParcelFileDescriptor pfd);
}