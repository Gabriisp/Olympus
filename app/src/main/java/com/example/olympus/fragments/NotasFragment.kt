package com.example.olympus

// Fragmento para gestionar notas del usuario
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.olympus.utils.SessionManager

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
            mostrarDialogoNota(null)
        }

        return view
    }

    // Carga las notas del usuario desde Firebase
    private fun loadNotas() {
        val sessionManager = SessionManager(requireContext())
        val userUid = sessionManager.getUserUid().orEmpty()

        if (userUid.isBlank()) {
            recyclerNotas.adapter = CloudNotasAdapter(emptyList(), {}, {})
            return
        }

        firebaseRepository.getNotesByUser(userUid) { result ->
            activity?.runOnUiThread {
                result.onSuccess { notas ->
                    recyclerNotas.adapter = CloudNotasAdapter(notas, { noteId ->
                        firebaseRepository.deleteNote(noteId) { deleteResult ->
                            activity?.runOnUiThread {
                                deleteResult.onSuccess {
                                    loadNotas()
                                }.onFailure { error ->
                                    requireContext().showToast(error.message ?: "No se pudo eliminar la nota", Toast.LENGTH_LONG)
                                }
                            }
                        }
                    }, { nota ->
                        mostrarDialogoNota(nota)
                    })
                }.onFailure { error ->
                    requireContext().showToast(error.message ?: "No se pudieron cargar las notas", Toast.LENGTH_LONG)
                    recyclerNotas.adapter = CloudNotasAdapter(emptyList(), {}, {})
                }
            }
        }
    }

    // Muestra un dialogo para crear o editar una nota
    private fun mostrarDialogoNota(nota: CloudNote?) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_nueva_nota, null)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        val tvTitulo = dialogView.findViewById<TextView>(R.id.dialogTitle)
        val etTitulo = dialogView.findViewById<EditText>(R.id.etTitulo)
        val etContenido = dialogView.findViewById<EditText>(R.id.etContenido)
        val btnGuardar = dialogView.findViewById<Button>(R.id.btnGuardar)
        val btnCancelar = dialogView.findViewById<Button>(R.id.btnCancelar)

        val esEdicion = nota != null
        tvTitulo.text = if (esEdicion) "Editar Nota" else "Nueva Nota"

        if (esEdicion) {
            etTitulo.setText(nota!!.titulo)
            etContenido.setText(nota.contenido)
        }

        btnCancelar.setOnClickListener {
            dialog.dismiss()
        }

        btnGuardar.setOnClickListener {
            val titulo = etTitulo.text.toString().trim()
            val contenido = etContenido.text.toString().trim()
            
            if (titulo.isEmpty()) {
                requireContext().showToast("Ingresa un título")
                return@setOnClickListener
            }

            val sessionManager = SessionManager(requireContext())
            val userUid = sessionManager.getUserUid().orEmpty()
            if (userUid.isBlank()) {
                requireContext().showToast("Necesitas iniciar sesión para guardar notas")
                return@setOnClickListener
            }

            if (esEdicion) {
                firebaseRepository.updateNote(nota!!.id, titulo, contenido) { result ->
                    activity?.runOnUiThread {
                        result.onSuccess {
                            dialog.dismiss()
                            loadNotas()
                            requireContext().showToast("Nota actualizada")
                        }.onFailure { error ->
                            requireContext().showToast(error.message ?: "No se pudo actualizar la nota", Toast.LENGTH_LONG)
                        }
                    }
                }
            } else {
                firebaseRepository.createNote(userUid, titulo, contenido) { result ->
                    activity?.runOnUiThread {
                        result.onSuccess {
                            dialog.dismiss()
                            loadNotas()
                            requireContext().showToast("Nota guardada")
                        }.onFailure { error ->
                            requireContext().showToast(error.message ?: "No se pudo guardar la nota", Toast.LENGTH_LONG)
                        }
                    }
                }
            }
        }

        dialog.show()
    }
}