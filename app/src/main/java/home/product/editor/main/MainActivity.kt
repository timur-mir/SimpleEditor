package home.product.editor.main

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import home.product.editor.R
import home.product.editor.base.BaseView
import java.io.FileInputStream
import java.io.FileOutputStream

class MainActivity : AppCompatActivity(), MainView {
    private lateinit var llContainer: LinearLayout
    private lateinit var homeFragment: HomeFragment
    private lateinit var contentFragment: ContentFragment
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupInstances()
        bindsView()
        navigateToHome()
    }

    override fun navigateToHome() {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.ll_container, homeFragment)
        transaction.commit()
        supportActionBar?.title = "Главная"
    }

    override fun navigateToContent() {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.ll_container, contentFragment)
        transaction.commit()
        supportActionBar?.title = "Контент"
    }

    override fun bindsView() {
        llContainer = findViewById(R.id.ll_container)
    }

    override fun setupInstances() {
        homeFragment = HomeFragment()
        contentFragment = ContentFragment()
    }

    private fun hideHomeNavigation() {
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
    }

    private fun showHomeNavigation() {
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item?.itemId
        if (id == android.R.id.home) {
            navigateToHome()
            hideHomeNavigation()
        }
        return super.onOptionsItemSelected(item)
    }

    class HomeFragment : Fragment(), BaseView, View.OnClickListener {
        private lateinit var layout: LinearLayout
        private lateinit var tvHeader: TextView
        private lateinit var etInput: EditText
        private lateinit var btnSubmit: Button
        private lateinit var btnViewFile: Button

        private var outputStream: FileOutputStream? = null
        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            layout = inflater.inflate(
                R.layout.fragment_home,
                container, false
            ) as LinearLayout
            setupInstances()
            bindsView()

            return layout
        }

        override fun bindsView() {
            tvHeader = layout.findViewById(R.id.tv_header)
            etInput = layout.findViewById(R.id.et_input)
            btnSubmit = layout.findViewById(R.id.btn_submit)
            btnViewFile = layout.findViewById(R.id.btn_view_file)

            btnSubmit.setOnClickListener(this)
            btnViewFile.setOnClickListener(this)
        }

        override fun setupInstances() {
            outputStream = activity?.openFileOutput("content_file", Context.MODE_PRIVATE)
        }

        private fun showInputError() {
            etInput.error = "Поле не может быть пустым"
            etInput.requestFocus()
        }

        private fun writeFile(content: String) {
            outputStream?.write(content.toByteArray())
        }

        private fun clearInput() {
            etInput.setText("")
        }

        private fun showSaveSuccess() {
            Toast.makeText(activity, "Файл обновлён.", Toast.LENGTH_LONG).show()
        }


        override fun onClick(view: View?) {
            val id = view?.id
            if (id == R.id.btn_submit) {
                if (TextUtils.isEmpty(etInput.text)) {
                    showInputError()
                } else {
                    writeFile(etInput.text.toString())
                    clearInput()
                    showSaveSuccess()
                }
            } else if (id == R.id.btn_view_file) {
                val mainActivity = activity as MainActivity
                mainActivity.navigateToContent()
                mainActivity.showHomeNavigation()
            }
        }

    }

    class ContentFragment : Fragment(), BaseView {
        private lateinit var layout: LinearLayout
        private lateinit var tvContent: TextView
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
        override fun onResume() {
            updateContent()
            super.onResume()
        }
        private fun updateContent() {
            tvContent.text = readFile()
        }


        override fun bindsView() {
            tvContent = layout.findViewById(R.id.tv_content)
        }

        override fun setupInstances() {
            inputStream = activity?.openFileInput("content_file") as FileInputStream
        }
        private fun readFile(): String {
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
    }
    }
