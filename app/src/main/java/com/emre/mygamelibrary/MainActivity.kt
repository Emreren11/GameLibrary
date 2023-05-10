package com.emre.mygamelibrary

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import com.emre.mygamelibrary.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var gameList: ArrayList<Game>
    private lateinit var gamesAdapter: Adapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        gameList = ArrayList<Game>()
        gamesAdapter = Adapter(gameList)
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        // recyclerView'un düzenleyici (layoutManager) ile öğelerin nasıl yerleşeceği seçilir. LinearLayout olarak lineew liste olarak ayarlanır
        binding.recyclerView.adapter = gamesAdapter
        // recyclerView'un adaptörü seçilir

        try {

            val database = openOrCreateDatabase("Games", MODE_PRIVATE, null)
            val cursor = database.rawQuery("select * from games", null)
            val nameIndex = cursor.getColumnIndex("name")
            val idIndex = cursor.getColumnIndex("id")

            while (cursor.moveToNext()) {
                val name = cursor.getString(nameIndex)
                val id = cursor.getInt(idIndex)
                val game = Game(name, id)
                gameList.add(game)
            }
            gamesAdapter.notifyDataSetChanged() // Değişiklik olduğunda listeyi yenilemesi için kullanılır
            cursor.close()

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val menuInflater = menuInflater
        menuInflater.inflate(R.menu.add_menu,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.addGame) {
            val intent = Intent(this@MainActivity, DetailGame::class.java)
            intent.putExtra("info","new")
            startActivity(intent)
        }
        return super.onOptionsItemSelected(item)
    }
}