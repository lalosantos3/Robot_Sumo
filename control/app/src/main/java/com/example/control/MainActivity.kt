package com.example.control
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.PorterDuff
import android.util.Log
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.core.app.ActivityCompat
import java.io.IOException
import java.io.OutputStream
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Handler
import java.util.*

const val REQUEST_ENABLE_BT = 1
class MainActivity : AppCompatActivity(), SensorEventListener {
    private lateinit var sensorManager: SensorManager
    private lateinit var accelerometer: Sensor
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var bluetoothSocket: BluetoothSocket
    private lateinit var outputStream: OutputStream
    private lateinit var mensajeTextView: TextView
    private lateinit var circleImageView: ImageView
    private var handler: Handler = Handler()
    private val colorChangeDelay: Long = 500 // Retraso de 500 milisegundos

    private val DIRECCION_ENFRENTE = 1
    private val DIRECCION_ATRAS = 2
    private val DIRECCION_IZQUIERDA = 3
    private val DIRECCION_DERECHA = 4
    lateinit var mBtAdapter: BluetoothAdapter
    var mAddressDevices: ArrayAdapter<String>? = null
    var mNameDevices: ArrayAdapter<String>? = null
    companion object {
        var m_myUUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
        private var m_bluetoothSocket: BluetoothSocket? = null

        var m_isConnected: Boolean = false
        lateinit var m_address: String
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        mAddressDevices = ArrayAdapter(this, android.R.layout.simple_list_item_1)
        mNameDevices = ArrayAdapter(this, android.R.layout.simple_list_item_1)
        val idBtnOnBT = findViewById<Button>(R.id.idBtnOnBT)
        val idBtnOffBT = findViewById<Button>(R.id.idBtnOffBT)
        val idBtnConect = findViewById<Button>(R.id.idBtnConect)
        val idBtnDispBT = findViewById<Button>(R.id.idBtnDispBT)
        val idSpinDisp = findViewById<Spinner>(R.id.idSpinDisp)
        circleImageView = findViewById(R.id.circleImageView)

        mensajeTextView = findViewById(R.id.mensajeTextView)

        idBtnOnBT.setOnClickListener {
            if (mBtAdapter.isEnabled) {
                //Si ya está activado
                Toast.makeText(this, "Bluetooth ya se encuentra activado", Toast.LENGTH_LONG).show()
            } else {
                //Encender Bluetooth
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.BLUETOOTH_CONNECT
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    Log.i("MainActivity", "ActivityCompat#requestPermissions")
                }
                someActivityResultLauncher.launch(enableBtIntent)
            }
        }

        //Boton apagar bluetooth
        idBtnOffBT.setOnClickListener {
            if (!mBtAdapter.isEnabled) {
                //Si ya está desactivado
                Toast.makeText(this, "Bluetooth ya se encuentra desactivado", Toast.LENGTH_LONG).show()
            } else {
                //Encender Bluetooth
                mBtAdapter.disable()
                Toast.makeText(this, "Se ha desactivado el bluetooth", Toast.LENGTH_LONG).show()
            }
        }

        //Boton dispositivos emparejados
        idBtnDispBT.setOnClickListener {
            if (mBtAdapter.isEnabled) {

                val pairedDevices: Set<BluetoothDevice>? = mBtAdapter?.bondedDevices
                mAddressDevices!!.clear()
                mNameDevices!!.clear()

                pairedDevices?.forEach { device ->
                    val deviceName = device.name
                    val deviceHardwareAddress = device.address // MAC address
                    mAddressDevices!!.add(deviceHardwareAddress)
                    //........... EN ESTE PUNTO GUARDO LOS NOMBRE A MOSTRARSE EN EL COMBO BOX
                    mNameDevices!!.add(deviceName)
                }

                //ACTUALIZO LOS DISPOSITIVOS
                idSpinDisp.setAdapter(mNameDevices)
            } else {
                val noDevices = "Ningun dispositivo pudo ser emparejado"
                mAddressDevices!!.add(noDevices)
                mNameDevices!!.add(noDevices)
                Toast.makeText(this, "Primero vincule un dispositivo bluetooth", Toast.LENGTH_LONG).show()
            }
        }

