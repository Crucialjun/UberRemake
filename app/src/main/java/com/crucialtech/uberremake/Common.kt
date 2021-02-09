package com.crucialtech.uberremake

import java.lang.StringBuilder

object Common {

    fun buildWelcomeMessage():String{
        return "Welcome ${currentUser?.firstName} ${currentUser?.lastName} "
    }

    var currentUser : DriverInfoModel? = null
}