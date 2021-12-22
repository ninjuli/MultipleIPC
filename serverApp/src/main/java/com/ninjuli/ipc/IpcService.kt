package com.ninjuli.ipc

import android.app.Service
import android.content.Intent
import android.os.IBinder
/**
 * 描述： 创建service实现IMyAidlInterface接口
 * author: ninjuli
 * email: ninjuli@163.com
 * created 2021/12/13
 */
class IpcService : Service() {

    override fun onBind(intent: Intent): IBinder {
        return myBinder
    }

    private val myBinder = object: IMyAidlInterface.Stub(){
        override fun ipcMsg(): String {
            return "服务端返回消息"
        }

        override fun addPerson(person: Person): MutableList<Person> {
            return mutableListOf(person)
        }
    }
}