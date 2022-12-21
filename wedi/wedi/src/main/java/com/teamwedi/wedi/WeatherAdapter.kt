package com.teamwedi.wedi

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView


class WeatherAdapter (var items : Array<WeatherInfo>) : RecyclerView.Adapter<WeatherAdapter.ViewHolder>() {
    // 뷰 홀더 만들어서 반환, 뷰릐 레이아웃은 list_item_weather.xml
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WeatherAdapter.ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.list_item_weather, parent, false)
        return ViewHolder(itemView)
    }

    // 전달받은 위치의 아이템 연결
    override fun onBindViewHolder(holder: WeatherAdapter.ViewHolder, position: Int) {
        val item = items[position]
        holder.setItem(item)
    }

    // 아이템 갯수 리턴
    override fun getItemCount() = items.count()

    // 뷰 홀더 설정
    inner class ViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView) {
        fun setItem(item : WeatherInfo) {
            // 시간
            val Time = itemView.findViewById<TextView>(R.id.Item_TodayTime)
            // 강수 확률, 형태
            val Rain = itemView.findViewById<TextView>(R.id.Item_RainRate)
            // 온도/습도
            val TemperatureAndHumidity = itemView.findViewById<TextView>(R.id.Item_TemperatureAndHumidity)
            // 풍속
            val WindSpeed = itemView.findViewById<TextView>(R.id.Item_Windspeed)
            // 입을 옷
            val Cloth = itemView.findViewById<TextView>(R.id.Item_Cloth)

            Time.text = item.PredictTime
            Rain.text = item.RainRate + "/" + TranslateRainType(item.RainType)
            TemperatureAndHumidity.text = item.Temperature + "°C/" + item.Humidity + "%"
            WindSpeed.text = item.WindSpeed + "m/s"

            // 옷 추천 로직
            // 우선 순위: 기온 -> 습도
            //Cloth
            var str: String = ""
//            for (i in 0 .. MainActivity().Ary_Top.size - 1)
//            {
//                if (item.Temperature.toDouble() >= MainActivity().Ary_Top[i].Temperature_Relevance_Min && item.Temperature.toDouble() <= MainActivity().Ary_Top[i].Temperature_Relevance_Max)
//                {
//                    str += MainActivity().Ary_Top[i].Cloth_Name
//                    str += ", "
//                }
//            }
//            for (i in 0 .. MainActivity().Ary_Bottoms.size - 1)
//            {
//                if (item.Temperature.toDouble() >= MainActivity().Ary_Bottoms[i].Temperature_Relevance_Min && item.Temperature.toDouble() <= MainActivity().Ary_Bottoms[i].Temperature_Relevance_Max)
//                {
//                    str += MainActivity().Ary_Bottoms[i].Cloth_Name
//                    str += ", "
//                }
//            }
//            for (i in 0 .. MainActivity().Ary_Jacket.size - 1)
//            {
//                if (item.Temperature.toDouble() >= MainActivity().Ary_Jacket[i].Temperature_Relevance_Min && item.Temperature.toDouble() <= MainActivity().Ary_Jacket[i].Temperature_Relevance_Max)
//                {
//                    str += MainActivity().Ary_Jacket[i].Cloth_Name
//                    if (i != MainActivity().Ary_Jacket.size - 1)
//                        str += ", "
//                }
//            }
            when(item.Temperature.toDouble())
            {
                in -99.0 .. -30.0 -> str = "아무리 껴입어도 얼어 죽습니다."
                in -29.0 .. -20.0 -> str = "외관을 포기하세요."
                in -19.0 .. -10.0 -> str = "많이 춥습니다."
                in -9.0 .. -0.0 -> str = "춥습니다."
                in 1.0 .. 10.0 -> str = "찬바람이 붑니다."
                in 11.0 .. 20.0 -> str = "날씨가 좋습니다."
                in 21.0 .. 30.0 -> str = "더워질 기미가 보입니다.조심하세요."
                in 31.0 .. 40.0 -> str = "진정한 여름의 시작입니다."
                in 41.0 .. 50.0 -> str = "다 벗고 나가도 더워 죽습니다."
            }
            Cloth.text = str

        }
    }

    // RainType
    fun TranslateRainType(rainType : String) : String {
        when(rainType) {
            "0" -> return "맑음"
            "1" -> return "비"
            "2" -> return "비/눈"
            "3" -> return "눈"
            else -> return "강수 형태 오류"
        }
    }
}