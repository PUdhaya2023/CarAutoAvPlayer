package com.exampl.carautoavplayer.activity

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.exampl.carautoavplayer.R
import com.exampl.carautoavplayer.databinding.ActivityMainBinding
import com.exampl.carautoavplayer.fragment.AudioFragment
import com.exampl.carautoavplayer.fragment.PlayListFragment
import com.exampl.carautoavplayer.fragment.VideoFragment

class MainActivity : AppCompatActivity() {

    private lateinit var requestPermissionLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)




        requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
                if (permissions[Manifest.permission.READ_MEDIA_AUDIO] == true &&
                    permissions[Manifest.permission.READ_MEDIA_VIDEO] == true
                ) {
                    Toast.makeText(this@MainActivity, "Permission granted", Toast.LENGTH_SHORT)
                        .show()


                } else {
                    Toast.makeText(this@MainActivity, "Permission Denied", Toast.LENGTH_SHORT)
                        .show()
                }


            }
        /*final ArrayList<String> args = new ArrayList<>();
        args.add("-vvv");*/if (Build.VERSION.SDK_INT >= 33) {
            checkPermission()

            binding.bottomNav.setOnItemSelectedListener {

                when (it.itemId) {

                    R.id.audio -> {
                        loadFragement(AudioFragment())
                        true
                    }
                    R.id.video -> {
                        loadFragement(VideoFragment())
                        true
                    }
                    R.id.playlist -> {
                        loadFragement(PlayListFragment())
                        true
                    }

                    else -> {
                        loadFragement(AudioFragment())
                        true
                    }

                }

            }
            binding.bottomNav.setSelectedItemId(R.id.audio);
        }

    }

    private fun loadFragement(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.container, fragment)
        transaction.commit()
    }

    private fun checkPermission() {

        if (
            ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.READ_MEDIA_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_MEDIA_VIDEO
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            Toast.makeText(this@MainActivity, "Permission granted", Toast.LENGTH_SHORT)
                .show()
        } else if (ActivityCompat.shouldShowRequestPermissionRationale(
                this, Manifest.permission.READ_MEDIA_AUDIO
            ) && ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.READ_MEDIA_VIDEO
            )
        ) {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Permission Required")
            builder.setMessage("This app requires AUDIO and VIDEO permission")
            builder.setCancelable(false)
            builder.setPositiveButton("OK") { dialog, which ->
                if (Build.VERSION.SDK_INT >= 33) {
                    requestPermissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.READ_MEDIA_AUDIO,
                            Manifest.permission.READ_MEDIA_VIDEO
                        )
                    )
                }
                dialog.dismiss()

            }

            builder.setNegativeButton("Cancel") { dialog, which ->

                dialog.dismiss()
            }
            builder.show()


        } else {
            // Everything is fine you can simply request the permission
            if (Build.VERSION.SDK_INT >= 33) {
                requestPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.READ_MEDIA_AUDIO,
                        Manifest.permission.READ_MEDIA_VIDEO
                    )
                )
            }
        }
    }
}

/* if (ContextCompat.checkSelfPermission(this@MainActivity,Manifest.permission.READ_MEDIA_AUDIO)+
     ContextCompat.checkSelfPermission(this@MainActivity,Manifest.permission.READ_MEDIA_VIDEO)
     == PackageManager.PERMISSION_DENIED) {

     // if permission is not granted then we are requesting for the permissions on below line.
     Log.d("PERMISSION:SELF","GRANTED")
     if (Build.VERSION.SDK_INT >= 33) {
         Log.d("PERMISSION:33","GRANTED")
         ActivityCompat.requestPermissions(
             this@MainActivity,
             arrayOf(Manifest.permission.READ_MEDIA_AUDIO,Manifest.permission.READ_MEDIA_VIDEO),
             100
         )
     }
 } else {
     // if permission is already granted then we are displaying a toast message as permission granted.
     Log.d("PERMISSION","GRANTED")
     Toast.makeText(this@MainActivity, "Permission already granted", Toast.LENGTH_SHORT)
         .show()
 }*/


/*override fun onRequestPermissionsResult(
    requestCode: Int, permissions: Array<String>,
    grantResults: IntArray
) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)

    // on below line we are checking for the request code.
    if (requestCode == 100) {
        // on below line we are checking if permissions are granted.

        // on below line we are checking if permissions are granted.
        if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // displaying a toast message when permission is granted.
            Log.d("PERMISSION:ONREQUEST","GRANTED")
            Toast.makeText(this@MainActivity, "Permission Granted", Toast.LENGTH_SHORT)
                .show()
        } else {
            Log.d("PERMISSION::ONREQUEST","DENIED")
            // displaying a toast message when permission is denied.
            Toast.makeText(this@MainActivity, "Permission Denied", Toast.LENGTH_SHORT)
                .show()
        }

}
}*/



