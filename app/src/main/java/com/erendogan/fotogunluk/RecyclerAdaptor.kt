package com.erendogan.fotogunluk
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.erendogan.fotogunluk.databinding.RowBinding

class RecyclerAdaptor(private var gunlukList:ArrayList<GunlukModel>): RecyclerView.Adapter<RecyclerAdaptor.ViewHolder>() {

    class ViewHolder (val binding: RowBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = RowBinding.inflate(inflater,parent,false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return gunlukList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.textRow.text = gunlukList[position].name
        holder.binding.rowResim.setImageBitmap((gunlukList[position].bitmap))
        try {
            holder.itemView.setOnClickListener {
                val intent = Intent(holder.itemView.context, Detay::class.java)
                intent.putExtra("data", gunlukList[position].id)
                holder.itemView.context.startActivity(intent)
            }
        }
        catch (e:Exception){
            e.printStackTrace()
        }
    }
}