package com.alexxingplus.nntuandroid

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.cardview.widget.CardView


class MoreRoomsActivity : AppCompatActivity() {

    fun updateBuilding(input: Int): HashMap<String, String>{
        val tempBuilding = allBuildings[input - 1]
        return tempBuilding
    }

//    fun search(input: String, data: ArrayList<room>): ArrayList<room>{
//        val output = ArrayList<room>()
//        for (i in (0..data.count()-1)){
//            if (data[i].name.toUpperCase().contains(input.toUpperCase())){
//                output.add(data[i])
//            } else if (data[i].number.toUpperCase().contains(input.toUpperCase())){
//                output.add(data[i])
//            }
//        }
//        return output
//    }

    fun search(input: String) : HashMap<String, String> {
        var output = HashMap<String, String>()
        val searchWord = input.toUpperCase()
        for (building in allBuildings) {
            for (key in building.keys){
                val keyWord = key.toUpperCase()
                val keyData = building[key]!!.toUpperCase()
                if (keyWord.contains(searchWord)){
                    output[key] = building[key]!!
                } else if (keyData.contains(searchWord)){
                    output[key] = building[key]!!
                }
            }
        }
        return output
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        setContentView(R.layout.activity_more_rooms)
        super.onCreate(savedInstanceState)

        val roomList : ListView = findViewById(R.id.roomsList)
        val searchButton : ImageButton = findViewById(R.id.RoomSearchButton)
        val nextButton : ImageButton = findViewById(R.id.nextBuildButton)
        val prevButton : ImageButton = findViewById(R.id.prevBuildButton)
        val searchField : EditText = findViewById(R.id.searchRoomField)
        val buildingLabel : TextView = findViewById(R.id.buildingLabel)
        var building = this.intent.getIntExtra("nowBuilding", 6)


        fun blockButtons(){
            if (building == 6){
                nextButton.isClickable = false
                nextButton.alpha = 0.4F
            } else {
                nextButton.isClickable = true
                nextButton.alpha = 1F
            }
            if (building == 1){
                prevButton.isClickable = false
                prevButton.alpha = 0.4F
            } else {
                prevButton.isClickable = true
                prevButton.alpha = 1F
            }
            val adapter = roomList.adapter as RoomsAdapter
            if (searchField.text.toString() != ""){
                adapter.mData = search(searchField.text.toString())
                buildingLabel.text = "🔎"
            } else {
                adapter.mData = updateBuilding(building)
                buildingLabel.text = building.toString()
            }
            adapter.notifyDataSetChanged()
        }

        nextButton.setOnClickListener {
            if (building < 6){
                building += 1
            } else {building = 6}
            blockButtons()
        }

        prevButton.setOnClickListener {
            if (building > 1){
                building -= 1
            } else {building = 1}
            blockButtons()
        }

        val watcher = object : TextWatcher{
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val adapter = roomList.adapter as RoomsAdapter
                if (searchField.text.toString() != ""){
                    adapter.mData = search(searchField.text.toString())
                    buildingLabel.text = "🔎"
                } else {
                    adapter.mData = updateBuilding(building)
                    buildingLabel.text = building.toString()
                }
                adapter.notifyDataSetChanged()
            }
        }

        searchButton.setOnClickListener {
            val adapter = roomList.adapter as RoomsAdapter
            if (searchField.text.toString() != ""){
                adapter.mData = search(searchField.text.toString())
                buildingLabel.text = "🔎"
            } else {
                adapter.mData = updateBuilding(building)
                buildingLabel.text = building.toString()
            }
            adapter.notifyDataSetChanged()
        }


        searchField.addTextChangedListener(watcher)

        Log.d("номер", building.toString())


        fun getRoomsWeNeed() : HashMap<String, String>{
            return allBuildings[building-1]
        }

        roomList.adapter = RoomsAdapter(context = this, building = building)
        blockButtons()
    }



    private class RoomsAdapter (context: Context, building: Int): BaseAdapter() {

        private val mContext : Context
        public var mData : HashMap<String, String>
        var keys:  List<String>
        private var building: Int

        init {
            mContext = context
            mData = HashMap<String, String>()
            keys = mData.keys.sorted()
            this.building = building
        }

        override fun notifyDataSetChanged() {
            super.notifyDataSetChanged()
            keys = mData.keys.sorted()
        }

        override fun getCount(): Int {
            return keys.count()
        }

        override fun getItem(position: Int): Any {
            return "Room String"
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val inflater = LayoutInflater.from(mContext)

            var room_cell : View

            room_cell = inflater.inflate(R.layout.room_cell, parent, false)
            val roomName = room_cell.findViewById<TextView>(R.id.roomName)
            val roomCard = room_cell.findViewById<CardView>(R.id.roomCard)


            roomName.text = keys[position]

            roomCard.setOnClickListener {
                val roomIntent = Intent(mContext, MainActivity::class.java)
                roomIntent.putExtra("room", mData[keys[position]]!!)
                this.mContext.startActivity(roomIntent)
            }

            return room_cell!!
        }

    }

}