package com.crucialtech.uberremake

import android.content.Intent
import android.content.IntentSender
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.bumptech.glide.Glide
import com.firebase.ui.auth.data.model.User
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.util.*
import kotlin.collections.HashMap

class DriverHomeActivity : AppCompatActivity() {


    private lateinit var appBarConfiguration: AppBarConfiguration


    lateinit var navView: NavigationView

    lateinit var drawerLayout: DrawerLayout

    lateinit var navController: NavController

    lateinit var waitingDialog: AlertDialog

    lateinit var imageAvatar: ImageView

    lateinit var storageReference : StorageReference

    var imageUri : Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_driver_home)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)


        drawerLayout = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.nav_view)
        navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
        init()
    }

    private fun init() {

        storageReference = FirebaseStorage.getInstance().reference

        waitingDialog = AlertDialog
            .Builder(this)
            .setMessage("Waiting ....")
            .setCancelable(false)
            .create()

        navView.setNavigationItemSelectedListener{
            if(it.itemId == R.id.nav_signout){
                val builder = AlertDialog.Builder(this)
                builder.setTitle("Sign out")
                    .setMessage("Do you really want to sign out")
                    .setNegativeButton("CANCEL") { dialogInterface, i ->
                        dialogInterface.dismiss()

                    }
                    .setPositiveButton("SIGN OUT") { _, _ ->
                        FirebaseAuth.getInstance().signOut()
                        val intent = Intent(this, MainActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        startActivity(intent)
                        finish()
                    }
                    .setCancelable(false)

                val dialog = builder.create()
                dialog.setOnShowListener {
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                        .setTextColor(resources.getColor(android.R.color.holo_red_dark))
                    dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                        .setTextColor(resources.getColor(R.color.colorAccent))
                }

                dialog.show()
            }
            true
        }


        val headerView = navView.getHeaderView(0)
        val txt_name = headerView.findViewById<TextView>(R.id.txt_name)
        val txt_photo = headerView.findViewById<TextView>(R.id.txt_phone)
        val txt_star = headerView.findViewById<TextView>(R.id.txt_star)
        imageAvatar = headerView.findViewById(R.id.img_avatar)

        txt_name.text = Common.buildWelcomeMessage()
        txt_photo.text = Common.currentUser?.phoneNumber
        txt_star.text = Common.currentUser?.rating.toString()

        if (
            Common.currentUser != null
            && !Common.currentUser?.avatar.isNullOrEmpty()
        ) {
            Glide.with(this).load(Common.currentUser!!.avatar).into(imageAvatar)
        }

        imageAvatar.setOnClickListener{
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(Intent.createChooser(intent,"Select Picture"),PICK_IMAGE_REQUEST)
        }

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.driver_home, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK){
            if(data != null && data.data != null){
                imageUri = data.data
                imageAvatar.setImageURI(imageUri)
                showDialogUpload()
            }
        }
    }

    private fun showDialogUpload() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Change Avatar")
            .setMessage("Do you really want to change Avatar?")
            .setNegativeButton("CANCEL") { dialogInterface, i ->
                dialogInterface.dismiss()

            }
            .setPositiveButton("CHANGE") { _, _ ->
                if(imageUri != null){
                    waitingDialog.show()
                    val avatarFolder = storageReference
                        .child("avatars/${FirebaseAuth.getInstance().currentUser?.uid}")

                    avatarFolder.putFile(imageUri!!).addOnFailureListener{
                        Snackbar.make(drawerLayout,it.message.toString(),Snackbar.LENGTH_LONG).show()
                        waitingDialog.dismiss()
                    }.addOnCompleteListener{
                        if(it.isSuccessful){
                            avatarFolder.downloadUrl.addOnSuccessListener {
                                val updateData = HashMap<String, Any>()
                                updateData.put("avatar",it.toString())
                                UserUtils.updateUser(drawerLayout,updateData)
                            }
                        }
                        waitingDialog.dismiss()
                    }.addOnProgressListener {
                        val progress = (100.0 * it.bytesTransferred / it.totalByteCount)
                        waitingDialog.setMessage("Uploading : $progress%")
                    }
                }
            }
            .setCancelable(false)

        val dialog = builder.create()
        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setTextColor(resources.getColor(android.R.color.holo_red_dark))
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                .setTextColor(resources.getColor(R.color.colorAccent))
        }

        dialog.show()
    }
}