        idBtnConect.setOnClickListener {
            try {
                if (m_bluetoothSocket == null || !m_isConnected) {

                    val IntValSpin = idSpinDisp.selectedItemPosition
                    m_address = mAddressDevices!!.getItem(IntValSpin).toString()
                    Toast.makeText(this,m_address,Toast.LENGTH_LONG).show()
                    // Cancel discovery because it otherwise slows down the connection.
                    mBtAdapter?.cancelDiscovery()
                    val device: BluetoothDevice = mBtAdapter.getRemoteDevice(m_address)
                    m_bluetoothSocket = device.createInsecureRfcommSocketToServiceRecord(m_myUUID)
                    m_bluetoothSocket!!.connect()
                }

                Toast.makeText(this,"CONEXION EXITOSA",Toast.LENGTH_LONG).show()
                Log.i("MainActivity", "CONEXION EXITOSA")

            } catch (e: IOException) {
                //connectSuccess = false
                e.printStackTrace()
                Toast.makeText(this,"ERROR DE CONEXION",Toast.LENGTH_LONG).show()
                Log.i("MainActivity", "ERROR DE CONEXION")
            }
        }
        val someActivityResultLauncher = registerForActivityResult(
            StartActivityForResult()
        ) { result ->
            if (result.resultCode == REQUEST_ENABLE_BT) {
                Log.i("MainActivity", "ACTIVIDAD REGISTRADA")
            }
        }

        //Inicializacion del bluetooth adapter
        mBtAdapter = (getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter

        //Checar si esta encendido o apagado
        if (mBtAdapter == null) {
            Toast.makeText(this, "Bluetooth no está disponible en este dipositivo", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(this, "Bluetooth está disponible en este dispositivo", Toast.LENGTH_LONG).show()
        }
    }
    val someActivityResultLauncher = registerForActivityResult(
        StartActivityForResult()
    ) { result ->
        if (result.resultCode == REQUEST_ENABLE_BT) {
            Log.i("MainActivity", "ACTIVIDAD REGISTRADA")
        }
    }
    override fun onResume() {
        super.onResume()
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        val x: Float = event!!.values[0]
        val y: Float = event!!.values[1]
        val z: Float = event!!.values[2]

        val maskRotation = when {
            x > 1.0 -> 0f  // Sin sombreado
            x < -1.0 -> 180f  // Sombreado en la mitad izquierda
            y > 1.0 -> 90f  // Sombreado en la mitad superior
            y < -1.0 -> -90f  // Sombreado en la mitad inferior
            else -> 0f  // Sin sombreado
        }
        circleImageView.rotation = maskRotation
        // Determinar la dirección según los valores del acelerómetro
        var direccion = DIRECCION_ENFRENTE
        if (x > 1.0) {
            // Girando a la izquierda
            mensajeTextView.text = "Girando a la izquierda"
            sendCommand("3")
        } else if (x < -1.0) {
            // Girando a la derecha
            mensajeTextView.text = "Girando a la derecha"
            sendCommand("4")
        } else if (y > 1.0) {
            // Yendo hacia atrás
            mensajeTextView.text = "Yendo hacia atrás"
            sendCommand("2")
        } else if (y < -1.0) {
            // Yendo hacia enfrente
            mensajeTextView.text = "Yendo hacia enfrente"
            sendCommand("1")
        } else {
            // Sin movimiento detectado
            circleImageView.clearColorFilter()
            mensajeTextView.text = "Sin movimiento detectado"
        }

    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        if (sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            // Implementa el código necesario para manejar cambios en la precisión del acelerómetro aquí
        }
    }


    private fun sendCommand(input: String) {
        if (m_bluetoothSocket != null) {
            try {
                val outputStream = m_bluetoothSocket!!.outputStream
                outputStream.write(input.toByteArray())
                outputStream.flush()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }


}