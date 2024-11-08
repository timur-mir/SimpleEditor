package home.product.editor.main

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.internal.view.SupportMenu
import androidx.fragment.app.Fragment
import home.product.editor.R
import home.product.editor.base.BaseView
import home.product.editor.base.setMovingText
import org.jetbrains.annotations.NotNull
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.util.logging.Handler

object Help {
    var clickFlag = false
}

class MainActivity : AppCompatActivity(), MainView {
    private lateinit var llContainer: LinearLayout
    private lateinit var homeFragment: HomeFragment
    private lateinit var contentFragment: ContentFragment
    private lateinit var tvHeader: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupInstances()
        bindsView()
        tvHeader.setSelected(true)
        val info = resources.getString(R.string.info_message)
        tvHeader.setMovingText(info)
        navigateToHome()
    }

    override fun navigateToHome() {
        val count = supportFragmentManager.backStackEntryCount
        val transaction = supportFragmentManager.beginTransaction()
        if (count == 0) {
            transaction.add(R.id.ll_container, homeFragment)
            transaction.addToBackStack("Home")
            transaction.commit()
            supportActionBar?.title = "Главная"
        } else {
            transaction.replace(R.id.ll_container, homeFragment)
            transaction.addToBackStack("Home")
            transaction.commit()
            supportActionBar?.title = "Главная"
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        val count = supportFragmentManager.backStackEntryCount
        if (count == 1) {
            // super.onBackPressed()
            finish()
        } else {
            supportFragmentManager.popBackStack()
            hideHomeNavigation()
            supportActionBar?.title = "Главная"
        }
    }

    override fun navigateToContent() {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.ll_container, contentFragment)
        transaction.addToBackStack("Content")
        transaction.commit()
        supportActionBar?.title = "Сохранённые записи"
    }

    override fun bindsView() {
        llContainer = findViewById(R.id.ll_container)
        tvHeader = findViewById(R.id.tv_header)
    }

    override fun setupInstances() {
        homeFragment = HomeFragment()
        contentFragment = ContentFragment()
    }

    override fun hideHomeNavigation() {
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
        supportActionBar?.title = "Главная"
    }

    private fun showHomeNavigation() {
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item?.itemId
        if (id == android.R.id.home) {
            val count = supportFragmentManager.backStackEntryCount
            supportFragmentManager.popBackStack()
            navigateToHome()
            supportFragmentManager.popBackStack()
            hideHomeNavigation()
        }
        return super.onOptionsItemSelected(item)
    }

    class HomeFragment : Fragment(), BaseView, View.OnClickListener {
        private lateinit var layout: RelativeLayout

        private lateinit var etInput: EditText
        private lateinit var btnSubmit: Button
        private lateinit var btnViewFile: Button
        private lateinit var btnAddFile: Button
        private lateinit var btnClear: Button

        private var outputStream: FileOutputStream? = null

        companion object {
            private const val RECOGNIZER_RESULT = 1234
        }

        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            layout = inflater.inflate(
                R.layout.fragment_home,
                container, false
            ) as RelativeLayout
            setupInstances()
            bindsView()

            return layout
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
        }

        override fun onPause() {
            super.onPause()
            clearInput()
        }

        override fun bindsView() {
            etInput = layout.findViewById(R.id.et_input)
            btnSubmit = layout.findViewById(R.id.btn_submit)
            btnViewFile = layout.findViewById(R.id.btn_view_file)
            btnAddFile = layout.findViewById(R.id.btn_add_file)
            btnClear = layout.findViewById(R.id.btn_clear_field)
            btnSubmit.setOnClickListener(this)
            btnViewFile.setOnClickListener(this)
            btnAddFile.setOnClickListener(this)
            btnClear.setOnClickListener(this)
        }
        override fun setupInstances() {
            outputStream = activity?.openFileOutput("content_file", Context.MODE_APPEND)
        }

        private fun showInputError() {
            etInput.error = "Поле не может быть пустым"
            etInput.requestFocus()
        }

        private fun writeFile(content: String) {
            try {
                outputStream?.write(content.toByteArray())
                showSaveSuccess()
            } catch (ex: IOException) {
                showException(ex)
            } finally {
                try {
                    if (outputStream != null)
                        outputStream!!.close();
                } catch (ex: IOException) {
                    showException(ex)
                }
            }

        }

        private fun clearInput() {
            etInput.setText("")
        }

        private fun showSaveSuccess() {
            Toast.makeText(activity, resources.getString(R.string.saved_note), Toast.LENGTH_LONG)
                .show()
        }

        private fun showException(exception: Exception) {
            Toast.makeText(activity, "${exception.message}", Toast.LENGTH_LONG).show()
        }

        override fun onClick(view: View?) {
            val id = view?.id
            if (id == R.id.btn_submit) {
                if (TextUtils.isEmpty(etInput.text)) {
                    showInputError()
                } else {
                    writeFile(" " + etInput.text.toString())
                }
            } else if (id == R.id.btn_view_file) {
                val mainActivity = activity as MainActivity
                mainActivity.navigateToContent()
                mainActivity.showHomeNavigation()
            } else if (id == R.id.btn_add_file) {
                addCharacters()
            } else if (id == R.id.btn_clear_field) {
                clearInput()
            }
        }

        private fun addCharacters() {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            intent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speech to text")
            startActivityForResult(intent, RECOGNIZER_RESULT)
        }

        @SuppressLint("SetTextI18n")
        override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
            if (requestCode === RECOGNIZER_RESULT && resultCode === RESULT_OK) {
                val matches: ArrayList<String>? = data?.getStringArrayListExtra(
                    RecognizerIntent.EXTRA_RESULTS
                )
                val temporaryText: String = etInput.text.toString()
                etInput.setText("$temporaryText ${matches?.get(0) ?: ""}")
            }
            super.onActivityResult(requestCode, resultCode, data)
        }

    }

    class ContentFragment : Fragment(), BaseView {
        private lateinit var layout: LinearLayout
        private lateinit var tvContent: TextView
        private lateinit var clContent: Button
        private lateinit var backMain: Button
        private lateinit var inputStream: FileInputStream

        override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View {
            layout = inflater.inflate(
                R.layout.fragment_content,
                container, false
            ) as LinearLayout
            setupInstances()
            bindsView()
            return layout
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            clContent.setOnClickListener {
                Help.clickFlag = true
                Erase()
                backToMainFragment()
            }
            backMain.setOnClickListener {
                backToMainFragment()
            }
        }

        override fun onResume() {
            updateContent()
            super.onResume()
        }

        private fun updateContent() {
            tvContent.text = readFile()
        }


        override fun bindsView() {
            tvContent = layout.findViewById(R.id.tv_content)
            clContent = layout.findViewById(R.id.cl_content)
            backMain = layout.findViewById(R.id.backt)
        }

        override fun setupInstances() {
            inputStream = activity?.openFileInput("content_file") as FileInputStream
        }

        private fun readFile(): String {
            val bytes = ByteArray(inputStream.available())
            inputStream.read(bytes)
            return String(bytes)
        }

        private fun backToMainFragment() {
     var homeFragment: HomeFragment= HomeFragment()
            var count = parentFragmentManager.backStackEntryCount
            while (count > 0) {
                parentFragmentManager.popBackStack()
                count -= 1
            }
            if (count == 0) {
                if (Help.clickFlag) {
                    Thread.sleep(1200)
                    val transaction = parentFragmentManager.beginTransaction()
                    transaction.replace(R.id.ll_container, homeFragment)
                    transaction.addToBackStack("Home")
                    transaction.commit()

                    (activity as MainView).hideHomeNavigation()
                    Help.clickFlag = false
                } else {
                    val transaction = parentFragmentManager.beginTransaction()
                    transaction.replace(R.id.ll_container, homeFragment)
                    transaction.addToBackStack("Home")
                    transaction.commit()
                    (activity as MainView).hideHomeNavigation()

                }
            }
        }

        private fun showClearSuccess() {
            Toast.makeText(requireContext(), "Файл очищен!", Toast.LENGTH_LONG).show()
            backToMainFragment()
        }

        override fun onPause() {
            super.onPause()
        }

        private fun showException(exception: Exception) {
            Toast.makeText(activity, "${exception.message}", Toast.LENGTH_LONG).show()
        }

        private fun Erase() {
            var fos2: FileOutputStream? = null
            try {
                val text = ""
                fos2 = activity?.openFileOutput("content_file", MODE_PRIVATE)
                fos2!!.write(text.toByteArray())
                tvContent.text = ""
                showClearSuccess()
            } catch (ex: IOException) {
                showException(ex)
            } finally {
                try {
                    fos2?.close()
                } catch (ex: IOException) {
                    showException(ex)
                }
            }
        }

        /*
            private fun readFile2(): String {
            var c: Int
            var content = ""
            c = inputStream.read()
            while (c != -1) {
                content += Character.toString(c.toChar())
                c = inputStream.read()
            }
            inputStream.close()
            return content
        }
    */
    }
}
