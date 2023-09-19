package com.erendogan.fotogunluk

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.erendogan.fotogunluk.databinding.ActivityDetayBinding
import com.google.android.material.snackbar.Snackbar
import java.io.ByteArrayOutputStream

class Detay : AppCompatActivity() {
    private lateinit var binding: ActivityDetayBinding
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var permissionLauncher: ActivityResultLauncher<String>
    private lateinit var bitmap:Bitmap
    private var isPicture = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetayBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        supportActionBar?.hide()

        val myIntent = intent
        val gelenID = myIntent.getIntExtra("data",-1)
        if (gelenID != -1){
            println(gelenID)
            try {
                val db = this.openOrCreateDatabase("gunluk", MODE_PRIVATE,null)
                val cursor = db.rawQuery("SELECT * FROM gunlukler WHERE id = ?",arrayOf(gelenID.toString()))
                if (cursor != null && cursor.moveToFirst()) { // cursor null değilse ve veri varsa
                    val isimIX = cursor.getColumnIndex("isim")
                    val notIX = cursor.getColumnIndex("nott")
                    val resimIX = cursor.getColumnIndex("resim")
                    val resim = cursor.getBlob(resimIX)

                    binding.imageView2.setImageBitmap(BitmapFactory.decodeByteArray(resim, 0, resim.size))
                    binding.imageView2.isClickable = false
                    binding.imageView2.isFocusable = false

                    binding.textIsim.isEnabled = false
                    binding.textIsim.setText(cursor.getString(isimIX))

                    binding.textNot.isEnabled = false
                    binding.textNot.setText(cursor.getString(notIX))
                    binding.textNot.hint = ""

                    binding.buttonKaydet.visibility = View.INVISIBLE
                }
                cursor.close()
                db.close()

            }catch (e:Exception){
                e.printStackTrace()
                toastGoster("Hata Oluştu")
            }

        }
        else {
            registerLauncher()
        }
    }

    private fun registerLauncher(){
        // aktiviteyi gerçekleştir
        activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){result->
            if (result.resultCode == RESULT_OK){
                if (result.data !=null && result.data!!.data !=null){
                    try {
                        bitmap = if (Build.VERSION.SDK_INT >= 28) {
                            val veri = ImageDecoder.createSource(this@Detay.contentResolver,result.data!!.data!!)
                            ImageDecoder.decodeBitmap(veri)
                        } else {
                            MediaStore.Images.Media.getBitmap(this@Detay.contentResolver,result.data!!.data)
                        }
                        val scaleFactor = 0.5f

                        bitmap = Bitmap.createScaledBitmap(bitmap,((bitmap.getWidth() * scaleFactor).toInt()),((bitmap.getHeight() * scaleFactor).toInt()),false)
                        isPicture = 1
                        binding.imageView2.setImageBitmap(bitmap)

                    }catch (e:Exception){
                        e.printStackTrace()
                        toastGoster("Hata Oluştu")
                    }
                }
            }
        }

        // izin iste
        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()){result ->
            if (result){
                galeriAc()
            }
            else{
                toastGoster("Resim İçin İzin Gereklidir.")
            }
        }
    }

    private fun toastGoster(mesaj: String){
        Toast.makeText(this,mesaj,Toast.LENGTH_SHORT).show()
    }

    fun kaydet(view : View){

        val isim = binding.textIsim.text.toString()
        if (isim.isBlank() || isim.isEmpty()){
            toastGoster("Lütfen İsim Giriniz")
            return
        }

        val not = binding.textNot.text.toString()


        if (isPicture==0){
            toastGoster("Lütfen Resim Seçiniz")
            return
        }

        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG,50,outputStream)
        val byteArray = outputStream.toByteArray()

        try {
            val db = this.openOrCreateDatabase("gunluk", MODE_PRIVATE,null)
            db.execSQL("CREATE TABLE IF NOT EXISTS gunlukler(id INTEGER PRIMARY KEY, isim VARCHAR, nott VARCHAR,resim BLOB)")
            val query = "INSERT INTO gunlukler(isim,nott,resim) VALUES (?, ?, ?)"
            val statment = db.compileStatement(query)
            statment.bindString(1,isim)
            statment.bindString(2,not)
            statment.bindBlob(3,byteArray)
            statment.execute()
            db.close()
        }catch (e:Exception){
            e.printStackTrace()
            toastGoster("Hata Oluştu")
        }
        val intent = Intent(this,MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)

    }

    private fun galeriAc(){
        val intentt = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        activityResultLauncher.launch(intentt)
    }
    
    fun sec (view: View){

        // Sürüm 33 Ve üstü ise farklı şekilde izin iste.
        val istenenIzin = if (Build.VERSION.SDK_INT>=33) {
            "android.permission.READ_MEDIA_IMAGES"
        } else {
            "android.permission.READ_EXTERNAL_STORAGE"
        }

        // Bu İzine Sahipmiyim ?
        if (ContextCompat.checkSelfPermission(this,istenenIzin) !=PackageManager.PERMISSION_GRANTED) {
            //Bu izine sahip değilsem ve izin isteme sebebini göstermem gerekiyor mu. Gerekiyorsa Snackbar ile göster
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,istenenIzin)){
                //Rationale
                Snackbar.make(view,"Resim İçin İzin Gereklidir",Snackbar.LENGTH_INDEFINITE).setAction("İzin Ver"
                ) {
                    permissionLauncher.launch(istenenIzin)
                }.show()
            }
            else {
                // Sebep Göstermem Gerekmiyorsa Doğrudan İzin İste
                permissionLauncher.launch(istenenIzin)
            }
        }

        // İzine Sahipsem Doğrudan Galeriden Resim Seçme İşlemini Gerçekleştir
        else {
            galeriAc()
        }
    }
}