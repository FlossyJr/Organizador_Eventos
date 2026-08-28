package com.example.trabalhodevmobile

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CalendarView
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import android.widget.CheckBox


private lateinit var auth: FirebaseAuth

private lateinit var db: FirebaseFirestore
private lateinit var calendarView: CalendarView
private lateinit var seletorEvento: Spinner
private lateinit var nomeEvento: EditText
private lateinit var descricaoEvento: EditText
private lateinit var dataEvento: TextView
private var dataSelecionada: Long = 0L
private lateinit var botaoConfirmar: Button
private lateinit var checkExcluir: CheckBox

data class Evento(
    var id: String = "",
    var titulo: String = "",
    var descricao: String = "",
    var data: Long = 0L
)

private val listaEventos = mutableListOf<Evento>()
class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        auth = Firebase.auth
        db = Firebase.firestore
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        seletorEvento = findViewById(R.id.seletorEvento)
        nomeEvento = findViewById(R.id.nomeEvento)
        descricaoEvento = findViewById(R.id.descricaoEvento)
        dataEvento = findViewById(R.id.dataEvento)
        calendarView = findViewById(R.id.calendarView)
        botaoConfirmar = findViewById(R.id.buttonConfirm)
        checkExcluir = findViewById(R.id.checkExcluir)
        val calendario = Calendar.getInstance()
        calendario.timeInMillis = calendarView.date
        calendario.set(Calendar.HOUR_OF_DAY, 0)
        calendario.set(Calendar.MINUTE, 0)
        calendario.set(Calendar.SECOND, 0)
        calendario.set(Calendar.MILLISECOND, 0)
        dataSelecionada = calendario.timeInMillis
        carregarEventos(dataSelecionada)
        calendarView.setOnDateChangeListener { _, year, month, day ->
            val calendario = Calendar.getInstance()
            calendario.set(year, month, day, 0, 0, 0)
            calendario.set(Calendar.MILLISECOND, 0)
            dataSelecionada = calendario.timeInMillis
            carregarEventos(dataSelecionada)
        }
        seletorEvento.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val evento = listaEventos[position]
                if (evento.id.isEmpty()) {
                    nomeEvento.setText("")
                    descricaoEvento.setText("")
                    val formato = SimpleDateFormat(
                        "dd/MM/yyyy",
                        Locale.getDefault()
                    )
                    dataEvento.text = formato.format(Date(dataSelecionada))
                    return
                }
                nomeEvento.setText(evento.titulo)
                descricaoEvento.setText(evento.descricao)
                val formato = SimpleDateFormat(
                    "dd/MM/yyyy HH:mm",
                    Locale.getDefault()
                )
                dataEvento.text = formato.format(Date(evento.data))
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
        botaoConfirmar.setOnClickListener {
            if (listaEventos.isEmpty()) {
                Toast.makeText(
                    this,
                    "Selecione uma data primeiro.",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
            val eventoSelecionado = listaEventos[seletorEvento.selectedItemPosition]
            if (eventoSelecionado.id.isEmpty()) {
                if (checkExcluir.isChecked) {
                    Toast.makeText(
                        this,
                        "Selecione um evento para excluir.",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    criarNovoEvento()
                }
            } else {
                if (checkExcluir.isChecked) {
                    excluirEvento(eventoSelecionado.id)
                } else {
                    atualizarEventoSelecionado(eventoSelecionado)
                }
            }
        }
    }
    private fun preencherSpinner(){
        val nomes = listaEventos.map { it.titulo }
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            nomes
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        seletorEvento.adapter = adapter
    }
    private fun criarNovoEvento() {
        val titulo = nomeEvento.text.toString().trim()
        val descricao = descricaoEvento.text.toString().trim()
        if (dataSelecionada == 0L) {
            Toast.makeText(
                this,
                "Selecione uma data no calendário.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        if (titulo.isEmpty()) {
            nomeEvento.error = "Informe o nome do evento"
            nomeEvento.requestFocus()
            return
        }
        val evento = Evento(
            titulo = titulo,
            descricao = descricao,
            data = dataSelecionada
        )
        salvarEvento(evento)
    }

    private fun salvarEvento(evento: Evento){
        val user = auth.currentUser
        if (user == null) {
            Toast.makeText(
                this,
                "Usuário não autenticado.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        val uid = user.uid
        db.collection("usuarios")
            .document(uid)
            .collection("eventos")
            .add(evento)
            .addOnSuccessListener {
                Toast.makeText(
                    this,
                    "Evento criado com sucesso!",
                    Toast.LENGTH_SHORT
                ).show()
                carregarEventos(dataSelecionada)
                nomeEvento.setText("")
                descricaoEvento.setText("")
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this,
                    e.message,
                    Toast.LENGTH_LONG
                ).show()
            }
    }
    private fun carregarEventos(dataSelecionada: Long) {
        val user = auth.currentUser ?: return
        val calendario = Calendar.getInstance()
        calendario.timeInMillis = dataSelecionada
        calendario.set(Calendar.HOUR_OF_DAY, 0)
        calendario.set(Calendar.MINUTE, 0)
        calendario.set(Calendar.SECOND, 0)
        calendario.set(Calendar.MILLISECOND, 0)
        val inicioDia = calendario.timeInMillis
        calendario.add(Calendar.DAY_OF_MONTH, 1)
        val fimDia = calendario.timeInMillis
        db.collection("usuarios")
            .document(user.uid)
            .collection("eventos")
            .whereGreaterThanOrEqualTo("data", inicioDia)
            .whereLessThan("data", fimDia)
            .orderBy("data")
            .get()
            .addOnSuccessListener { resultado ->
                listaEventos.clear()
                listaEventos.add(
                    Evento(
                        id = "",
                        titulo = "Novo Evento",
                        descricao = "",
                        data = dataSelecionada
                    )
                )
                for(document in resultado){
                    val evento = document.toObject(Evento::class.java)
                    evento.id = document.id
                    listaEventos.add(evento)
                }
                preencherSpinner()
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this,
                    e.message,
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private fun atualizarEvento(evento: Evento) {
        val user = auth.currentUser ?: return
        db.collection("usuarios")
            .document(user.uid)
            .collection("eventos")
            .document(evento.id)
            .set(evento)
            .addOnSuccessListener {
                Toast.makeText(
                    this,
                    "Evento atualizado com sucesso!",
                    Toast.LENGTH_SHORT
                ).show()
                carregarEventos(dataSelecionada)

            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this,
                    e.message,
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private fun atualizarEventoSelecionado(evento: Evento) {
        val titulo = nomeEvento.text.toString().trim()
        val descricao = descricaoEvento.text.toString().trim()
        if (titulo.isEmpty()) {
            nomeEvento.error = "Informe o nome do evento"
            nomeEvento.requestFocus()
            return
        }
        evento.titulo = titulo
        evento.descricao = descricao
        atualizarEvento(evento)
    }
    private fun excluirEvento(id: String) {
        val user = auth.currentUser ?: return
        db.collection("usuarios")
            .document(user.uid)
            .collection("eventos")
            .document(id)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(
                    this,
                    "Evento excluído com sucesso!",
                    Toast.LENGTH_SHORT
                ).show()
                checkExcluir.isChecked = false
                nomeEvento.setText("")
                descricaoEvento.setText("")
                carregarEventos(dataSelecionada)
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this,
                    e.message,
                    Toast.LENGTH_LONG
                ).show()
            }
    }
}