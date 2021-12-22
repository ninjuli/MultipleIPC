// IMyAidlInterface.aidl
package com.ninjuli.ipc;

import com.ninjuli.ipc.Person;

interface IMyAidlInterface {
    String ipcMsg();

    List<Person> addPerson(in Person person);
}