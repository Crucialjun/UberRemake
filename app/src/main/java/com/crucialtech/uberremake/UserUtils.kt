package com.crucialtech.uberremake

import android.view.View
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

object UserUtils {
    fun updateUser(view : View?,updateData : Map<String,Any>){
        FirebaseDatabase
            .getInstance()
            .getReference(DRIVER_INFO_REFERENCE).child(FirebaseAuth.getInstance().currentUser!!.uid)

            .updateChildren(updateData)
            .addOnFailureListener{
                Snackbar.make(view!!,it.message.toString(),Snackbar.LENGTH_LONG).show()
            }
            .addOnSuccessListener {
                Snackbar.make(view!!,"Update Information Successful",Snackbar.LENGTH_LONG)
            }
    }
}