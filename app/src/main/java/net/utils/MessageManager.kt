package net.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.app.PendingIntent
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.provider.ContactsContract
import android.telephony.SmsManager
import net.entity.MessageEntity
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class MessageManager {
    companion object {
        private var i: MessageManager? = null
            get() {
                field ?: run {
                    field = MessageManager()
                }
                return field
            }

        @Synchronized
        fun get(): MessageManager {
            return i!!
        }
    }

    fun sendMessage(activity: Activity, phone: String, content: String) {
        val i = PendingIntent.getActivity(activity, 0, Intent(), 0)
        val sManager = SmsManager.getDefault()
        sManager.sendTextMessage(phone, null, content, i, null)
    }

    fun getMessage(context: Context): ArrayList<MessageEntity> {
        val result = ArrayList<MessageEntity>()
        val SMS_URI_ALL = "content://sms/"
        try {
            val cr = context.contentResolver
            val projection = arrayOf(
                "_id", "address", "person",
                "body", "date", "type"
            )
            val uri = Uri.parse(SMS_URI_ALL)
            val cur = cr.query(uri, projection, null, null, "date desc")
            if (cur!!.moveToFirst()) {
                val nameColumn = cur.getColumnIndex("person")
                val phoneNumberColumn = cur.getColumnIndex("address")
                val smsbodyColumn = cur.getColumnIndex("body")
                val dateColumn = cur.getColumnIndex("date")
                val typeColumn = cur.getColumnIndex("type")

                do {
                    val phone = cur.getString(phoneNumberColumn)
                    val content = cur.getString(smsbodyColumn)
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd hh:mm:ss")
                    val d = Date(cur.getString(dateColumn).toLong())
                    val date = dateFormat.format(d)
                    val typeId = cur.getInt(typeColumn)
                    var type = ""
                    type = when (typeId) {
                        1 -> {
                            "received"
                        }
                        2 -> {
                            "send"
                        }
                        else -> {
                            ""
                        }
                    }
                    val entity = MessageEntity(
                        phone = phone,
                        content = content,
                        date = date,
                        type = type
                    )
                    result.add(entity)
                } while (cur.moveToNext())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return result
    }

    @SuppressLint("Recycle")
    fun getPhoneContacts(uri: Uri, context: Context): Array<String?>? {
        val contact = arrayOfNulls<String>(2)
        //得到ContentResolver对象
        val cr: ContentResolver = context.contentResolver
        //取得电话本中开始一项的光标
        val cursor: Cursor? = cr.query(uri, null, null, null, null)
        if (cursor != null) {
            cursor.moveToFirst()
            //取得联系人姓名
            val nameFieldColumnIndex: Int =
                cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
            contact[0] = cursor.getString(nameFieldColumnIndex)
            //取得电话号码
            val ContactId: String =
                cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID))
            val phone: Cursor? = cr.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=" + ContactId, null, null
            )
            if (phone != null) {
                phone.moveToFirst()
                contact[1] =
                    phone.getString(phone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
            }
            phone!!.close()
            cursor.close()
        } else {
            return null
        }
        return contact
    }

    fun call(context: Context, phone: String) {
        val intent = Intent()
        intent.action = Intent.ACTION_DIAL
        intent.data = Uri.parse("tel:$phone")
        context.startActivity(intent)
    }
}