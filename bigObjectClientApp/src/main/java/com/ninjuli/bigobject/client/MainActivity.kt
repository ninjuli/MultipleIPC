package com.ninjuli.bigobject.client

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.BitmapFactory
import android.os.*
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.ninjuli.bigobject.ipc.ICallbackInterface
import com.ninjuli.bigobject.ipc.IMyAidlInterface
import java.io.FileDescriptor
import java.io.FileInputStream
import java.io.IOException
import java.lang.reflect.Method

class MainActivity : AppCompatActivity() {
    var ipcAidl: IMyAidlInterface? = null
    var bindFlag = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bindService()

        findViewById<Button>(R.id.btn).setOnClickListener {
            sendBigImage()
        }
    }

    private fun sendBigImage() {
        ipcAidl?.apply {
            try {
                /*** 读取assets目录下文件 */
                val inputStream = assets.open("client.jpg")

                /*** 将inputStream转换成字节数组 */
                val byteArray = inputStream.readBytes()

                /*** 创建MemoryFile */
                val memoryFile = MemoryFile("image", byteArray.size)

                /*** 向MemoryFile中写入字节数组 */
                memoryFile.writeBytes(byteArray, 0, 0, byteArray.size)

                /**
                 * 获取MemoryFile对应的FileDescriptor
                 * MemoryFile下的getFileDescriptor是@hide,在这用反射使用他
                 */
                val fd = getFileDescriptor(memoryFile)

                /*** 根据FileDescriptor创建ParcelFileDescriptor */
                val pfd = ParcelFileDescriptor.dup(fd)

                /*** 发送数据 */
                ipcAidl?.clientSendserver(pfd)
            } catch (e: IOException) {
                e.printStackTrace()
            } catch (e: RemoteException) {
                e.printStackTrace()
            }
        }
    }

    private fun getFileDescriptor(memoryFile: MemoryFile?): FileDescriptor? {
        if (memoryFile == null)
            return null
        //val fd: FileDescriptor?
        return invokeKt(
            "android.os.MemoryFile",
            memoryFile,
            "getFileDescriptor"
        ) as FileDescriptor
    }

    private fun invokeKt(
        className: String,
        instance: Any?,
        methodName: String,
    ): Any? {
        try {
            val c = Class.forName(className)
            val method: Method = c.getDeclaredMethod(methodName)
            method.isAccessible = true
            return method.invoke(instance)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        if (bindFlag) unbindService(serviceConnection)
    }

    private fun bindService() {
        val mIntent = Intent()
        mIntent.component =
            ComponentName(
                "com.ninjuli.bigobject.server",
                "com.ninjuli.bigobject.server.BigIpcService"
            )
        bindFlag = bindService(mIntent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(p0: ComponentName?, iBinder: IBinder?) {
            ipcAidl = IMyAidlInterface.Stub.asInterface(iBinder)
            ipcAidl?.registerCallback(serverCallBack)
        }

        override fun onServiceDisconnected(p0: ComponentName?) {
            ipcAidl = null
        }
    }

    private val serverCallBack = object : ICallbackInterface.Stub() {
        override fun serverSendclient(pfd: ParcelFileDescriptor?) {
            val fileDescriptor = pfd?.fileDescriptor
            val fis = FileInputStream(fileDescriptor)
            val bytes = fis.readBytes()
            bytes.let {
                val bitMap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                runOnUiThread {
                    findViewById<ImageView>(R.id.image).setImageBitmap(bitMap)
                }
            }
        }
    }
}