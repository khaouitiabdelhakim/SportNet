package com.ensias.sportnet

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.ensias.sportnet.databinding.ActivityStarterBinding
import com.ensias.sportnet.models.User
import com.ensias.sportnet.utils.Utils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.google.gson.GsonBuilder

// Déclaration de la classe StarterActivity qui hérite de AppCompatActivity
class StarterActivity : AppCompatActivity() {

    // Déclaration des variables pour le binding, l'authentification Firebase et la référence à la base de données
    private lateinit var binding: ActivityStarterBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    // Méthode onCreate appelée lors de la création de l'activité
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStarterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialisation de l'authentification Firebase et de la référence à la base de données
        auth = Firebase.auth
        database = FirebaseDatabase.getInstance().reference

        // Vérification si un utilisateur est actuellement connecté
        val currentUser = auth.currentUser
        if (currentUser != null) {
            // Si un utilisateur est connecté, récupérer ses données
            getUserData(currentUser.uid)
        } else {
            // Sinon, lancer l'activité principale et terminer cette activité
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    // Méthode pour récupérer les données de l'utilisateur à partir de Firebase
    private fun getUserData(uid: String) {
        database.child("users").child(uid).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(User::class.java)
                if (user != null) {
                    // Si les données de l'utilisateur sont récupérées avec succès, les assigner à currentUser
                    MainActivity.currentUser = user
                    val gson = GsonBuilder().create()
                    try {
                        // Convertir les chaînes JSON en ArrayList
                        MainActivity.userLikes = gson.fromJson(user.likes, Utils.arrayListOfStringsToken)
                        MainActivity.userCommentsLikes = gson.fromJson(user.commentsLikes, Utils.arrayListOfStringsToken)
                    } catch (_:Exception){ }
                }
                // Lancer l'activité principale après avoir récupéré les données
                startMainActivity()
            }
            override fun onCancelled(error: DatabaseError) {
                // En cas d'erreur lors de la récupération des données, lancer quand même l'activité principale
                startMainActivity()
            }
        })
    }

    // Méthode pour lancer l'activité principale
    private fun startMainActivity() {
        startActivity(Intent(this@StarterActivity, MainActivity::class.java))
        finish()
    }
}
