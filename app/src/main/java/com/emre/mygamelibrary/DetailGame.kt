package com.emre.mygamelibrary

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.database.sqlite.SQLiteDatabase
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.media.Image
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.emre.mygamelibrary.databinding.ActivityDetailGameBinding
import com.google.android.material.snackbar.Snackbar
import java.io.ByteArrayOutputStream

class DetailGame : AppCompatActivity() {

    private lateinit var binding: ActivityDetailGameBinding
    private lateinit var activityLauncher: ActivityResultLauncher<Intent> // Intent yapılacağı için Intent olarak verildi
    private lateinit var permissionLauncher: ActivityResultLauncher<String> // İzin kontrolü
    var imageBitmap: Bitmap? = null
    private lateinit var database: SQLiteDatabase
    var selectedID = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailGameBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        database = openOrCreateDatabase("Games", MODE_PRIVATE, null)
        val intent = intent
        val info = intent.getStringExtra("info")
        if (info.equals("new")) {
            binding.saveBtn.visibility = View.VISIBLE
            binding.deleteBtn.visibility = View.INVISIBLE
            binding.gameNameText.setText("")
        } else {
            binding.saveBtn.visibility = View.INVISIBLE
            binding.deleteBtn.visibility = View.VISIBLE
            selectedID = intent.getIntExtra("id", 0)
            val cursor = database.rawQuery("select * from games where id = ?", arrayOf(selectedID.toString()))
            val nameIndex = cursor.getColumnIndex("name")
            val imageIndex = cursor.getColumnIndex("image")
            while (cursor.moveToNext()) {

                binding.gameNameText.setText(cursor.getString(nameIndex))

                val byteArray = cursor.getBlob(imageIndex)
                val image = BitmapFactory.decodeByteArray(byteArray, 0,byteArray.size)
                // (Bitmap'e dönüştürülecek byte dizisi, dizinin hangi indexsinden başlanacak, dizi uzunluğu)
                binding.imageView.setImageBitmap(image)

            }
            cursor.close()
        }
        registerLauncher()
    }

    fun save(view: View) {

        var gameName = binding.gameNameText.text.toString()
        if (imageBitmap != null) {
            val smallBitmap = makeSmallerBitmap(imageBitmap!!,300)

            //Görseli byte dizisine çevirme
            val outPutStream = ByteArrayOutputStream()
            smallBitmap.compress(Bitmap.CompressFormat.PNG,100, outPutStream)
            val imageByteArray = outPutStream.toByteArray()

            try {
                database = openOrCreateDatabase("Games", MODE_PRIVATE, null)
                database.execSQL("create table if not exists games(id integer primary key, name varchar, image blob)")
                val sqlStatement = "insert into games(name, image) values (?, ?)"
                val statement = database.compileStatement(sqlStatement)
                statement.bindString(1,gameName)
                statement.bindBlob(2, imageByteArray)
                statement.execute()
            } catch (e: Exception) {
                e.printStackTrace()
            }

            returnToMain()
        }

    }

    private fun returnToMain() {
        val intentToMainActivity = Intent(this@DetailGame, MainActivity::class.java)
        intentToMainActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP) // Diğer tüm aktiviteleri kapatır
        startActivity(intentToMainActivity)
    }

    fun delete(view: View) {
        database.execSQL("delete from games where id = ?", arrayOf(selectedID.toString()))

        returnToMain()

    }

    private fun makeSmallerBitmap(image: Bitmap, maxSize: Int) : Bitmap {

        var width = image.width
        var height = image.height

        val bitmapRatio : Double = width.toDouble() / height.toDouble()

        if (bitmapRatio > 1) {
            // landscape
            width = maxSize
            height = (width / bitmapRatio).toInt()
        } else if(bitmapRatio < 1) {
            // potrait
            height = maxSize
            width = (height * bitmapRatio).toInt()
        } else {
            //square
            width = maxSize
            height = maxSize
        }

        return Bitmap.createScaledBitmap(image,width,height,true)
    }

    fun selectImage(view: View){

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            // Versiyon 33+

            if (ContextCompat.checkSelfPermission(this@DetailGame,Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                // İzim verilmedi

                if (ActivityCompat.shouldShowRequestPermissionRationale(this@DetailGame,Manifest.permission.READ_MEDIA_IMAGES)) {
                    // İzin verilmedikten sonra tekrar tıklandığında çalışacak kod bloğu
                    Snackbar.make(view, "Permission Needed for Gallery", Snackbar.LENGTH_INDEFINITE)
                        .setAction("Give Permission", View.OnClickListener {
                            // İzin ister
                            permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                        }).show()

                } else {
                    // İzin kontrolu yapar
                    permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                }

            } else {
                // İzin verildi
                val intentToGallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                // Action_Pick -> resme tıklanma işlemi. External Uri -> Görselin depolandığı alan
                activityLauncher.launch(intentToGallery)

            }
        } else {
            // Versiyon 32-

            if (ContextCompat.checkSelfPermission(this@DetailGame,Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                // İzim verilmedi

                if (ActivityCompat.shouldShowRequestPermissionRationale(this@DetailGame,Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    // İzin verilmedikten sonra tekrar tıklandığında çalışacak kod bloğu
                    println("snackbar")
                    //Snackbar.make(view,"Permission needed for gallery", Snackbar.LENGTH_INDEFINITE).setAction("Give Permission", View.OnClickListener {
                    // İzin kontrolu yapar
                    //permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                    //})
                    Snackbar.make(view, "Permission Needed for Gallery", Snackbar.LENGTH_INDEFINITE)
                        .setAction("Give Permission", View.OnClickListener {
                            // İzin ister
                            permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                        }).show()

                } else {
                    // İzin kontrolu yapar
                    println("no permission")
                    permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                }

            } else {
                // İzin verildi
                val intentToGallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                // Action_Pick -> resme tıklanma işlemi. External Uri -> Görselin depolandığı alan
                activityLauncher.launch(intentToGallery)

            }

        }


   }

    private fun registerLauncher() { // Launcher'ları kayıt etme

        activityLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result -> // Seçilenin sonucunu verir
            if (result.resultCode == RESULT_OK) { // Bir veriye tıklanıp tıklanmadığını kontrol eder

                if (result.data != null){ // Seçilen verinin gelip gelmediğini kontrol eder
                    val imageData = result.data!!.data // Seçilen verinin Uri bilgisini verir

                    if(imageData != null) { // Tekrar kontrol etmemizin sebebi, ilk kontrol verinin gelip gelmediğini, bu kontrol ise gelen verinin datasının gelip gelmediğini kontrol eder

                        try {
                            val decoderSource = ImageDecoder.createSource(this@DetailGame.contentResolver, imageData)
                            imageBitmap = ImageDecoder.decodeBitmap(decoderSource)
                            binding.imageView.setImageBitmap(imageBitmap)

                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }

        }
        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) {result -> // Boolen değer döndürür
            if (result){
                // Döndürülen değer true ise izin verilmiştir
                val intent = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityLauncher.launch(intent)
            } else {
                // Döndürülen değer false ise izin verilmemiştir
                Toast.makeText(this@DetailGame,"Permission needed", Toast.LENGTH_LONG).show()
            }

        }

    }

}