package com.ninjuli.ipc

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * 描述：
 * author: hui.zhong
 * email: hui.zhong@oneiotworld.com
 * created 2021/12/13
 */
@Parcelize
data class Person(val name: String, val age: Int) : Parcelable
