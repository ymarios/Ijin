package com.adl.ijin

import android.app.Activity
import android.app.DatePickerDialog
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.DatePicker
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.adl.ijin.model.GetIjinResponse
import com.adl.ijin.model.ResponsePostData
import com.adl.ijin.service.RetrofitConfig
import com.github.drjacky.imagepicker.ImagePicker
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    lateinit var photoURI: Uri




    private val cameraLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                val uri = it.data?.data!!

                photo.setImageURI(uri)
                photoURI = uri
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupUI()
        Toast.makeText(this,"testes",Toast.LENGTH_LONG)show()
        RetrofitConfig().getIjin().getAllIjin().enqueue(object: Callback<GetIjinResponse> {
            override fun onResponse(
                call: Call<GetIjinResponse>,
                response: Response<GetIjinResponse>
            ) {
                Log.d("Response",response.body().toString())
            }

            override fun onFailure(call: Call<GetIjinResponse>, t: Throwable) {
                Log.e("error request",t.localizedMessage.toString())
            }

        })
    }

    var calendar = Calendar.getInstance()
    private fun setupUI() {

        btnDari.setOnClickListener({
            DatePickerDialog(this@MainActivity,getCalendarListener(0),calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),calendar.get(Calendar.DAY_OF_MONTH)).show()

        })

        btnSampai.setOnClickListener({
            DatePickerDialog(this@MainActivity,getCalendarListener(1),calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),calendar.get(Calendar.DAY_OF_MONTH)).show()

        })

        btnCamera.setOnClickListener({
            cameraLauncher.launch(
                ImagePicker.with(this)
                    .crop()
                    .cameraOnly()
                    .maxResultSize(480, 800, true)
                    .createIntent()
            )

        })

//        btnKirim.setOnClickListener({
//            RetrofitConfig().getIjin().addDataForm(spnKategori.selectedItem.toString(),txtDariTanggal.text.toString(),
//                txtSampaiTanggal.text.toString(),txtPerihal.text.toString(),txtKeterangan.text.toString() ).enqueue(
//                object:Callback<ResponsePostData>{
//                    override fun onResponse(
//                        call: Call<ResponsePostData>,
//                        response: Response<ResponsePostData>
//                    ) {
//                        Toast.makeText(this@MainActivity,(response.body() as ResponsePostData).message,
//                            Toast.LENGTH_LONG).show()
//                    }
//
//                    override fun onFailure(call: Call<ResponsePostData>, t: Throwable) {
//                        Log.e("error post data",t.localizedMessage.toString())
//                    }
//
//                }
//            )
//        })


        btnKirim.setOnClickListener({
            RetrofitConfig().getIjin().addDataAndImage(createRB(spnKategori.selectedItem.toString()),createRB(txtDariTanggal.text.toString()),
                createRB(txtSampaiTanggal.text.toString()),createRB(txtPerihal.text.toString()),createRB(txtKeterangan.text.toString()),
                uploadImage(photoURI,"lampiran")

            ).enqueue(
                object:Callback<ResponsePostData>{
                    override fun onResponse(
                        call: Call<ResponsePostData>,
                        response: Response<ResponsePostData>
                    ) {
                        Toast.makeText(this@MainActivity,(response.body() as ResponsePostData).message,Toast.LENGTH_LONG).show()
                    }

                    override fun onFailure(call: Call<ResponsePostData>, t: Throwable) {
                        Log.e("error post data",t.localizedMessage.toString())
                    }

                }
            )
        })

    }

    fun getCalendarListener (tipe:Int):DatePickerDialog.OnDateSetListener {
        var calendar = Calendar.getInstance()
        var dateSetListener = object : DatePickerDialog.OnDateSetListener {
            override fun onDateSet(p0: DatePicker?, p1: Int, p2: Int, p3: Int) {
                calendar.set(Calendar.YEAR, p1)
                calendar.set(Calendar.MONTH, p2)
                calendar.set(Calendar.DAY_OF_MONTH, p3)

                val formatDate = "MM/dd/yyyy"
                val sdf = SimpleDateFormat(formatDate, Locale.US)

                if(tipe==0){
                    txtDariTanggal.setText(sdf.format(calendar.time))

                }else{
                    txtSampaiTanggal.setText(sdf.format(calendar.time))

                }
            }

        }

        return dateSetListener

    }

    fun createRB(data:String):RequestBody{
        return RequestBody.create(MultipartBody.FORM,data)
    }

    fun uploadImage(uri:Uri,param:String): MultipartBody.Part {
        val file: File = File(uri.path)
        val rb:RequestBody =  file.asRequestBody("image/jpeg".toMediaTypeOrNull())

        return MultipartBody.Part.createFormData(param,file.name,rb)

    }
}