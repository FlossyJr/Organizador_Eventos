package com.example.trabalhodevmobile

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException

class criarUsuario : AppCompatActivity() {
    private lateinit var editTextEmailCriar: EditText
    private lateinit var auth: FirebaseAuth
    private lateinit var editTextPasswordCriar: EditText
    private lateinit var buttonCria: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_criar_usuario)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        editTextEmailCriar = findViewById(R.id.editTextTextEmailAddressCriar)
        editTextPasswordCriar = findViewById(R.id.editTextTextPasswordCriar)
        buttonCria = findViewById(R.id.buttonCriarUser)
        auth = Firebase.auth

        buttonCria.setOnClickListener {
            val email = editTextEmailCriar.text.toString().trim()
            val password = editTextPasswordCriar.text.toString()
            if (email.isEmpty()) {
                editTextEmailCriar.error = "Informe um e-mail"
                editTextEmailCriar.requestFocus()
                return@setOnClickListener
            }
            if (password.isEmpty()) {
                editTextPasswordCriar.error = "Informe uma senha"
                editTextPasswordCriar.requestFocus()
                return@setOnClickListener
            }
            if (password.length < 6) {
                editTextPasswordCriar.error = "A senha deve possuir no mínimo 6 caracteres"
                editTextPasswordCriar.requestFocus()
                return@setOnClickListener
            }
            createUser(email, password)
        }
    }
    private fun createUser(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "createUserWithEmail:success")
                    Toast.makeText(
                        this,
                        "Usuário criado com sucesso!",
                        Toast.LENGTH_SHORT
                    ).show()
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    //updateUI(user)
                } else {
                    val mensagemErro = when (task.exception) {

                        is FirebaseAuthWeakPasswordException ->
                            "A senha deve possuir pelo menos 6 caracteres."

                        is FirebaseAuthInvalidCredentialsException ->
                            "E-mail inválido."

                        else ->
                            "Erro ao criar usuário."
                    }

                    Toast.makeText(this, mensagemErro, Toast.LENGTH_LONG).show()
                    Log.e(TAG, "Erro", task.exception)
                }
            }
    }


}