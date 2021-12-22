package com.ninjuli.bigobject.server

import android.app.Service
import android.content.Intent
import android.os.*
import com.ninjuli.bigobject.ipc.ICallbackInterface
import com.ninjuli.bigobject.ipc.IMyAidlInterface
import org.greenrobot.eventbus.EventBus
import java.io.FileDescriptor
import java.io.FileInputStream
import java.io.IOException
import java.lang.reflect.Method
import org.greenrobot.eventbus.ThreadMode

import org.greenrobot.eventbus.Subscribe

class BigIpcService : Service() {
    /**
     * 主要作用是可以把多个callback保存到列表里，在合适的时机同时回调，
     * 也可以防止重复的调用相同的任务，只保证你需要的一个结果回调
     */
    val callBackList = RemoteCallbackList<ICallbackInterface>()

    override fun onBind(intent: Intent): IBinder {
        EventBus.getDefault().register(this)
        return myBinder
    }

    private val myBinder = object : IMyAidlInterface.Stub() {
        //接收客户端发来的数据
        override fun clientSendserver(pfd: ParcelFileDescriptor?) {
            /*** 从ParcelFileDescriptor中获取FileDescriptor */
            val fileDescriptor = pfd?.fileDescriptor

            /*** 根据FileDescriptor构建InputStream对象 */
            val fis = FileInputStream(fileDescriptor)

            /*** 从InputStream中读取字节数组 */
            val data = fis.readBytes()

            ipcBitmapBinder(data)
        }

        override fun registerCallback(callback: ICallbackInterface?) {
            callBackList.register(callback)
        }

        override fun unregisterCallback(callback: ICallbackInterface?) {
            callBackList.unregister(callback)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    /**
     * 将客户端发来的图片展示到服务端的ui
     * 在这里使用的putBinder传递大数据
     * @param byteArray ByteArray
     */
    private fun ipcBitmapBinder(byteArray: ByteArray) {
        val intent = Intent(this, MainActivity::class.java)
        val bundle = Bundle()
         bundle.putBinder("bitmap", ImageBinder(byteArray))
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        intent.putExtras(bundle)
        startActivity(intent)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEventMainThread(event: MessageEvent) {
        if (event.message == "sendClient") {
            serverToClient {
                getParcelFileDescriptor()
            }
        }
    }

    private fun serverToClient(pfd: () -> ParcelFileDescriptor?) {
        val n = callBackList.beginBroadcast()
        for (i in 0 until n) {
            val callback = callBackList.getBroadcastItem(i);
            if (callback != null) {
                try {
                    callback.serverSendclient(pfd())
                } catch (e: RemoteException) {
                    e.printStackTrace()
                }
            }
        }
        callBackList.finishBroadcast()
    }

    private fun getParcelFileDescriptor(): ParcelFileDescriptor? {
        var pdf: ParcelFileDescriptor? = null
        try {
            /*** 读取assets目录下文件 */
            val inputStream = assets.open("server.jpg")

            /*** 将inputStream转换成字节数组 */
            val byteArray = inputStream.readBytes()

            /*** 创建MemoryFile */
            val memoryFile = MemoryFile("image", byteArray.size)

            /*** 向MemoryFile中写入字节数组 */
            memoryFile.writeBytes(byteArray, 0, 0, byteArray.size)

            /*** 获取MemoryFile对应的FileDescriptor */
            val fd = getFileDescriptor(memoryFile)

            /*** 根据FileDescriptor创建ParcelFileDescriptor */
            pdf = ParcelFileDescriptor.dup(fd)

        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: RemoteException) {
            e.printStackTrace()
        }
        return pdf
    }

    private fun getFileDescriptor(memoryFile: MemoryFile): FileDescriptor? {
        var fd: FileDescriptor? = null
        fd = invokeKt(
            "android.os.MemoryFile",
            memoryFile,
            "getFileDescriptor"
        ) as FileDescriptor
        return fd
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

}