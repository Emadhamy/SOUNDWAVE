import android.content.Context
import android.content.Intent
import androidx.media3.session.MediaButtonReceiver as Media3ButtonReceiver

class MediaButtonReceiver : Media3ButtonReceiver() {
    
    override fun onReceive(context: Context, intent: Intent) {
        // يمكننا إضافة منطق مخصص هنا إذا لزم الأمر، ثم نمرر الطلب للأب
        try {
            super.onReceive(context, intent)
        } catch (e: Exception) {
            // تجاهل أي أخطاء محتملة أثناء معالجة الزر
            e.printStackTrace()
        }
    }
}