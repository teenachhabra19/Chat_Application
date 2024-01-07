package com.example.whatsappclone

import android.view.View
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.paging.FirestorePagingAdapter
import com.google.android.material.imageview.ShapeableImageView
import com.squareup.picasso.Picasso

class UserViewHolder(itemView:View):RecyclerView.ViewHolder(itemView) {


    fun bind(user:User,onClick:(name:String,photo:String,id:String)->Unit)=with(itemView){
       val countTv= itemView.findViewById<TextView>(R.id.countTv)
        countTv.isVisible=false
        val timeTv=itemView.findViewById<TextView>(R.id.timeTv)
        timeTv.isVisible=false
        val titleTv=itemView.findViewById<TextView>(R.id.titleTv)
        titleTv.text=user.name

        val subtitleTv=itemView.findViewById<TextView>(R.id.subtitleTv)
        subtitleTv.text=user.status
        val userImgView2=itemView.findViewById<ShapeableImageView>(R.id.userImgView2)
        Picasso.get().load(user.thumbImage).placeholder(R.drawable.defaultavatar).error(R.drawable.defaultavatar).into(userImgView2)
        setOnClickListener {
            onClick.invoke(user.name,user.thumbImage,user.uid)
        }
    }
}