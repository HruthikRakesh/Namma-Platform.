package com.example.nammaplatform

import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.nammaplatform.adapter.CoachAdapter
import com.example.nammaplatform.adapter.TrainAdapter
import com.example.nammaplatform.model.Station
import com.example.nammaplatform.model.Train
import com.example.nammaplatform.model.TrainsData
import org.json.JSONObject
import java.util.Locale

class MainActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    // ── Views ──────────────────────────────────────────────────────────────
    private lateinit var spinnerStation: Spinner
    private lateinit var tvNoStation: TextView
    private lateinit var rvTrains: RecyclerView
    private lateinit var rvCoaches: RecyclerView
    private lateinit var tvSelectedTrainName: TextView
    private lateinit var tvGeneralPosition: TextView
    private lateinit var layoutGeneralInfo: LinearLayout
    private lateinit var tvAnnouncementText: TextView
    private lateinit var btnSpeak: Button

    // ── State ──────────────────────────────────────────────────────────────
    private var tts: TextToSpeech? = null
    private var ttsReady = false
    private var stations: List<Station> = emptyList()
    private var selectedTrain: Train? = null
    private var currentAnnouncementText = ""

    // ──────────────────────────────────────────────────────────────────────
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bindViews()
        initTts()
        loadData()
        setupStationSpinner()
        setupSpeakButton()
    }

    // ── View binding ───────────────────────────────────────────────────────
    private fun bindViews() {
        spinnerStation     = findViewById(R.id.spinnerStation)
        tvNoStation        = findViewById(R.id.tvNoStation)
        rvTrains           = findViewById(R.id.rvTrains)
        rvCoaches          = findViewById(R.id.rvCoaches)
        tvSelectedTrainName = findViewById(R.id.tvSelectedTrainName)
        tvGeneralPosition  = findViewById(R.id.tvGeneralPosition)
        layoutGeneralInfo  = findViewById(R.id.layoutGeneralInfo)
        tvAnnouncementText = findViewById(R.id.tvAnnouncementText)
        btnSpeak           = findViewById(R.id.btnSpeak)
    }

    // ── TTS ────────────────────────────────────────────────────────────────
    private fun initTts() {
        tts = TextToSpeech(this, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            // Try Kannada first, fall back to English
            val kannadaLocale = Locale("kn", "IN")
            val result = tts?.setLanguage(kannadaLocale)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.w("TTS", "Kannada not supported, falling back to English")
                tts?.setLanguage(Locale.ENGLISH)
            }
            tts?.setSpeechRate(0.85f)
            tts?.setPitch(1.0f)
            ttsReady = true
        } else {
            Toast.makeText(this, "Text-To-Speech initialisation failed", Toast.LENGTH_SHORT).show()
        }
    }

    private fun speak(text: String) {
        if (!ttsReady) {
            Toast.makeText(this, "TTS not ready yet, please wait…", Toast.LENGTH_SHORT).show()
            return
        }
        tts?.stop()
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "NP_UTTERANCE")
    }

    // ── Data loading ───────────────────────────────────────────────────────
    private fun loadData() {
        try {
            val json = assets.open("trains_data.json")
                .bufferedReader()
                .use { it.readText() }
            stations = parseStations(json)
        } catch (e: Exception) {
            Log.e("NammaPlatform", "Failed to load trains_data.json", e)
            Toast.makeText(this, "Error loading data", Toast.LENGTH_SHORT).show()
        }
    }

    private fun parseStations(json: String): List<Station> {
        val root = JSONObject(json)
        val stationsArr = root.getJSONArray("stations")
        val result = mutableListOf<Station>()

        for (i in 0 until stationsArr.length()) {
            val s = stationsArr.getJSONObject(i)
            val trainsArr = s.getJSONArray("trains")
            val trains = mutableListOf<Train>()

            for (j in 0 until trainsArr.length()) {
                val t = trainsArr.getJSONObject(j)
                val coachesArr = t.getJSONArray("coaches")
                val coaches = (0 until coachesArr.length()).map { coachesArr.getString(it) }
                trains.add(
                    Train(
                        train_number  = t.getString("train_number"),
                        train_name    = t.getString("train_name"),
                        train_name_kn = t.getString("train_name_kn"),
                        arrival_time  = t.getString("arrival_time"),
                        platform      = t.getInt("platform"),
                        destination   = t.getString("destination"),
                        destination_kn = t.getString("destination_kn"),
                        coaches       = coaches
                    )
                )
            }

            result.add(
                Station(
                    id      = s.getString("id"),
                    name    = s.getString("name"),
                    name_kn = s.getString("name_kn"),
                    trains  = trains
                )
            )
        }
        return result
    }

    // ── Station Spinner ────────────────────────────────────────────────────
    private fun setupStationSpinner() {
        val stationNames = mutableListOf("-- ನಿಲ್ದಾಣ ಆಯ್ಕೆ ಮಾಡಿ / Select Station --")
        stationNames.addAll(stations.map { "${it.name_kn}  (${it.id})" })

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            stationNames
        ).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

        spinnerStation.adapter = adapter

        spinnerStation.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
                if (pos == 0) {
                    showNoStation()
                } else {
                    val station = stations[pos - 1]
                    showTrains(station)
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>) = showNoStation()
        }
    }

    private fun showNoStation() {
        tvNoStation.visibility = View.VISIBLE
        rvTrains.visibility = View.GONE
        resetCoachSection()
        resetAnnouncement()
    }

    // ── Train List ─────────────────────────────────────────────────────────
    private fun showTrains(station: Station) {
        tvNoStation.visibility = View.GONE
        rvTrains.visibility = View.VISIBLE

        val adapter = TrainAdapter(station.trains) { train, _ ->
            onTrainSelected(train, station)
        }
        rvTrains.layoutManager = LinearLayoutManager(this)
        rvTrains.adapter = adapter

        resetCoachSection()
        resetAnnouncement()
    }

    // ── Train selected ─────────────────────────────────────────────────────
    private fun onTrainSelected(train: Train, station: Station) {
        selectedTrain = train
        showCoachLayout(train)
        buildAnnouncement(train, station)
    }

    // ── Coach Layout ───────────────────────────────────────────────────────
    private fun showCoachLayout(train: Train) {
        tvSelectedTrainName.text = "${train.train_name}  •  ${train.train_name_kn}"

        val coachAdapter = CoachAdapter(train.coaches)
        rvCoaches.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rvCoaches.adapter = coachAdapter

        // Find first General coach position (1-indexed, excluding engine)
        val genIndex = train.coaches.indexOfFirst { it == "GEN" }
        if (genIndex >= 0) {
            layoutGeneralInfo.visibility = View.VISIBLE
            tvGeneralPosition.text = "Coach #${genIndex + 1} from Engine"
        } else {
            layoutGeneralInfo.visibility = View.GONE
        }
    }

    private fun resetCoachSection() {
        selectedTrain = null
        tvSelectedTrainName.text = "ರೈಲು ಆಯ್ಕೆ ಮಾಡಿ / Tap a train above"
        rvCoaches.adapter = null
        layoutGeneralInfo.visibility = View.GONE
    }

    // ── Announcement ───────────────────────────────────────────────────────
    private fun buildAnnouncement(train: Train, station: Station) {
        val genIndex = train.coaches.indexOfFirst { it == "GEN" }
        val genPos = if (genIndex >= 0) "Coach number ${genIndex + 1} from the engine" else "not available"

        // English announcement
        val english = "Attention passengers. Train number ${train.train_number}, " +
                "${train.train_name}, arriving at Platform ${train.platform} " +
                "at ${train.arrival_time}, going to ${train.destination}. " +
                "The General Coach is at $genPos. " +
                "Please stand at the correct position on the platform."

        // Kannada announcement (transliterated for TTS)
        val kannada = "ಗಮನಿಸಿ. ರೈಲು ಸಂಖ್ಯೆ ${train.train_number}, " +
                "${train.train_name_kn}, ಪ್ಲಾಟ್‌ಫಾರ್ಮ್ ${train.platform} ರಲ್ಲಿ " +
                "${train.arrival_time} ಕ್ಕೆ ಬರಲಿದೆ. " +
                "${train.destination_kn} ಗೆ ಹೋಗುತ್ತಿದೆ. " +
                "ಜನರಲ್ ಕೋಚ್ ಇಂಜಿನ್‌ನಿಂದ ${if (genIndex >= 0) genIndex + 1 else "?"} ನೇ ಕೋಚ್ ಆಗಿದೆ."

        currentAnnouncementText = "$kannada\n\n$english"

        tvAnnouncementText.text = currentAnnouncementText
    }

    private fun resetAnnouncement() {
        currentAnnouncementText = ""
        tvAnnouncementText.text = "ರೈಲು ಆಯ್ಕೆ ಮಾಡಿ ಘೋಷಣೆ ಕೇಳಿ\nSelect a train to hear the announcement"
    }

    // ── Speak Button ───────────────────────────────────────────────────────
    private fun setupSpeakButton() {
        btnSpeak.setOnClickListener {
            if (currentAnnouncementText.isEmpty()) {
                Toast.makeText(this, "ಮೊದಲು ರೈಲು ಆಯ್ಕೆ ಮಾಡಿ / Please select a train first", Toast.LENGTH_SHORT).show()
            } else {
                speak(currentAnnouncementText)
            }
        }
    }

    // ── Lifecycle ──────────────────────────────────────────────────────────
    override fun onDestroy() {
        tts?.stop()
        tts?.shutdown()
        super.onDestroy()
    }
}
