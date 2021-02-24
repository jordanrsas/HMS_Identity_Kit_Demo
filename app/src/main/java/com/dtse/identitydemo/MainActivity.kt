/**
 * Copyright 2020. Huawei Technologies Co., Ltd. All rights reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dtse.identitydemo

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.IntentSender.SendIntentException
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.huawei.hms.identity.Address
import com.huawei.hms.identity.entity.GetUserAddressResult
import com.huawei.hms.identity.entity.UserAddress
import com.huawei.hms.identity.entity.UserAddressRequest
import com.dtse.identitydemo.log.LogUtil
import com.dtse.identitydemo.util.isConnected
import com.dtse.identitydemo.util.toast
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "identitycodelab"
        private const val GET_ADDRESS = 1000
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        query_user_address.setOnClickListener {
            if (this.isConnected) {
                getUserAddress()
            } else {
                this.toast("Please check your internet connection..")
            }
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        LogUtil.i(TAG, "onActivityResult requestCode $requestCode resultCode $resultCode")
        when (requestCode) {
            GET_ADDRESS -> when (resultCode) {
                Activity.RESULT_OK -> {
                    val userAddress = UserAddress.parseIntent(data)
                    if (userAddress != null) {
                        val sb = StringBuilder()
                        sb.apply {
                            append("name: ${userAddress.name} ,\n")
                            append("city: ${userAddress.administrativeArea} ,\n")
                            append("area: ${userAddress.locality} ,\n")
                            append("address: ${userAddress.addressLine1} ${userAddress.addressLine2} ,\n")
                            append("phone: ${userAddress.phoneNumber}")
                        }
                        Log.i(TAG, "user address is $sb")
                        user_address.text = sb.toString()
                    } else {
                        user_address.text = "Failed to get user address."
                    }
                }
                Activity.RESULT_CANCELED -> {
                }
                else -> LogUtil.e(TAG, "result is wrong, result code is $resultCode")
            }
            else -> {
            }
        }
    }

    private fun getUserAddress() {
        val req = UserAddressRequest()
        val task = Address.getAddressClient(this).getUserAddress(req)
        task.addOnSuccessListener { result ->
            LogUtil.i(TAG, "onSuccess result code: ${result.returnCode}")
            try {
                startActivityForResult(result)
            } catch (e: SendIntentException) {
                e.printStackTrace()
            }
        }.addOnFailureListener { e -> LogUtil.i(TAG, "on Failed result code:${e.message}") }
    }

    private fun startActivityForResult(result: GetUserAddressResult) {
        val status = result.status
        if (result.returnCode == 0 && status.hasResolution()) {
            LogUtil.i(TAG, "the result had resolution.")
            status.startResolutionForResult(this, GET_ADDRESS)
        } else {
            LogUtil.i(TAG, "the response is wrong, the return code is ${result.returnCode}")
        }
    }
}
