package ppapps.phapamnhacnho.modules.settings

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import ppapps.phapamnhacnho.R
import ppapps.phapamnhacnho.databinding.ActivitySettingsBinding
import java.util.*

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private lateinit var sharedPreferences: SharedPreferences
    
    companion object {
        private const val PREFS_NAME = "AppSettings"
        private const val KEY_DARK_MODE = "dark_mode"
        private const val KEY_LANGUAGE = "language"
        const val LANG_VIETNAMESE = "vi"
        const val LANG_ENGLISH = "en"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        setupToolbar()
        loadSettings()
        setupListeners()
    }

    private fun setupToolbar() {
        binding.settingsToolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun loadSettings() {
        // Load Dark Mode
        val isDarkMode = sharedPreferences.getBoolean(KEY_DARK_MODE, false)
        binding.switchDarkMode.isChecked = isDarkMode

        // Load Language
        val currentLang = sharedPreferences.getString(KEY_LANGUAGE, LANG_VIETNAMESE) ?: LANG_VIETNAMESE
        updateLanguageUI(currentLang)
    }

    private fun setupListeners() {
        // Dark Mode Toggle
        binding.switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            saveDarkMode(isChecked)
            applyDarkMode(isChecked)
        }

        // Vietnamese Language
        binding.cardLangVietnamese.setOnClickListener {
            changeLanguage(LANG_VIETNAMESE)
        }

        // English Language
        binding.cardLangEnglish.setOnClickListener {
            changeLanguage(LANG_ENGLISH)
        }
    }

    private fun saveDarkMode(isEnabled: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_DARK_MODE, isEnabled).apply()
    }

    private fun applyDarkMode(isEnabled: Boolean) {
        if (isEnabled) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }

    private fun changeLanguage(languageCode: String) {
        // Save language preference
        sharedPreferences.edit().putString(KEY_LANGUAGE, languageCode).apply()

        // Update UI
        updateLanguageUI(languageCode)

        // Apply language change
        setLocale(languageCode)

        // Recreate activity to apply changes
        recreate()
    }

    private fun setLocale(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        
        val config = Configuration(resources.configuration)
        config.setLocale(locale)
        
        @Suppress("DEPRECATION")
        resources.updateConfiguration(config, resources.displayMetrics)
    }

    private fun updateLanguageUI(languageCode: String) {
        val primaryColor = getColor(R.color.md_theme_light_primary)
        
        when (languageCode) {
            LANG_VIETNAMESE -> {
                // Vietnamese selected
                binding.cardLangVietnamese.strokeWidth = dpToPx(2)
                binding.cardLangVietnamese.strokeColor = primaryColor
                binding.iconLangViCheck.visibility = View.VISIBLE

                binding.cardLangEnglish.strokeWidth = 0
                binding.iconLangEnCheck.visibility = View.GONE
            }
            LANG_ENGLISH -> {
                // English selected
                binding.cardLangEnglish.strokeWidth = dpToPx(2)
                binding.cardLangEnglish.strokeColor = primaryColor
                binding.iconLangEnCheck.visibility = View.VISIBLE

                binding.cardLangVietnamese.strokeWidth = 0
                binding.iconLangViCheck.visibility = View.GONE
            }
        }
    }

    private fun dpToPx(dp: Int): Int {
        val density = resources.displayMetrics.density
        return (dp * density).toInt()
    }
}
