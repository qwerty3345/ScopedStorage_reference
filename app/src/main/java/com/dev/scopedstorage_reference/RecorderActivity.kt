package com.dev.scopedstorage_reference

import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.dev.scopedstorage_reference.databinding.ActivityRecorderBinding
import com.dev.scopedstorage_reference.util.FilePaths
import com.dev.scopedstorage_reference.util.PermissionUtil
import java.util.*

/**
 * 녹음기를 실행하기 위한 Dialog Activity
 *
 * 참고 : https://developer.android.com/guide/topics/media/mediarecorder
 */
class RecorderActivity : AppCompatActivity() {
    private val TAG: String = "##########"
    private lateinit var binding: ActivityRecorderBinding

    /** MediaPlayer 녹음기능 관련 멤버들 */
    private var isRecorderStopped: Boolean = false  // 녹음 일시정지 여부
    private var isRecording: Boolean = false        // 녹음 진행중 여부
    private var recordingTime: Long = 0L            // 녹음 시간 측정
    private lateinit var timer: Timer               // 녹음 시간 측정을 위한 Timer
    private lateinit var mediaRecorder: MediaRecorder   // 녹음하기 위한 MediaRecorder
    private val recordPath = FilePaths.getFilePath()?.path  // 녹음 파일을 저장할 경로
    private lateinit var recordOutput: String               // 녹음 파일 자체의 경로
    private fun setRecordOutputPath() = recordPath + "/recording_${System.currentTimeMillis()}.m4a" // 녹음 파일 경로 지정
    private val maxRecordingSec = 600

    /** MediaPlayer 재생기능 관련 멤버들 */
    lateinit var player: MediaPlayer            // 녹음파일 재생 할 MediaPlayer
    private var isPlaying: Boolean = false      // 녹음파일 재생중 여부


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecorderBinding.inflate(layoutInflater)
        val rootView = binding.root
        setContentView(rootView)

        this.setFinishOnTouchOutside(false) // setCancelable = false 랑 같은 기능
//        this.actionBar?.hide()

        /** 오디오 사용 권한 체크 */
        PermissionUtil.checkRecordAudioPermission(this)

