package com.erendogan.fotogunluk

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.erendogan.fotogunluk.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var gunlukList:ArrayList<GunlukModel>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        supportActionBar?.title = "Günlüğünüze Hoş Geldiniz"
        gunlukList = ArrayList()

        try {
            val db = this.openOrCreateDatabase("gunluk", MODE_PRIVATE,null)
            val cursor = db.rawQuery("SELECT * FROM gunlukler",null)
            val isimIX = cursor.getColumnIndex("isim")
            val idIX = cursor.getColumnIndex("id")
            val resimIX = cursor.getColumnIndex("resim")
            while (cursor.moveToNext()){
                val name = cursor.getString(isimIX)
                val id = cursor.getInt(idIX)
                val resim = cursor.getBlob(resimIX)
                gunlukList.add(GunlukModel(name,null,id,Bitmap.createBitmap(BitmapFactory.decodeByteArray(resim,0,resim.size))))
            }
            cursor.close()
            db.close()

        }catch (e:Exception){
            e.printStackTrace()
        }
        val recyclerView: RecyclerView = binding.recyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)

        val adaptor = RecyclerAdaptor(gunlukList)
        recyclerView.adapter = adaptor
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.add,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId==R.id.optionAdd){
            val intent = Intent(this@MainActivity,Detay::class.java)
            startActivity(intent)
        }
        return super.onOptionsItemSelected(item)
    }
}