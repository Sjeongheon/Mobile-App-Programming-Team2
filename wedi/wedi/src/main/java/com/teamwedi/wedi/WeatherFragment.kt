package com.teamwedi.wedi

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.teamwedi.wedi.databinding.FragmentWeatherBinding
import retrofit2.Call
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*


class WeatherFragment : Fragment() {
    lateinit var binding : FragmentWeatherBinding
    lateinit var weatherRecyclerView : RecyclerView

//    // 옷 리스트
//    // 바지
//    var Bottoms1 = Cloth("츄리닝 바지", -5.0, 15.0, 20, SeasonCategory.Spring)
//    var Bottoms2 = Cloth("두꺼운 청바지", -10.0, 5.0, 60, SeasonCategory.Winter)
//    var Bottoms3 = Cloth("기모 바지", -20.0, 0.0, 50, SeasonCategory.Winter)
//    var Bottoms4 = Cloth("정장 바지", -5.0, 15.0, 40, SeasonCategory.Winter)
//
//    public var Ary_Bottoms: Array<Cloth> = arrayOf(Bottoms1, Bottoms2, Bottoms3, Bottoms4)
//
//    // 상의
//    var Top1 = Cloth("셔츠", 15.0, 28.0, 20, SeasonCategory.Spring)
//    var Top2 = Cloth("티셔츠", 23.0, 40.0, 20, SeasonCategory.Summer)
//    var Top3 = Cloth("기모 후드티", -15.0, 0.0, 40, SeasonCategory.Spring)
//    var Top4 = Cloth("니트", -5.0, 10.0, 30, SeasonCategory.Fall)
//    var Top5 = Cloth("맨투맨", -15.0, 15.0, 40, SeasonCategory.Winter)
//
//    public var Ary_Top: Array<Cloth> = arrayOf(Top1, Top2, Top3, Top4, Top5)
//
//    // 잠바
//    val Jacket1 = Cloth("오리털 패딩", -20.0, 5.0, 30, SeasonCategory.Winter)
//    val Jacket2 = Cloth("롱패딩", -20.0, 0.0, 80, SeasonCategory.Winter)
//    val Jacket3 = Cloth("코트", -5.0, 15.0, 20, SeasonCategory.Fall)
//    val Jacket4 = Cloth("바람막이", 0.0, 20.0, 10, SeasonCategory.Spring)
//
//    public var Ary_Jacket: Array<Cloth> = arrayOf(Jacket1, Jacket2, Jacket3, Jacket4)


    private var base_date = "20221220"  // 발표 일자
    private var base_time = "2200"      // 발표 시각
    // 위치.
    private var nx = "37"               // 위도
    private var ny = "128"              // 경도

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentWeatherBinding.inflate(inflater, container, false);
        val TimeDate = binding.TimeDate                             // 오늘 날짜 텍스트뷰
        weatherRecyclerView = binding.weatherRecyclerView           // 날씨 리사이클러 뷰
        val btnRefresh = binding.btnRefresh                     // 새로고침 버튼

        // 리사이클러 뷰 매니저 설정
        weatherRecyclerView.layoutManager = LinearLayoutManager(container?.context)

        // 오늘 날짜 텍스트뷰 설정
        TimeDate.text = SimpleDateFormat("MM월 dd일", Locale.getDefault()).format(Calendar.getInstance().time) + " 날씨"

        // nx, ny지점의 날씨 가져와서 설정하기
        setWeather(nx, ny)

        // <새로고침> 버튼 누를 때 날씨 정보 다시 가져오기
        btnRefresh.setOnClickListener {
            setWeather(nx, ny)
        }
        return binding.root
    }

    // 날씨 가져와서 설정하기
    private fun setWeather(nx : String, ny : String) {
        // 준비 단계 : base_date(발표 일자), base_time(발표 시각)
        // 현재 날짜, 시간 정보 가져오기
        val cal = Calendar.getInstance()
        base_date = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(cal.time) // 현재 날짜
        val Current_time_M = SimpleDateFormat("HH", Locale.getDefault()).format(cal.time) // 현재 시각
        val Current_time_S = SimpleDateFormat("HH", Locale.getDefault()).format(cal.time) // 현재 분
        // API 가져오기 적당하게 변환
        base_time = getBaseTime(Current_time_M, Current_time_S)

        // 날씨 정보 가져오기
        // (한 페이지 결과 수 = 60, 페이지 번호 = 1, 응답 자료 형식-"JSON", 발표 날짜, 발표 시각, 예보지점 좌표)
        val call = ApiObject.retrofitService.GetWeather(60, 1, "JSON", base_date, base_time, nx, ny)

        // 비동기적으로 실행하기
        call.enqueue(object : retrofit2.Callback<WEATHER> {
            // 응답 성공 시
            override fun onResponse(call: Call<WEATHER>, response: Response<WEATHER>) {
                if (response.isSuccessful) {
                    // 날씨 정보 가져오기
                    val it: List<ITEM> = response.body()!!.response.body.items.item


                    // 현재 시각부터 1시간 뒤의 날씨 6개를 담을 배열
                    val weatherArr = arrayOf(WeatherInfo(), WeatherInfo(), WeatherInfo(), WeatherInfo(), WeatherInfo(), WeatherInfo())

                    // 배열 채우기
                    var index = 0
                    val totalCount = response.body()!!.response.body.totalCount - 1
                    for (i in 0..totalCount) {
                        index %= 6
                        when(it[i].category) {
                            "RN1" -> weatherArr[index].RainRate = it[i].fcstValue
                            "PTY" -> weatherArr[index].RainType = it[i].fcstValue
                            "T1H" -> weatherArr[index].Temperature = it[i].fcstValue
                            "REH" -> weatherArr[index].Humidity = it[i].fcstValue
                            "WSD" -> weatherArr[index].WindSpeed = it[i].fcstValue
                            else -> continue
                        }
                        index++
                    }

                    // 각 날짜 배열 시간 설정
                    for (i in 0..5) weatherArr[i].PredictTime = it[i].fcstTime

                    // 리사이클러 뷰에 데이터 연결
                    weatherRecyclerView.adapter = WeatherAdapter(weatherArr)

                    // 토스트 띄우기
                    Toast.makeText(context, it[0].fcstDate + ", " + it[0].fcstTime + "의 날씨 정보입니다.", Toast.LENGTH_SHORT).show()
                }
            }

            // 응답 실패 시
            override fun onFailure(call: Call<WEATHER>, t: Throwable) {
                val tvError = binding.tvError
                tvError.text = "api fail : " +  t.message.toString() + "\n 다시 시도해주세요."
                tvError.visibility = View.VISIBLE
                Log.d("api fail", t.message.toString())
            }
        })
    }

    // baseTime 설정하기
    private fun getBaseTime(h : String, m : String) : String {
        var result = ""

        // 날씨 데이터는 00분 30분 기준으로 기상청에 저장되기때문에 이런 로직이 필요.
        if (m.toInt() < 45) {
            // 0시면 2330
            if (h == "00") result = "2330"
            // 아니면 1시간 전 날씨 정보 부르기
            else {
                var resultH = h.toInt() - 1
                // 1자리면 0 붙여서 2자리로 만들기
                if (resultH < 10) result = "0" + resultH + "30"
                // 2자리면 그대로
                else result = resultH.toString() + "30"
            }
        }
        // 45분 이후면 바로 정보 받아오기
        else result = h + "30"

        return result
    }

}