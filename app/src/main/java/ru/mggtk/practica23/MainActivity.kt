package ru.mggtk.practica23

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class MainActivity : AppCompatActivity(), CoroutineScope {
    private var job:Job=Job()
    override val coroutineContext :CoroutineContext
        get()=Dispatchers.Default+job

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }


    fun Int.to2ByteArray() : ByteArray = byteArrayOf(toByte(), shr(8).toByte())

    suspend fun crc16(byteArray: ByteArray) : ByteArray {
        var crc = 0xffff
        byteArray.forEach {byte ->
            crc = (crc ushr 8 or crc shl 8) and 0xffff
            crc = crc xor (byte.toInt() and 0xff)
            crc = crc xor ((crc and 0xff) shr 4)
            crc = crc xor ((crc shl 12) and 0xffff)
            crc = crc xor (((crc and 0xff) shl 5) and 0xffff)
        }
        crc = crc and 0xffff
        return crc.to2ByteArray()
    }
    fun ByteArray.toHex(): String = joinToString("") { eachByte -> "%02x".format(eachByte) }

    suspend fun calculate(a:Int):String{
        var b :ByteArray=a.to2ByteArray()
        var res:ByteArray = crc16(b)
        return res.toHex()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val button:Button=findViewById(R.id.button)


        button.setOnClickListener {
            launch {
                val progressBar=findViewById<ProgressBar>(R.id.progressBar)
                progressBar.post {
                    progressBar.visibility= View.VISIBLE
                }
                calculateByte()
                progressBar.post {
                    progressBar.visibility= View.INVISIBLE
                }

            }
        }
    }
    suspend fun calculateByte(){
        var result = GlobalScope.async {
        val editText:EditText=findViewById(R.id.editTextPhone)
            var a:Int= editText.text.toString().toInt()
            calculate(a)
        }
        GlobalScope.launch {
            val textResult:TextView=findViewById(R.id.textView2)
            textResult.text="result: ${result.await()}"
        }
        result.join()
        }
    }



