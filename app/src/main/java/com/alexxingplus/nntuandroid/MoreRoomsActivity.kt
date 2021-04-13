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
    class room (name: String, number: String){
        public val name: String
        public val number: String
        init {
            this.number = number
            this.name = name
        }
    }

    val firstBuilding = ArrayList<room>()
    val secondBuilding = ArrayList<room>()
    val thirdBuilding = ArrayList<room>()
    val fourthBuilding = ArrayList<room>()
    val fifthBuilding = ArrayList<room>()
    val sixthBuilding = ArrayList<room>()

    fun updateBuilding(input: Int): ArrayList<MoreRoomsActivity.room>{
        var allBuildings = arrayOf(firstBuilding, secondBuilding, thirdBuilding, fourthBuilding, fifthBuilding, sixthBuilding)
        var tempBuilding = allBuildings[input - 1]
        return tempBuilding
    }

    fun ArrayList<room>.addRoom (name: String, number: String){
        this.add(room(name, number))
    }

    fun search(input: String, data: ArrayList<room>): ArrayList<room>{
        val output = ArrayList<room>()
        for (i in (0..data.count()-1)){
            if (data[i].name.toUpperCase().contains(input.toUpperCase())){
                output.add(data[i])
            } else if (data[i].number.toUpperCase().contains(input.toUpperCase())){
                output.add(data[i])
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
                adapter.mData = search(searchField.text.toString(), updateBuilding(building))
            } else {
                adapter.mData = updateBuilding(building)
            }
            adapter.notifyDataSetChanged()
            buildingLabel.text = building.toString()
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
                    adapter.mData = search(searchField.text.toString(), updateBuilding(building))
                } else {
                    adapter.mData = updateBuilding(building)
                }
                adapter.notifyDataSetChanged()
            }
        }

        searchButton.setOnClickListener {
            
        }

        searchField.addTextChangedListener(watcher)




        Log.d("номер", building.toString())

        firstBuilding.addRoom("Скоро тут будут аудитории", "1101")

        secondBuilding.addRoom("Директор библиотеки", "2306")
        secondBuilding.addRoom("Женский туалет", "2206")
        secondBuilding.addRoom("Зал электронных ресурсов", "2210")
        secondBuilding.addRoom("Информационно-библиографический отдел. Каталоги.", "2209")
        secondBuilding.addRoom("Компьютерный класс АГПМиСМ", "2102")
        secondBuilding.addRoom("Мужской туалет", "2105")
        secondBuilding.addRoom("Научно-методический отдел", "2301")
        secondBuilding.addRoom("Отдел автоматизации библиотечных ресурсов", "2305")
        secondBuilding.addRoom("Отдел комплектования", "2304")
        secondBuilding.addRoom("Отдел комплектования НТБ и зал технической обработки", "2107")
        secondBuilding.addRoom("Отдел обслуживания научной литературой. Читальный зал научной литературы", "2303")
        secondBuilding.addRoom("Отдел связи ИВЦ", "2101")
        secondBuilding.addRoom("Отдел учета и библиотечной обработки документов", "2201")
        secondBuilding.addRoom("Отдел учета и библиотечной обработки документов (второе помещение)", "2211")
        secondBuilding.addRoom("Президентская программа подготовки управленческих кадров (специальность \"Менеджмент\"); Администрация", "2205")
        secondBuilding.addRoom("Преподавательская кафедра АГПМиСМ", "2108")
        secondBuilding.addRoom("Преподавательская кафедра ТиОМ", "2308")
        secondBuilding.addRoom("Сектор анализа и прогнозирования деятельности НТБ", "2207")
        secondBuilding.addRoom("Учебная лаборатория \"Теория механизмов и прикладная механика\"", "2309")
        secondBuilding.addRoom("Учебная лаборатория АГПМиСМ", "2102")
        secondBuilding.addRoom("Учебная лаборатория дизельных двигателей", "2104")
        secondBuilding.addRoom("Учебная лаборатория метрологии, стандартизации, сертификации кафедры \"Машиностроительные технологические комплексы\"", "2207")
        secondBuilding.addRoom("Учебная лаборатория метрологии, стандартизации, сертификации кафедры \"Машиностроительные технологические комплексы\"", "2109")
        secondBuilding.addRoom("Учебная лаборатория нанотехнологий и машиностроения", "2103")
        secondBuilding.addRoom("Учебная лаборатория технологической оснастки", "2307")
        secondBuilding.addRoom("Ученая лаборатория гидро и пневмосистем", "2106")
        secondBuilding.addRoom("Хозяйственный отдел НТБ", "2204")
        secondBuilding.addRoom("Читальный зал", "2202")
        secondBuilding.addRoom("Электрощитовая ОГЭ", "2105")

        thirdBuilding.addRoom("Скоро тут будут аудитории", "1221")
        fourthBuilding.addRoom("Скоро тут будут аудитории", "1221")
        firstBuilding.addRoom("Скоро тут будут аудитории", "1221")

        sixthBuilding.addRoom("Библиотека: абонемент для студентов младших курсов", "6116")
        sixthBuilding.addRoom("Библиотека: абонемент для студентов старших курсов", "6270")
        sixthBuilding.addRoom("Архивохранилище (1 этаж)", "6100")
        sixthBuilding.addRoom("Архивохранилище (2 этаж)", "6234")
        sixthBuilding.addRoom("Дирекция ИНЭЛ", "6210")
        sixthBuilding.addRoom("Дирекция ИРИТ", "6211")
        sixthBuilding.addRoom("Дирекция ИТС", "6206")
        sixthBuilding.addRoom("Дирекция ИТС (второе помещение)", "6209")
        sixthBuilding.addRoom("Дом научной коллаборации (ДНК)", "6022")
        sixthBuilding.addRoom("Зал электронных ресурсов", "6119")
        sixthBuilding.addRoom("Заочное отделение кафедры «Связи с общественностью, маркетинга и коммуникации»", "6301")
        sixthBuilding.addRoom("Кабинет ведущего инженера службы главного инженера", "6226")
        sixthBuilding.addRoom("Кабинет группы сопровождения учебного процесса", "6344")
        sixthBuilding.addRoom("Кабинет директора ИВЦ, начальника управления информатизации", "6337")
        sixthBuilding.addRoom("Кабинет зав. кафедрой «Графические информационные системы»", "6446")
        sixthBuilding.addRoom("Кабинет зав. кафедрой «Инженерная графика»", "6551")
        sixthBuilding.addRoom("Кабинет зав. кафедрой «Общая и ядерная физика»", "6248")
        sixthBuilding.addRoom("Кабинет зав. кафедрой «Проектирование и эксплуатация газонефтепроводов и газонефтехранилищ", "6458")
        sixthBuilding.addRoom("Кабинет зав. кафедрой «Производственная безопасность, экология и химия»", "6262")
        sixthBuilding.addRoom("Кабинет зав. кафедрой \"Высшая математика\"", "6202")
        sixthBuilding.addRoom("Кабинет зав. лабораторией БЖД", "6348")
        sixthBuilding.addRoom("Кабинет заместителя начальника отдела обеспечения безопасности", "6203")
        sixthBuilding.addRoom("Кабинет коменданта 6 корпуса", "6223")
        sixthBuilding.addRoom("Кабинет начальника 2 отдела", "6207")
        sixthBuilding.addRoom("Кабинет помощника ректора по 6 корпусу", "6212")
        sixthBuilding.addRoom("Кабинет помощника ректора по безопасности", "6216")
        sixthBuilding.addRoom("Кабинет сотрудников архива", "6230")
        sixthBuilding.addRoom("Кафедра «Графические информационные системы»", "6451")
        sixthBuilding.addRoom("Компьютерный класс БЖД", "6346")
        sixthBuilding.addRoom("Лаборатория «Оптика»", "6257")
        sixthBuilding.addRoom("Лаборатория «Физико-химических методов анализа»", "6260")
        sixthBuilding.addRoom("Лаборатория ИНЭУ", "6215")
        sixthBuilding.addRoom("Лаборатория кафедры «Электроэнергетика, электроснабжение и силовая электроника»", "6440")
        sixthBuilding.addRoom("Лаборатория социологических исследований", "6303")
        sixthBuilding.addRoom("Лаборатория эксплуатации надежности метариалов ", "6353")
        sixthBuilding.addRoom("Лабораторный зал", "6264")
        sixthBuilding.addRoom("Лабораторный класс", "6261")
        sixthBuilding.addRoom("Медицинский кабинет", "6103")
        sixthBuilding.addRoom("Методический кабинет кафедры «Иностранные языки»", "6405")
        sixthBuilding.addRoom("Методический кабинет кафедры «Прикладная математика»", "6227")
        sixthBuilding.addRoom("Научно-исследовательская экоаналитическая лаборатория", "6355")
        sixthBuilding.addRoom("Научно-техническая библиотека", "6199")
        sixthBuilding.addRoom("Отдел по работе со студентами", "6225")
        sixthBuilding.addRoom("Помещение воинского учета студентов и работников НГТУ", "6233")
        sixthBuilding.addRoom("Помещение кафедра «Проектирование и эксплуатация газонефтепроводов и газонефтехранилищ»", "6459")
        sixthBuilding.addRoom("Помещение кафедры «Инженерная графика»", "6559")
        sixthBuilding.addRoom("Помещение кафедры «Иностранные языки»", "6404")
        sixthBuilding.addRoom("Помещение кафедры «Методология, история и философия науки»", "6402")
        sixthBuilding.addRoom("Помещение кафедры «Общая и ядерная физика»", "6247")
        sixthBuilding.addRoom("Помещение кафедры «Связи с общественностью, маркетинга и коммуникации»", "6300")
        sixthBuilding.addRoom("Помещение кафедры \"Высшая математика\"", "6201")
        sixthBuilding.addRoom("Помещение кафедры \"Физическое воспитание\"", "6020")
        sixthBuilding.addRoom("Помещение преподавателей дисциплины БЖД", "6352")
        sixthBuilding.addRoom("Помещение преподавателей дисциплины экология", "6349")
        sixthBuilding.addRoom("Помещение ШОПОМ", "6208")
        sixthBuilding.addRoom("Помещения Центра системных технологий открытого образования", "6543")
        sixthBuilding.addRoom("Помощник ректора", "6220")
        sixthBuilding.addRoom("Приемная ректора и проректоров в 6 учебном корпусе", "6218")
        sixthBuilding.addRoom("Профсоюзный комитет студентов НГТУ", "6315")
        sixthBuilding.addRoom("Региональный центр просветительства, культурного и исторического наследия", "6401")
        sixthBuilding.addRoom("Спортивный комплекс", "6111")
        sixthBuilding.addRoom("Столовая", "6114")
        sixthBuilding.addRoom("Учебная аудитория «Центра обучения иностранных студентов»", "6462")
        sixthBuilding.addRoom("Учебная часть ИНЭУ", "6205")
        sixthBuilding.addRoom("Учебный отдел", "6221")
        sixthBuilding.addRoom("Читальный зал", "6162")



        var allBuildings = arrayOf(firstBuilding, secondBuilding, thirdBuilding, fourthBuilding, fifthBuilding, sixthBuilding)

        fun getRoomsWeNeed() : ArrayList<room>{
            return allBuildings[building-1]
        }

        roomList.adapter = RoomsAdapter(context = this, building = building)
        blockButtons()
    }



    private class RoomsAdapter (context: Context, building: Int): BaseAdapter() {

        private val mContext : Context
        public var mData : ArrayList<room>
        private var building: Int
        var word : String

        init {
            mContext = context
            mData = ArrayList<room>()
            this.building = building
            word = ""
        }



        override fun getCount(): Int {
            return mData.count()
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

            room_cell= inflater.inflate(R.layout.room_cell, parent, false)
            val roomName = room_cell.findViewById<TextView>(R.id.roomName)
            val roomCard = room_cell.findViewById<CardView>(R.id.roomCard)


            roomName.text = mData[position].name

            roomCard.setOnClickListener {
                val roomIntent = Intent(mContext, MainActivity::class.java)
                roomIntent.putExtra("room", mData[position].number)
                this.mContext.startActivity(roomIntent)
            }

//            if (position == 0){
//                room_cell = inflater.inflate(R.layout.building_switcher_cell, parent, false)
//                val nextButton : ImageButton = room_cell.findViewById(R.id.nextBuildButton)
//                val prevButton : ImageButton = room_cell.findViewById(R.id.prevBuildButton)
//                val buildingLabel : TextView = room_cell.findViewById(R.id.buildingLabel)
//                val searchRoomField : EditText = room_cell.findViewById(R.id.searchRoomField)
//                val searchButton : ImageButton = room_cell.findViewById(R.id.RoomSearchButton)
//
//                searchRoomField.setText(word)
//
//                nextButton.setOnClickListener {
//                    if (building < 6){
//                        building += 1
//                        nextButton.isClickable = true
//                        nextButton.alpha = 1F
//                    } else {
//                        building = 6
//                        nextButton.isClickable = false
//                        nextButton.alpha = 0.4F
//                    }
//                    word = searchRoomField.text.toString()
//                    mData = updateBuilding(building)
//                    this.notifyDataSetChanged()
//                }
//
//                prevButton.setOnClickListener {
//                    if (building > 1){
//                        building -= 1
//                        prevButton.isClickable = true
//                        prevButton.alpha = 1F
//                    } else {
//                        building = 1
//                        prevButton.isClickable = false
//                        prevButton.alpha = 0.4F
//                    }
//                    word = searchRoomField.text.toString()
//                    mData = updateBuilding(building)
//                    this.notifyDataSetChanged()
//                }
//
//                searchButton.setOnClickListener {
//                    word = searchRoomField.text.toString()
//                    if (word == ""){
//                        mData = updateBuilding(building)
//                        this.notifyDataSetChanged()
//                    } else {
//                        mData = search(word, updateBuilding(building))
//                        this.notifyDataSetChanged()
//                    }
//                }
//
//                buildingLabel.text = building.toString()
//
//            } else {
//
//            }



            return room_cell!!
        }

    }

}