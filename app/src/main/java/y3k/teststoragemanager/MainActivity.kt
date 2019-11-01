package y3k.teststoragemanager

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.os.storage.StorageManager
import android.util.Log
import android.widget.Toast
import androidx.core.content.getSystemService
import androidx.core.net.toFile
import androidx.documentfile.provider.DocumentFile
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {

    private val storageManager by lazy {
        getSystemService<StorageManager>()!!
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.d("Storage", "android version=${Build.VERSION.SDK_INT}")
        hello.setOnClickListener {
            it.isEnabled = false
            storageManager.storageVolumes.forEach {storage->
                if(storage.isRemovable){
                    hello.text = "Open Movies in ${storage.getDescription(this)}"
                    startActivityForResult(storage.createAccessIntent(Environment.DIRECTORY_MOVIES),1234)
                    return@setOnClickListener
                }
            }
            Toast.makeText(this,"OOPS, No External Storage?",Toast.LENGTH_SHORT).show()
            it.isEnabled = true
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode==Activity.RESULT_OK){
            contentResolver.takePersistableUriPermission(data!!.data!!,
                Intent.FLAG_GRANT_READ_URI_PERMISSION + Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            val newMp4File = DocumentFile.fromTreeUri(this,data.data!!)!!.createFile("video/mp4","small")
            thread {
                val assetInputStream = assets.open("small.mp4")
                val mp4OutputStream = contentResolver.openOutputStream(newMp4File!!.uri)
                assetInputStream.copyTo(mp4OutputStream!!)
                mp4OutputStream.close()
                assetInputStream.close()
                runOnUiThread {
                    hello.text = "small.mp4 file written"
                }
            }
        } else{
            finish()
        }
    }
}