        /** Q 미만 저장공간 권한 체크 */
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            PermissionUtil.checkStoragePermission(this)
        }

        /** 액티비티 다이얼로그 닫기 */
        binding.btnClosePopup.setOnClickListener {
            this.finish()
        }

        /** 녹음 시작 */
        binding.btnStartRecording.setOnClickListener {
            startRecording()
        }

        /** 녹음 중지 */
        binding.btnStopRecording.setOnClickListener {
            stopRecording()
        }

        /** API 24버전 미만은 pause 기능을 사용할 수 없어 버튼 가림 */
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            binding.btnPauseRecording.visibility = View.INVISIBLE
        }

        /** 녹음 일시정지 */
        binding.btnPauseRecording.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                pauseRecording()
            }
        }

        /** 녹음 완료 파일 재생 */
        binding.btnPlayStopRecording.setOnClickListener {
            if (!isRecording) {
                // 재생중이 아니면,
                if (!isPlaying) {
                    startPlayer()

                    // 미디어 재생 종료 시,
                    player.setOnCompletionListener {
                        binding.btnPlayStopRecording.text = "재생"
                        isPlaying = false

                        stopTimer()
                        resetTimer()
                    }
                } else {
                    // 재생 중이면,
                    stopPlayer()
                }
            }
        }

    }


    /** MediaRecorder 초기화 */
    private fun initRecorder() {
        mediaRecorder = MediaRecorder().apply {
            // initialize
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            // data source configure
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            recordOutput = setRecordOutputPath()
            setOutputFile(recordOutput)

            // 음질 높이기 위해 Rate 높여줌.
            // 테스트 녹음파일 용량 : 1분 - 1MB, 10분 - 10MB 1시간 - 60MB
            setAudioEncodingBitRate(128000) // bitrate
            setAudioSamplingRate(44100)
        }
    }

    /** Recording 시작 */
    private fun startRecording() {
        // 녹음 파일이 재생중이었으면 재생 정지
        if (isPlaying) {
            stopPlayer()
        }

        if (!isRecording) { // 녹음중이 아니면 녹음 시작
            try {
                initRecorder()
                mediaRecorder.prepare()
                mediaRecorder.start()
                isRecording = true
                startTimer()

                // 플레이어 메뉴 보이게 함
                binding.lyRecordingPlayer.visibility = View.INVISIBLE

                Toast.makeText(this, "녹음 시작!", Toast.LENGTH_SHORT).show()
                Log.e(TAG, "녹음 시작")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        // 녹음 중일 땐 화면이 항상 켜져있도록 설정
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    /** Recording 정지 */
    private fun stopRecording() {
        if (isRecording) {  // 녹음 중일 때만 녹음 정지
            mediaRecorder.stop()
            mediaRecorder.release()
            isRecording = false
            stopTimer()
            resetTimer()
            binding.btnPauseRecording.text = "Pause"

            Toast.makeText(this, "녹음 종료\n $recordPath 에 저장", Toast.LENGTH_SHORT).show()
            Log.e(TAG, "녹음 종료\n $recordPath 에 저장")

            // 플레이어 메뉴 보이게 함
            binding.lyRecordingPlayer.visibility = View.VISIBLE
        } else {
            Toast.makeText(this, "녹음 중이 아닙니다.", Toast.LENGTH_SHORT).show()
        }

        // 녹음 중지 시, 화면 항상 켜짐 해제
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }


    /** Recording 일시정지 (Api 24 이상 사용 가능) */
    @RequiresApi(Build.VERSION_CODES.N)
    private fun pauseRecording() {
        if (isRecording) {  // 녹음 중일때만 일시정지
            if (!isRecorderStopped) {
                mediaRecorder.pause()
                isRecorderStopped = true
                binding.btnPauseRecording.text = "녹음 재개"
                stopTimer()

                Toast.makeText(this, "일시정지함", Toast.LENGTH_SHORT).show()
                Log.e(TAG, "녹음 일시정지")
            } else {
                resumeRecording()
            }
        } else {
            Toast.makeText(this, "녹음 중이 아닙니다.", Toast.LENGTH_SHORT).show()
        }
    }

    /** Recording 재개 (pause후에)  (Api 24 이상 사용 가능) */
    @RequiresApi(Build.VERSION_CODES.N)
    private fun resumeRecording() {
        mediaRecorder.resume()
        binding.btnPauseRecording.text = "일시정지"
        isRecorderStopped = false
        startTimer()

        Toast.makeText(this, "이어서 녹음 시작", Toast.LENGTH_SHORT).show()
        Log.e(TAG, "이어서 녹음 시작")
    }

    /** MediaPlayer 초기화 */
    private fun initPlayer() {
        player = MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA).build()
            )
            setDataSource(recordOutput)
        }
    }

    /** MediaPlayer 재생 */
    private fun startPlayer() {
        initPlayer()
        player.prepare()
        player.start()

        isPlaying = true
        binding.btnPlayStopRecording.text = "재생 정지"

        startTimer()
    }

    /** MediaPlayer 정지 */
    private fun stopPlayer() {
        player.stop()

        isPlaying = false
        binding.btnPlayStopRecording.text = "재생"

        stopTimer()
        resetTimer()
    }


    /** 녹음 시간을 알려주기 위한 타이머 시작 */
    private fun startTimer() {
        if (isRecording || !isRecorderStopped) {
            timer = Timer()
            // TimerTask를 인자로 받음
            timer.scheduleAtFixedRate(object : TimerTask() {
                override fun run() {
                    recordingTime++

                    // UI Thread 에서 동작하도록.
                    runOnUiThread {
                        binding.tvRecordingTime.text = updateTimeString()
                        /** 레코딩 시간이 10분 이상이면 중지하고 저장 TODO: 시간제한 얼마로 둘지? (maxRecordingSec) */
                        if (recordingTime > maxRecordingSec) {
                            stopRecording()
                            Toast.makeText(this@RecorderActivity, "녹음 시간은 ${maxRecordingSec/60}분을 초과할 수 없습니다. \n자동 저장되었습니다.", Toast.LENGTH_LONG)
                                .show()
                        }
                    }
                }
                // 1초 간격으로 업데이트
            }, 1000, 1000)
        }
    }

    /** Timer stop */
    private fun stopTimer() {
        timer.cancel()
    }

    /** Timer reset */
    private fun resetTimer() {
        timer.cancel()
        recordingTime = 0
    }

    /** 녹음시간을 화면에 나타내기 위한 string */
    private fun updateTimeString(): String {
        val minutes = recordingTime / 60
        val seconds = recordingTime % 60

        return String.format("%02d:%02d", minutes, seconds)
    }


    // 권한 부여 후... -> 아직 아무 처리도 안함.
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PermissionUtil.REQUEST_RECORD_AUDIO_PERMISSION) {
            if (grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            }
        }
    }

}