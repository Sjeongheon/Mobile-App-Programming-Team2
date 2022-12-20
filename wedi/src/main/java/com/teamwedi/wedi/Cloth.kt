package com.example.t2

import android.graphics.Picture
import android.media.Image

enum class SeasonCategory{
    Spring, Summer, Fall, Winter
}

public class Cloth constructor(// 옷 이름
    var Cloth_Name: String,// 적정온도 최소수치
    var Temperature_Relevance_Min: Double,// 적정온도 최대수치
    var Temperature_Relevance_Max: Double,// 습도 가중치(통풍 잘되는지)
    var Humidity_Weight: Int,// 습도 가중치 관련 계산 및 계절 적절성
    var Season_Relevance: SeasonCategory
) {
    var Cloth_Image: Image? = null //옷 이미지

}