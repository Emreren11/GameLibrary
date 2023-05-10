package com.emre.mygamelibrary

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.emre.mygamelibrary.databinding.ActivityMainBinding
import com.emre.mygamelibrary.databinding.RecyclerRowBinding

class Adapter(val gameList: ArrayList<Game>): RecyclerView.Adapter<Adapter.GameHolder>() {

    class GameHolder(val binding: RecyclerRowBinding): RecyclerView.ViewHolder(binding.root) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GameHolder {
        // RecyclerView içerisine koyulan her öğe için görünümün nasıl olacağını belirler
        val binding = RecyclerRowBinding.inflate(LayoutInflater.from(parent.context),parent, false)
        return GameHolder(binding)
    }

    override fun getItemCount(): Int {
        // Toplam öğe sayısı kadar dönmelidir
        return gameList.size
    }

    override fun onBindViewHolder(holder: GameHolder, position: Int) {
        // İçeriği değiştirmek ve etkileşime girildiğinde yapılacakların işlendiği kod bloğu
        // holder -> güncelleme yapılmasına olanak sağlar (ViewBinding gibi)
        holder.binding.recyclerTextView.text = gameList.get(position).name
        holder.itemView.setOnClickListener {// Tıklanma işleminden sonra çalışacak kod bloğu
            // itemView -> RecyclerView içerisindeki her bir öğe anlamına gelmektedir. Tıklanma işlemi tüm öğeler için olacak
            val intentToDetailGame = Intent(holder.itemView.context, DetailGame::class.java)
            intentToDetailGame.putExtra("info", "old")
            intentToDetailGame.putExtra("id",gameList.get(position).id)
            holder.itemView.context.startActivity(intentToDetailGame)

        }
    }
}