package com.example.olympus

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class NotasFragment : Fragment() {

    private val firebaseRepository = FirebaseRepository.instance
    private lateinit var recyclerNotas: RecyclerView
    private lateinit var btnNuevaNota: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_notas, container, false)

        recyclerNotas = view.findViewById(R.id.recyclerNotas)
        btnNuevaNota = view.findViewById(R.id.btnNuevaNota)

        recyclerNotas.layoutManager = LinearLayoutManager(requireContext())
        
        loadNotas()

        btnNuevaNota.setOnClickListener {
            mostrarDialogoNuevaNota()
        }

        return view
    }

    private fun loadNotas() {
        val sessionManager = SessionManager(requireContext())
        val userUid = sessionManager.getUserUid().orEmpty()

        if (userUid.isBlank()) {
            recyclerNotas.adapter = CloudNotasAdapter(emptyList()) { }
            return
        }

        firebaseRepository.getNotesByUser(userUid) { result ->
            activity?.runOnUiThread {
                result.onSuccess { notas ->
                    recyclerNotas.adapter = CloudNotasAdapter(notas) { noteId ->
                        firebaseRepository.deleteNote(noteId) { deleteResult ->
                            activity?.runOnUiThread {
                                deleteResult.onSuccess {
                                    loadNotas()
                                }.onFailure { error ->
                                    Toast.makeText(
                                        requireContext(),
                                        error.message ?: "No se pudo eliminar la nota",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                        }
                    }
                }.onFailure { error ->
                    Toast.makeText(
                        requireContext(),
                        error.message ?: "No se pudieron cargar las notas",
                        Toast.LENGTH_LONG
                    ).show()
                    recyclerNotas.adapter = CloudNotasAdapter(emptyList()) { }
                }
            }
        }
    }

    private fun mostrarDialogoNuevaNota() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_nueva_nota, null)
        val etTitulo = dialogView.findViewById<EditText>(R.id.etTitulo)
        val etContenido = dialogView.findViewById<EditText>(R.id.etContenido)

        AlertDialog.Builder(requireContext())
            .setTitle("Nueva Nota")
            .setView(dialogView)
            .setPositiveButton("Guardar") { _, _ ->
                val titulo = etTitulo.text.toString().trim()
                val contenido = etContenido.text.toString().trim()
                
                if (titulo.isNotEmpty()) {
                    val sessionManager = SessionManager(requireContext())
                    val userUid = sessionManager.getUserUid().orEmpty()
                    if (userUid.isBlank()) {
                        Toast.makeText(requireContext(), "Necesitas iniciar sesión para guardar notas", Toast.LENGTH_SHORT).show()
                    } else {
                        firebaseRepository.createNote(userUid, titulo, contenido) { result ->
                            activity?.runOnUiThread {
                                result.onSuccess {
                                    loadNotas()
                                    Toast.makeText(requireContext(), "Nota guardada", Toast.LENGTH_SHORT).show()
                                }.onFailure { error ->
                                    Toast.makeText(
                                        requireContext(),
                                        error.message ?: "No se pudo guardar la nota",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                        }
                    }
                } else {
                    Toast.makeText(requireContext(), "Ingresa un título", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}
