package com.example.keep.adapter

import androidx.recyclerview.widget.GridLayoutManager
import com.example.keep.image.ImageAdapter
import kotlin.random.Random

class ImageGridAdapter(canEdit: Boolean, clickListener: OnClickListener) :
    ImageAdapter(canEdit, clickListener) {


    val variableSpanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {

        private var indexSpanCounts: List<Int> = generateSpanCountForItems(itemCount)

        override fun getSpanSize(position: Int): Int {
            return indexSpanCounts[position]
        }

        private fun generateSpanCountForItems(count: Int): List<Int> {
            val list = mutableListOf<Int>()


            var rowSpansOccupied = 0

            if(count==1){
                list.add(4)
            }else {
                repeat(count) {
                    var size = 0
                    do {
                         size = Random.nextInt(2, 4 +1 - rowSpansOccupied)
                    }while (size==3)
                    rowSpansOccupied += size
                    if (rowSpansOccupied >= 4) rowSpansOccupied = 0
                    list.add(size)
                }
            }
            return list
        }

        override fun invalidateSpanIndexCache() {
            super.invalidateSpanIndexCache()
            indexSpanCounts = generateSpanCountForItems(itemCount)
        }
    }
}