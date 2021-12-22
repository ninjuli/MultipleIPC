package com.ninjuli.client

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.ninjuli.ipc.IMyAidlInterface
import com.ninjuli.ipc.Person

class MainActivity : AppCompatActivity() {
    var ipcAidl: IMyAidlInterface? = null
    var bindFlag = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bindService()

        findViewById<Button>(R.id.btn).setOnClickListener {
            findViewById<TextView>(R.id.text).text = ipcAidl?.ipcMsg()
        }

        findViewById<Button>(R.id.btn1).setOnClickListener {
            val addPerson = ipcAidl?.addPerson(Person("小青", 21))
            findViewById<TextView>(R.id.text).text = addPerson?.toString()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (bindFlag) unbindService(serviceConnection)
    }

    private fun bindService() {
        val mIntent = Intent()
        mIntent.component = ComponentName("com.ninjuli.ipc", "com.ninjuli.ipc.IpcService")
        bindFlag = bindService(mIntent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(p0: ComponentName?, iBinder: IBinder?) {
            ipcAidl = IMyAidlInterface.Stub.asInterface(iBinder)
        }

        override fun onServiceDisconnected(p0: ComponentName?) {
            ipcAidl = null
        }
    }
}