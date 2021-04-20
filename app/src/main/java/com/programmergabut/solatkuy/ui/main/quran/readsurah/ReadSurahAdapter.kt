package com.programmergabut.solatkuy.ui.main.quran.readsurah

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.programmergabut.solatkuy.R
import com.programmergabut.solatkuy.data.local.localentity.MsAyah
import com.programmergabut.solatkuy.data.remote.json.readsurahJsonAr.Ayah
import com.programmergabut.solatkuy.databinding.ListReadSurahBinding

class ReadSurahAdapter(
    //val onClickFavAyah: (MsAyah, ListReadSurahBinding) -> Unit,
    val setTheme: (ListReadSurahBinding) -> Unit,
    val isFav: Drawable,
    val isNotFav: Drawable,
    val accentColor: Int
) : RecyclerView.Adapter<ReadSurahAdapter.ReadSurahViewHolder>() {

    private val diffCallback = object: DiffUtil.ItemCallback<MsAyah>(){
        override fun areItemsTheSame(oldItem: MsAyah, newItem: MsAyah) = oldItem == newItem
        override fun areContentsTheSame(oldItem: MsAyah, newItem: MsAyah) = oldItem == newItem
    }

    private val differ = AsyncListDiffer(this, diffCallback)

    var listAyah : List<MsAyah>
        get() = differ.currentList
        set(value) = differ.submitList(value)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReadSurahViewHolder {
        val binding = DataBindingUtil.inflate<ListReadSurahBinding>(
            LayoutInflater.from(parent.context),
            R.layout.list_read_surah, parent, false
        )
        return ReadSurahViewHolder(binding)
    }

    override fun getItemCount(): Int = listAyah.size

    override fun onBindViewHolder(holder: ReadSurahViewHolder, position: Int) = holder.bind(listAyah[position])

    inner class ReadSurahViewHolder(private val binding: ListReadSurahBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(data: MsAyah){
            binding.tvListFavAr.text = data.text
            binding.tvListFavEn.text = data.textEn
            binding.tvListFavNum.text = data.numberInSurah.toString()
            setTheme(binding)
            if(data.isFav){
                binding.ivListFavFav.setImageDrawable(isFav)
            } else {
                binding.ivListFavFav.setImageDrawable(isNotFav)
            }
            if(data.isLastRead){
                binding.clVhReadSurah.setBackgroundColor(accentColor)
            }
            binding.ivListFavFav.setOnClickListener {
                //onClickFavAyah(data, binding)
            }
        }
    }

}