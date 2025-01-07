package com.example.gradeanddetails

data class Grade(var code:String,var name:String,var score:String,var credits:String,val semester:String){
    var detail:String?=null
}
data class Data(val semesterId2studentGrades:Map<String,List<SingleSubject>>)
data class SingleSubject(val course:Course,val gaGrade:String,val gradeDetail:String)
data class Course(val code:String,val nameZh:String,val credits:Double)

data class Data2(val result:Int,val message:String?,val data:List<SingleSubject2>)
data class SingleSubject2(val courseCode:String,val gradeDetail: String)